# V22 - Free Google email + student-entered Indian mobile

## Functionality built
- Google sign-in with Android Credential Manager and Firebase Authentication.
- Students enter Indian mobile numbers, saved as +91XXXXXXXXXX. Mobile is UNVERIFIED.
- WhatsApp messaging opt-in is separate and off by default; no WhatsApp/SMS OTP.
- Firebase student_profiles/{uid} stores private email, name, phone and opt-in. Student can read/edit only their own profile, enforced by Firestore rules.
- Profile tab can edit phone or consent and sign out; delete account removes Firestore profile and leaderboard before deleting Firebase Auth user.
- Admin-only firebase/export_student_profiles.py prints count; --export-csv exports names, emails, numbers on a trusted admin machine.
- Debug APK optionally allows reviewing other screens WITHOUT registering; this does not count towards registered students.

## Essential Firebase owner setup (not possible from source code alone)
Existing app/google-services.json has "oauth_client": [] and cannot support Google sign-in by itself.
V22 also supports a **public Firestore webClientId config**, so activating Google after the APK is delivered does NOT require rebuilding that specific APK.
1. In https://console.firebase.google.com/project/bssc-cgl/authentication/providers enable Google as a sign-in provider and choose contact email.
2. In Firebase Project Settings > Android app com.bssc.cgl4.free100mock register the SHA-1 and SHA-256 of the exact APK signing certificate, NOT a different debug build.
3. Recommended for the delivered APK: in Cloud Firestore create collection auth_public, document google, Boolean field enabled=true, String field webClientId set to your OAuth **Web application** client ID ending in .apps.googleusercontent.com. This ID is public, not an API secret. The app reads it when Google Sign-In is tapped. No APK rebuild is required.
   Alternative for future source builds: refresh app/google-services.json with generated OAuth client, or set public GOOGLE_WEB_CLIENT_ID in gradle.properties.
4. Review and deploy firebase/firestore.rules using Firebase CLI with your Firebase account: firebase deploy --project bssc-cgl --only firestore:rules. These rules permit authenticated (including anonymous) reads of the single active auth_public/google client-ID document.
5. Test from an actual Android phone with internet: sign in, save profile, reload, edit opt-in, delete account.
6. Publish a real privacy policy and accurate Google Play Data safety information before distribution.
These operations require authenticated project-owner permissions. A Git commit does NOT deploy Firestore rules or activate Google authentication.

## Counting and exporting
Firebase Console > Firestore > student_profiles: each finished registration has exactly one doc keyed by Firebase UID.
Admin command:
  pip install firebase-admin google-cloud-firestore
  export GOOGLE_APPLICATION_CREDENTIALS=/private/path/service-account.json
  python firebase/export_student_profiles.py --project bssc-cgl
  python firebase/export_student_profiles.py --project bssc-cgl --export-csv ./private-students.csv
Keep credentials and CSV private. CSV export uses Firestore reads, subject to Spark limits. A manually typed number can be false; consent is not automated messaging permission outside Meta terms.

## APK status
V22 is a developer review build using Google test ads. It is NOT Play Store ready and cannot authenticate with Google until OAuth & SHA setup and rule deployment are complete. APK signature may change between builds; use the exact delivered APK SHA fingerprints.
