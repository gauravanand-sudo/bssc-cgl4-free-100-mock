#!/usr/bin/env python3
"""Validate and optionally upload Firebase Home horizontal cards.

No Firebase writes without --execute. Never commit service-account keys.
Example:
  python3 firebase/manage_home_cards.py --file firebase/home_cards.example.json
  GOOGLE_APPLICATION_CREDENTIALS=/private/key.json python3 firebase/manage_home_cards.py --file my_cards.json --execute

Use Firestore Console to edit one card at a time after creation.
"""
import argparse
import datetime as dt
import json
import os
import re
from pathlib import Path
from urllib.parse import urlsplit

TYPES={"standard","metric","image","cutoff"}
LAYOUTS={"horizontal","stacked"}
PLACEMENTS={"top","middle","bottom"}
THEMES={"white","violet","blue","mint","gold","sunset"}
ROUTES={"mock_list","insights","ranking","home","profile"}
ICON_NAMES={"calendar","school","book","target","chart","trophy","news","gift","link","mail"}
VALID_ACTIONS={"none","internal","external"}
CLAIMS={"expected","official"}


def https(value,required=False):
    if value is None or value=="":
        if required: raise ValueError("HTTPS URL required")
        return ""
    if not isinstance(value,str) or len(value)>2048:
        raise ValueError("URL must be a string <=2048 characters")
    url=urlsplit(value)
    if url.scheme!="https" or not url.hostname or url.username or url.password:
        raise ValueError(f"Only public HTTPS URLs allowed: {value!r}")
    if url.port is not None and url.port not in (443,):
        raise ValueError("Only standard HTTPS port 443 allowed")
    return value


def timestamp(value):
    if not value: return None
    if not isinstance(value,str): raise ValueError("Time must be a string")
    parsed=dt.datetime.fromisoformat(value.replace("Z","+00:00"))
    if not parsed.tzinfo or parsed.utcoffset() is None:
        raise ValueError("Dates require timezone, e.g. 2027-01-10T10:00:00+05:30")
    return parsed.astimezone(dt.timezone.utc)


