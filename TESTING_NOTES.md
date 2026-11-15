# V4 verification notes — 2026-11-15

Completed:
- Integrated the actual launcher icon in all main bitmap densities, branded header icon, officer-journey image and JSON-based copy resources in native Kotlin source.
- Bilingual motivational/footer copy available from the packaged JSON; study illustration and rotation are wired into the Android Composables.
- Firebase banner admin accepts both flat and bilingual nested metadata with allowlisted internal destinations. Two live-eligible inspirational examples; new-paper announcement stays inactive pending publication.
- Notification sender supports bilingual admin-only message templates, and dry-run validation passed.
- V4 preview syntax validated and Playwright smoke-tested for original app icon, officer hero, English/Hindi slogans, common footer, tab language toggle, opening an exam and exam focus. No console page errors.
- Four bundled question JSON files validated: 4 × 150 = 600 question slots; some topics repeat.
- HTML preview inline scripts passed syntax validation.
- Playwright/Chromium smoke flow passed in a sandboxed DOM with a mocked localStorage implementation (because this sandbox prohibits file/HTTP navigation): home hero, exam countdown, simulated promo, Hindi toggle, ranking navigation, profile navigation, starting a mock, language toggle on exam. No page errors during that flow.
- Firebase engagement JSON dry run accepted; dry-run FCM sender checked (does not send).
- Python admin scripts passed `py_compile`.

Not completed:
- Android APK/AAB build, Gradle sync, on-device UI testing; Android SDK/Gradle wrapper binary unavailable in this runtime.
- Firebase / Firestore / FCM live deployment and connection to your project (requires your config and admin credentials).
- AdMob live-fill and UMP consent testing with your own IDs.
- Independent verification of 100 completed mocks (only 4 sample papers are bundled); full Hindi translation of secondary UI strings.
- Accessibility, Android 13+ permission flows on hardware, or app-store review.

Known design safeguards:
- Countdown never shows an unverified date; demo browser editor is explicitly simulated.
- App icon is original and not a BSSC/government emblem.
- Active exams show no ads. Promotional banners are independent editorial links, not AdMob creatives.
- Push messages are opt-in, topic based and do not require registration UI; no messages are sent from this source until an administrator runs the sender or uses Firebase Console.
