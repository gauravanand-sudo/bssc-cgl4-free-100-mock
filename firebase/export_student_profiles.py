#!/usr/bin/env python3
"""Admin-only registered student count and optional protected CSV export.

Requires pip install firebase-admin google-cloud-firestore; set
GOOGLE_APPLICATION_CREDENTIALS to a PRIVATE service account JSON path.
Never commit credentials or the exported CSV.
"""
import argparse
import csv
from pathlib import Path

def csv_safe(value):
    value = str(value if value is not None else "")
    return "'" + value if value[:1] in ("=", "+", "-", "@", "\t", "\r") else value

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--project", required=True)
    parser.add_argument("--export-csv", type=Path)
    args = parser.parse_args()
    import firebase_admin
    from firebase_admin import credentials, firestore
    if not firebase_admin._apps:
        firebase_admin.initialize_app(credentials.ApplicationDefault(), {"projectId": args.project})
    db = firestore.client()
    users = db.collection("student_profiles")
    count = users.count().get()[0][0].value
    print("Registered student profiles:", count)
    print("Numbers entered by students are NOT OTP-verified.")
    if args.export_csv is None:
        return
    args.export_csv.parent.mkdir(parents=True, exist_ok=True)
    with args.export_csv.open("w", newline="", encoding="utf-8-sig") as file:
        writer = csv.writer(file)
        keys = ("email", "displayName", "phoneE164", "phoneVerified",
                "whatsappOptIn", "createdAt", "updatedAt")
        writer.writerow(("uid",) + keys)
        for doc in users.stream():
            data = doc.to_dict() or {}
            writer.writerow((csv_safe(doc.id),) + tuple(csv_safe(data.get(k)) for k in keys))
    print("Sensitive CSV saved to:", args.export_csv)

if __name__ == "__main__":
    main()
