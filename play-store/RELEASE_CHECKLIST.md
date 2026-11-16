# First production release — OneShotGS BSSC CGL 4

STATUS: Release build automation prepared; do not submit until live auth/QA and legal pages are ready.

Project and build:
- Application ID: com.bssc.cgl4.free100mock
- Release: versionCode 30, versionName 1.0.0
- Android targetSdk: 36, minSdk: 24
- Real AdMob banner, rewarded and interstitial unit IDs are already in gradle.properties.
- Native and App Open formats require additional publisher ad units before monetizing those formats.
- Manual Actions workflow: .github/workflows/build-play-aab.yml (NO auto-publish)

ONE-TIME SECURE UPLOAD KEY SETUP
1. Create one long-lived Play UPLOAD key, preferably using Android Studio > Build > Generate Signed Bundle / APK > Create new, or run keytool locally.
2. Keep secure copies offline. Do NOT upload the keystore, passwords or Play Console login tokens into chat. NEVER commit them to GitHub source.
3. In GitHub repo Settings > Secrets and variables > Actions, create repository or play-release environment secrets:
   PLAY_UPLOAD_KEYSTORE_B64    (base64-encoded binary JKS contents)
   PLAY_UPLOAD_STORE_PASSWORD (store password)
   PLAY_UPLOAD_KEY_ALIAS      (alias)
   PLAY_UPLOAD_KEY_PASSWORD   (key password)
   Your JKS file is normally small enough for a GitHub Actions secret. Use platform base64 tooling without inserting extra whitespace.
4. Create GitHub Actions variable GOOGLE_WEB_CLIENT_ID set to the Firebase project bssc-cgl WEB OAuth client ID, ending .apps.googleusercontent.com. This is a public identifier.
5. In Actions, run "Build Google Play signed AAB (manual only)"; download the private artifact containing app-release.aab and the PUBLIC upload certificate.
6. Enroll in Google Play App Signing when creating the Play release. Google generates the actual app-signing certificate, which is DIFFERENT from the upload key. Register the Google Play APP-SIGNING SHA-1/SHA-256 fingerprints in Firebase for release-installed Google Sign-In.

GOOGLE CONSOLE AND POLICY PREREQUISITES
- Enable Google and anonymous Firebase Authentication; deploy exact Firestore rules.
- Verify live name -> Google -> phone -> Firestore save -> Home, returning login, logout, deletion.
- Confirm 100 mock question claims match actual catalogue (avoid falsely claiming unavailable content).
- Final legal publisher details, privacy policy and deletion request URL at publicly accessible HTTPS links.
- Play Store icon 512x512; feature graphic; minimum two genuine Android device screenshots.
- Play Console: "Contains ads" = YES; Data Safety, app access for reviewer, content rating, target audience, official-affiliation disclaimer, country distribution, support info.
- Confirm latest approved logo source in GitHub. Existing small drawable is an older compressed rendition; review APKs used manual logo replacement.
- Run internal test of the Play-signed build on real phones; confirm Google sign-in with Play App Signing SHA and ads/consent.
- If the developer account is a personal account created after 13 November 2023, production access generally requires a closed test with at least 12 opted-in testers continuously for 14 days and approved production-access application.
- Review final Play Console warnings and pre-launch report before approving production roll-out.

Official references:
https://support.google.com/googleplay/android-developer/answer/11926878
https://developer.android.com/studio/publish/app-signing
https://support.google.com/googleplay/android-developer/answer/14151465
https://support.google.com/googleplay/android-developer/answer/13327111
https://support.google.com/googleplay/android-developer/answer/9866151
