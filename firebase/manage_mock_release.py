#!/usr/bin/env python3
"""Create an Upcoming mock card or release it after the paper exists.

DRY RUN by default (no Firebase credentials needed):
  python3 firebase/manage_mock_release.py --mock 55 --status upcoming
  python3 firebase/manage_mock_release.py --mock 55 --status available

To write for project bssc-cgl, set private GOOGLE_APPLICATION_CREDENTIALS and add --execute.
Never commit a Firebase Admin key or create an Android client write permission.
"""
import argparse
import datetime
import json
import os


def parse_date(value):
    if not value:
        return None
    date = datetime.datetime.fromisoformat(value.replace("Z", "+00:00"))
    if date.tzinfo is None or date.utcoffset() is None:
        raise ValueError("availableAt must have an explicit timezone offset, e.g. +05:30")
    return date.astimezone(datetime.timezone.utc)


def make_update(number, status, title_en=None, title_hi=None,
                message_en=None, message_hi=None, available_at=None):
    if type(number) is not int or number not in range(1, 101):
        raise ValueError("Mock number must be 1..100")
    if status not in ("upcoming", "available"):
        raise ValueError("status must be upcoming or available")
    for label, value in (("title", title_en), ("titleHi", title_hi),
                         ("comingSoonTextEn", message_en),
                         ("comingSoonTextHi", message_hi)):
        if value is not None and (not isinstance(value, str) or len(value) > 160):
            raise ValueError(f"{label} must be text of at most 160 characters")
    payload = {
        "testId": number,
        "sortOrder": number,
        "published": True,
        "status": status,
        "questionCount": 150,
        "durationMinutes": 135,
        "isFree": number <= 2,
    }
    if status == "upcoming":
        payload.update({
            "title": title_en or f"Mock Test - {number:03d}",
            "titleHi": title_hi or f"मॉक टेस्ट - {number:03d}",
            "subtitle": "Coming soon",
            "difficulty": "Balanced",
            "comingSoonTextEn": message_en or "New mock launching soon",
            "comingSoonTextHi": message_hi or "नया मॉक जल्द उपलब्ध होगा",
        })
        when = parse_date(available_at)
        if when is not None:
            payload["availableAt"] = when
    else:
        # Releasing an existing paper doesn't silently create it.
        if available_at:
            raise ValueError("availableAt is an upcoming-only display hint")
        payload.update({
            "comingSoonTextEn": "",
            "comingSoonTextHi": "",
        })
    return payload


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mock", type=int, required=True)
    parser.add_argument("--status", choices=["upcoming", "available"], required=True)
    parser.add_argument("--title-en")
    parser.add_argument("--title-hi")
    parser.add_argument("--message-en")
    parser.add_argument("--message-hi")
    parser.add_argument("--available-at", help="ISO 8601 timezone-aware timestamp")
    parser.add_argument("--execute", action="store_true")
    parser.add_argument("--project-id", default="bssc-cgl")
    args = parser.parse_args()

    payload = make_update(args.mock, args.status, args.title_en, args.title_hi,
                          args.message_en, args.message_hi, args.available_at)
    doc_id = f"mock_{args.mock:03d}"
    print(f"Project {args.project_id}, mock_catalog/{doc_id}")
    print(json.dumps(payload, indent=2, default=str, ensure_ascii=False))
    if not args.execute:
        print("DRY RUN ONLY. Re-run with --execute and private Firebase Admin credentials.")
        return
    if not os.getenv("GOOGLE_APPLICATION_CREDENTIALS"):
        parser.error("Set private GOOGLE_APPLICATION_CREDENTIALS (never commit the JSON key).")
    import firebase_admin
    from firebase_admin import firestore
    app = firebase_admin.initialize_app(options={"projectId": args.project_id})
    db = firestore.client(app)
    if args.status == "available":
        paper = db.collection("mock_papers").document(doc_id).get()
        if not paper.exists:
            parser.error(f"{doc_id} has no uploaded mock_papers document. Upload and validate 150 bilingual questions first.")
        raw = (paper.to_dict() or {}).get("questionsJson")
        try:
            rows = json.loads(raw)
        except (TypeError, ValueError):
            parser.error(f"{doc_id}: questionsJson is missing or invalid")
        if not isinstance(rows, list) or len(rows) != 150 or any(
            not isinstance(q, dict) or not q.get("questionEn")
            or not q.get("questionHi") or len(q.get("optionsEn", [])) != 4
            or len(q.get("optionsHi", [])) != 4
            or q.get("correctIndex") not in range(4)
            for q in rows
        ):
            parser.error(f"{doc_id}: expected 150 structurally valid bilingual questions")
        payload["availableAt"] = firestore.DELETE_FIELD
    db.collection("mock_catalog").document(doc_id).set(payload, merge=True)
    print("UPDATED FIRESTORE. Refresh the Android mock catalogue to see the new status.")


if __name__ == "__main__":
    main()
