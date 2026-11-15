# V7 Firebase connection and testing

Firebase project: bssc-cgl. Android app: com.bssc.cgl4.free100mock.

- The Android client configuration and Google Services Gradle plugin are installed in this repository. This DOES NOT deploy server-side Firestore rules or upload test questions.
- In Firebase Authentication, enable Anonymous provider. In Firestore, deploy firebase/firestore.rules and firebase/firestore.indexes.json. Do not use open test-mode rules in production.
- To test banners: upload firebase/engagement.example.json with firebase/manage_engagement.py using Firebase Admin SDK credentials kept OUTSIDE GitHub. The active banner with highest priority appears on the Home page when the app fetches Firestore.
- To test questions: first publish just Mock 1 using firebase/import_content.py and the reviewed paper. Firestore requires mock_catalog/mock_001 and mock_papers/mock_001. Paper JSON must contain exactly 150 bilingual questions.
- To test FCM: install V7, grant notifications using the in-app bell (and Android 13+ permission), background the app, then use firebase/send_notification.py with Admin SDK to send to topic bssc_cgl4_updates.
- Important: 1-2 free, 3-50 rewarded ads, 51-100 premium ₹25 one-time bundle. Do NOT publish 51-100 before server-side purchase verification and Firestore entitlements are implemented. Current rules do not enforce a paid-content paywall.
- Firebase config contains public client identifiers, not Firebase Admin service account credentials. Do not store Razorpay secrets or Admin service-account JSON in the repository.