def validate_card(raw):
    if not isinstance(raw,dict):
        raise ValueError("Every card must be a JSON object")
    x=dict(raw)
    card_id=x.pop("id",None)
    if not isinstance(card_id,str) or not re.fullmatch(r"[a-z][a-z0-9_-]{2,60}",card_id):
        raise ValueError(f"Invalid card document ID {card_id!r}")
    defaults={
        "active":False,"sectionId":"updates","sectionTitleEn":"",
        "sectionTitleHi":"","sectionOrder":50,"sortOrder":50,
        "placement":"middle","layout":"horizontal",
        "type":"standard","theme":"white",
        "iconEmoji":"","iconName":"","titleEn":"","titleHi":"",
        "bodyEn":"","bodyHi":"","value":"","badgeEn":"","badgeHi":"",
        "buttonEn":"","buttonHi":"","imageUrl":"",
        "actionType":"none","actionValue":"","claim":"",
        "sourceUrl":"","startsAt":None,"endsAt":None
    }
    allowed=set(defaults)
    if set(x)-allowed: raise ValueError(f"{card_id}: unexpected fields {sorted(set(x)-allowed)}")
    out={**defaults,**x}
    if type(out["active"]) is not bool:
        raise ValueError(f"{card_id}: active must be true/false")
    for key,options in (("type",TYPES),("theme",THEMES),
                        ("layout",LAYOUTS),("placement",PLACEMENTS),
                        ("actionType",VALID_ACTIONS)):
        if out[key] not in options: raise ValueError(f"{card_id}: invalid {key}: {out[key]!r}")
    if not re.fullmatch(r"[a-z][a-z0-9_-]{1,59}",str(out["sectionId"])):
        raise ValueError(f"{card_id}: sectionId must be slug-like")
    for field in ("sectionOrder","sortOrder"):
        if type(out[field]) is not int or not -1000<=out[field]<=1000:
            raise ValueError(f"{card_id}: {field} must be integer -1000..1000")
    lengths={
        "sectionTitleEn":90,"sectionTitleHi":90,
        "iconEmoji":12,"iconName":30,
        "titleEn":100,"titleHi":100,
        "bodyEn":400,"bodyHi":400,
        "value":40,"badgeEn":50,"badgeHi":50,
        "buttonEn":35,"buttonHi":35,
        "actionValue":2048,"imageUrl":2048,"sourceUrl":2048,"claim":20
    }
    for field,maxlen in lengths.items():
        v=out[field]
        if not isinstance(v,str) or len(v)>maxlen:
            raise ValueError(f"{card_id}: {field} must be text at most {maxlen} characters")
        out[field]=v.strip()
    if out["iconName"] and out["iconName"] not in ICON_NAMES:
        raise ValueError(f"{card_id}: unsupported iconName")
    if out["actionType"]=="internal" and out["actionValue"] not in ROUTES:
        raise ValueError(f"{card_id}: unsupported internal action")
    if out["actionType"]=="external":
        https(out["actionValue"],required=True)
    elif out["actionType"]=="none" and out["actionValue"]:
        raise ValueError(f"{card_id}: none action must have an empty actionValue")
    https(out["imageUrl"],required=out["type"]=="image")
    https(out["sourceUrl"])
    if not (out["titleEn"] or out["titleHi"] or out["imageUrl"]):
        raise ValueError(f"{card_id}: provide a title or image")
    if out["type"]=="cutoff":
        if out["claim"] not in CLAIMS:
            raise ValueError(f"{card_id}: cutoff claim must be expected or official")
        if out["claim"]=="official":
            https(out["sourceUrl"],required=True)
            if not out["sourceUrl"].startswith("https://bssc.bihar.gov.in/"):
                raise ValueError(f"{card_id}: official cutoff requires actual BSSC notice URL")
        if out["active"] and not out["value"]:
            raise ValueError(f"{card_id}: active cutoff must have verified/expected value")
    else:
        if out["claim"]:
            raise ValueError(f"{card_id}: claim is used only for cutoff")
    for field in ("startsAt","endsAt"):
        out[field]=timestamp(out[field])
    if out["startsAt"] and out["endsAt"] and out["startsAt"]>=out["endsAt"]:
        raise ValueError(f"{card_id}: startsAt must be earlier than endsAt")
    return (card_id,out)


def validate(data):
    cards=data.get("cards") if isinstance(data,dict) else data
    if not isinstance(cards,list): raise ValueError("Expected {cards:[...]} or JSON array")
    if len(cards)>100: raise ValueError("At most 100 cards per import; UI renders first 80")
    items=[validate_card(c) for c in cards]
    if len({i for i,_ in items})!=len(items):
        raise ValueError("Duplicate Firebase card document IDs")
    return items


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--file",default="firebase/home_cards.example.json")
    parser.add_argument("--execute",action="store_true",help="Write validated cards to Firestore")
    parser.add_argument("--project-id",default="bssc-cgl")
    args=parser.parse_args()
    patches=validate(json.loads(Path(args.file).read_text(encoding="utf-8")))
    for ident,fields in patches:
        print(f"home_cards/{ident}: {'ACTIVE' if fields['active'] else 'INACTIVE'}"
              f" · {fields['type']} · {fields['placement']}/{fields['layout']}"
              f" · section={fields['sectionId']}")
    if not args.execute:
        print("DRY RUN. No Firestore writes. Use --execute after reviewing content.")
        return
    if not os.getenv("GOOGLE_APPLICATION_CREDENTIALS"):
        parser.error("Set private GOOGLE_APPLICATION_CREDENTIALS outside your GitHub repo.")
    import firebase_admin
    from firebase_admin import credentials,firestore
    app=firebase_admin.initialize_app(credentials.ApplicationDefault(),
                                      options={"projectId":args.project_id})
    db=firestore.client(app)
    batch=db.batch()
    for ident,fields in patches:
        batch.set(db.collection("home_cards").document(ident),fields,merge=True)
    batch.commit()
    print(f"Wrote {len(patches)} Home card records. Toggle active in Firebase Console.")


if __name__=="__main__":
    main()
