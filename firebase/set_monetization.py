#!/usr/bin/env python3
"""Admin-controlled Firestore mode switch (dry-run unless --execute)."""
import argparse
import os

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", required=True, choices=["rewarded","premium"])
    parser.add_argument("--execute", action="store_true")
    args = parser.parse_args()
    enabled = args.mode == "premium"
    print("Project bssc-cgl; document app_config/monetization; premiumEnabled:", enabled)
    print("Mock 1-2 free; 3-49 rewarded; 50-100:", args.mode)
    if enabled:
        print("₹25 checkout is NOT integrated yet; this mode locks mocks 50-100.")
    if not args.execute:
        print("DRY RUN. Pass --execute with private Firebase Admin credentials to apply.")
        return
    if not os.environ.get("GOOGLE_APPLICATION_CREDENTIALS"):
        parser.error("Provide GOOGLE_APPLICATION_CREDENTIALS (private admin JSON outside GitHub).")
    import firebase_admin
    from firebase_admin import credentials, firestore
    app = firebase_admin.initialize_app(credentials.ApplicationDefault(), {"projectId":"bssc-cgl"})
    firestore.client(app).collection("app_config").document("monetization").set({
        "premiumEnabled": enabled,
        "premiumStartTest": 50,
        "premiumEndTest": 100,
        "premiumPriceInr": 25,
        "updatedAt": firestore.SERVER_TIMESTAMP
    }, merge=True)
    print("Firebase mode updated.")

if __name__ == "__main__":
    main()
