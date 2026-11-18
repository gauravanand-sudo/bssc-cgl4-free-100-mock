# OneShotGS BSSC CGL 4 — Google Play 14-day closed-testing launch plan

Status: **READY TO CONFIGURE, NOT YET RUNNING**. Never count days before the Play closed-test release is live and testers have opted in. Do not substitute a GitHub debug APK for a Play-installed signed AAB.

## Eligibility and official requirements

Google Play Console personal developer accounts created after **2023-11-13** must have at least **12 testers opted into the closed track continuously for at least 14 days** before applying for production access. Testers who opt out and rejoin have a reset continuous period. Make a buffer group of 15–20 genuine testers, not bots or fake accounts. Production access is not automatic: the owner must apply, explain tester engagement and implemented improvements, and await Google review. An organization or older developer account may have different eligibility—verify the individual Play Console account.

Official: https://support.google.com/googleplay/android-developer/answer/14151465
Track setup: https://support.google.com/googleplay/android-developer/answer/9845334
Release setup: https://support.google.com/googleplay/android-developer/answer/9859348

## Before starting the clock

1. GitHub code: merge the complete modern Material 3 UI and Google login-after-logout fix to `main` once CI succeeds. No source changes to timed exam, mock scoring, AdMob network behavior or Firebase rules are needed just for the visual merge.
2. Android application ID: `com.bssc.cgl4.free100mock`; Google Play upload release versionCode 30 / versionName 1.0.0. Only one bundle may use this version code; increment for later test updates.
3. Use the **permanent** upload signing keystore stored in protected GitHub Actions secrets (never paste/upload the key into chat, repo or tickets). Required: `PLAY_UPLOAD_KEYSTORE_B64`, `PLAY_UPLOAD_STORE_PASSWORD`, `PLAY_UPLOAD_KEY_ALIAS`, `PLAY_UPLOAD_KEY_PASSWORD`. Protect branch/environment `play-release`.
4. Set GitHub Actions variable `GOOGLE_WEB_CLIENT_ID` with the real **Web application** OAuth client for Firebase project `bssc-cgl`. Use `Build Google Play signed AAB (manual only)` workflow and download the signed `app-release.aab` plus public upload-certificate fingerprints. No automatic Play publishing occurs.
5. Play Console: create/select app, enroll in Play App Signing, upload signed AAB **to closed testing only**. Get **app-signing SHA-1/SHA-256** from Play Console > App integrity, register them in Firebase. The app-signing cert differs from the *upload* cert and the debug APK cert.
6. Firebase: enable Google and Anonymous providers; configure `auth_public/google` and permissions, deploy approved Firestore rules, verify user-chosen name + 10-digit mobile are persisted. Test the **Play-installed** app's sign-in, logout and immediate sign-in again, cold start, and account deletion. Never use APK debug SHA for Play-installed sign-in.
7. Publish public HTTPS privacy policy and account-deletion request pages; confirm monitored `ruhionlabs@gmail.com`, retention terms and owner identity. Complete Play Console *Contains ads*, Data safety, target audience, content rating, independent-exam-app disclaimer, and app access/reviewer instructions.
8. Supply real app screenshots (at least two), approved icon and feature graphic. Do not claim 100 mocks live until they exist.
9. Configure testers under Play Console > Test and release > Testing > Closed testing. Add eligible Google Account emails via Play Console-managed email list or Google Group (do not store tester emails in GitHub). Make test available in India/other selected countries.
10. Roll out to closed testers, copy the **opt-in URL from Play Console**, ask testers to opt in using the same Google Account as Play Store and install via Play. Track the opt-in counts/dates in Console, not just chat replies.

## 14-day genuine-use plan (start relative to eligible opt-ins)

- **Day 0:** Publish closed-test AAB; share opt-in URL; verify at least 12 opted-in users (aim 15–20) and Play App Signing SHA login on actual Play-installed device.
- **Day 1:** Install, signup name -> Google -> mobile -> Firestore, verify welcome name appears and survives relaunch.
- **Day 2:** Logout -> Google login **without restarting**. Confirm picker opens, credentials accepted, previous data cleared on logout.
- **Day 3:** Cold-start login while already registered; no duplicate signup; anonymous fallback not blocked by protected login config.
- **Day 4:** Lost network, airplane mode, cancellation, slow network and retry; app never grants login without saved mobile.
- **Day 5:** User-chosen names in Hindi and English; mobile-number validation, profile edit, and Firestore persistence.
- **Day 6:** WhatsApp consent defaults off and is optional; no phone-number disclosure to other users.
- **Day 7:** Navigate five tabs, dynamic Home banners/cards, Hindi/English switching; accessibility/font scaling and small phones.
- **Day 8:** Unlock a mock via rewarded ad and test ad-unavailable fallback; no repeated accidental-click traps.
- **Day 9:** Complete an exam; timed questions remain ad-free, readable, scrollable and responsive.
- **Day 10:** Results, answer explanations, accuracy, timer, completion persistence; confirm no fake achievements.
- **Day 11:** Rankings/Insights, profile privacy, empty/invalid records, app resumes and back navigation.
- **Day 12:** AdMob banner/natives and UMP consent/opt-out; avoid encouraging testers to click ads. Audit crash/ANR and Android vitals in Play Console.
- **Day 13:** In-app account/data deletion, external deletion request page, Google re-auth recovery, and support email.
- **Day 14:** Confirm Play Console shows >=12 **continuously opted-in** testers for >=14 days. Collect genuine tester feedback and bug-fix record; apply for production access if the console permits it. Google may require additional testing.

Testers may test multiple features throughout the period; the day topics are a useful plan, not a way to bypass the opt-in/engagement requirement.

## Evidence to keep privately

Tester opt-in count and earliest eligible date, closed track release version, test installer source, anonymous crash/ANR events, app startup timings, issue tracker summaries, tester feedback, bugs fixed, and a release QA signoff. Avoid storing student email/phone data or tester personal details in the repo.

## Status guardrails

- The 14 days have **NOT begun** until an actual Play Console closed release is accessible and enough testers have opted in.
- GitHub Actions success is **not** a real-device login test.
- A GitHub debug APK is **not** the signed AAB for Play closed testing.
- Never claim that Play developer approval is guaranteed after 14 days.
