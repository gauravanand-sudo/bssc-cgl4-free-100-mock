"""Safely upsert Firebase-controlled promotional banners and official countdown events.

Usage: python firebase/manage_engagement.py --file firebase/engagement.example.json
       python firebase/manage_engagement.py --file firebase/engagement.example.json --execute
Always read the official BSSC notice before setting officialConfirmed true.
"""
import argparse
import datetime as dt
import json
import os
import re
from pathlib import Path
from urllib.parse import urlparse


def https_url(value, required=False):
    if not value:
        if required:
            raise ValueError('targetUrl/sourceUrl is required')
        return None
    p = urlparse(value)
    if p.scheme != 'https' or not p.netloc or p.username or p.password:
        raise ValueError(f'Only safe HTTPS URLs allowed: {value!r}')
    return value


def as_time(value):
    if not value:
        return None
    result = dt.datetime.fromisoformat(value.replace('Z', '+00:00'))
    if result.tzinfo is None:
        raise ValueError('Date-time must include timezone, e.g. 2027-01-10T09:00:00+05:30')
    return result.astimezone(dt.timezone.utc)


def validate(payload):
    patches = []
    for source, collection, ids in [('banners', 'promo_banners', 'active'), ('events', 'exam_events', 'published')]:
        for raw in payload.get(source, []):
            obj=dict(raw)
            ident=obj.pop('id')
            if not re.fullmatch(r'[a-z][a-z0-9_-]{2,60}', ident):
                raise ValueError(f'Invalid {source} ID: {ident}')
            if not isinstance(obj.get(ids), bool):
                raise ValueError(f'{ident}: {ids} must be true/false')
            if source=='banners':
                action_type=obj.get('action_type','external')
                action_value=obj.get('action_value','')
                allowed_routes={'mock_list','insights','ranking','home'}
                if action_type not in {'internal','external'}:
                    raise ValueError(f'{ident}: action_type must be internal or external')
                if action_type=='internal':
                    if action_value not in allowed_routes:
                        raise ValueError(f'{ident}: unsupported internal destination')
                    https_url(obj.get('targetUrl'),required=False)
                else:
                    https_url(obj.get('targetUrl'),required=True)
                https_url(obj.get('imageUrl'))
                # The legacy flat schema is still accepted for older banners.
                if 'title' in obj or 'subtitle' in obj or 'cta' in obj:
                    for field in ('title','subtitle','cta'):
                        values=obj.get(field)
                        if not isinstance(values,dict) or not all(
                            isinstance(values.get(lang),str) and len(values[lang])<=200
                            for lang in ('en','hi')):
                            raise ValueError(f'{ident}: {field} must have en and hi strings (max 200)')
                    # Keep fields nested for Android; no need to duplicate legacy fields.
                else:
                    for field in ('titleEn','titleHi','descriptionEn','descriptionHi','buttonEn','buttonHi'):
                        if not isinstance(obj.get(field),str) or len(obj[field])>200:
                            raise ValueError(f'{ident}: {field} must be a string (max 200)')
                if not isinstance(obj.get('priority'),int):
                    raise ValueError(f'{ident}: priority must be an integer')
                obj['startsAt']=as_time(obj.get('startsAt'))
                obj['endsAt']=as_time(obj.get('endsAt'))
                if obj['startsAt'] and obj['endsAt'] and obj['startsAt']>=obj['endsAt']:
                    raise ValueError('startsAt must be earlier than endsAt')
            else:
                https_url(obj.get('sourceUrl'),required=True)
                obj['examAt']=as_time(obj.get('examAt'))
                if obj.get('officialConfirmed'):
                    if not obj['examAt']:
                        raise ValueError(f'{ident}: confirmed exam needs a date')
                    if not obj['sourceUrl'].startswith('https://bssc.bihar.gov.in/'):
                        raise ValueError(f'{ident}: confirmation requires official BSSC notice source')
                if not isinstance(obj.get('officialConfirmed'),bool):
                    raise ValueError('officialConfirmed must be true/false')
            patches.append((collection,ident,obj))
    return patches


def main():
    ap=argparse.ArgumentParser(description=__doc__)
    ap.add_argument('--file',default='firebase/engagement.example.json')
    ap.add_argument('--execute',action='store_true',help='Actually publish writes, otherwise dry run')
    args=ap.parse_args()
    patches=validate(json.loads(Path(args.file).read_text(encoding='utf-8')))
    for coll,doc,obj in patches:
        print(f'{coll}/{doc} => {"active" if obj.get("active",obj.get("published")) else "inactive/draft"}')
    if not args.execute:
        print('DRY RUN: no writes performed. Use --execute after review.')
        return
    if not os.environ.get('GOOGLE_APPLICATION_CREDENTIALS'):
        raise SystemExit('GOOGLE_APPLICATION_CREDENTIALS must point to your private Admin SDK service account JSON (outside repo).')
    import firebase_admin
    from firebase_admin import credentials, firestore
    app=firebase_admin.initialize_app(credentials.ApplicationDefault())
    db=firestore.client(app)
    batch=db.batch()
    for collection,ident,obj in patches:
        batch.set(db.collection(collection).document(ident),obj,merge=True)
    batch.commit()
    print(f'Uploaded {len(patches)} Firebase documents successfully.')

if __name__=='__main__':
    main()
