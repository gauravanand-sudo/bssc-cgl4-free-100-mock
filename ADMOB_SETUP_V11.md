# AdMob integration — V11: SDK App ID complete

All values were provided directly by the app owner:

- **Android SDK App ID:** `ca-app-pub-3094504988564015~2693006627`
- **Banner:** `ca-app-pub-3094504988564015/6356919461`
- **Interstitial:** `ca-app-pub-3094504988564015/7237776357`
- **Rewarded:** `ca-app-pub-3094504988564015/8248760642`
- **Rewarded Interstitial:** `ca-app-pub-3094504988564015/3188005659`

These are client-side public identifiers, not passwords or service-account credentials.
They live in `gradle.properties` and are injected into the Android manifest
via `manifestPlaceholders["ADMOB_APP_ID"]` and `BuildConfig`.

## Build modes
- **Debug APK (V11 GitHub Release):** Always uses Google's demo AdMob App ID and demo ad units. It does not earn revenue and is suitable for local phone testing. It still uses Firebase project `bssc-cgl`.
- **Production release variant:** Uses the owner-supplied real AdMob App ID and four real ad units. The Gradle script validates their format and matching publisher prefixes.
- Native and App Open placements remain off in the production variant because those ad-unit IDs have not been created. No demo IDs are allowed as production fallbacks.
- CI runs `:app:processReleaseMainManifest` to validate production AdMob manifest configuration, but **does not upload any production APK/AAB**. Only the debug test APK is published to GitHub Releases.

## Before Google Play publication
1. Build a properly signed, reviewed **Android App Bundle (AAB)** using secure signing material that is never stored in the public repository.
2. Confirm AdMob app readiness, supported-store linking, privacy policy, Google UMP consent and Play Data Safety.
3. Verify ad placements, reward callback and premium gating against a **test device/test app**, not live ad clicks.
4. Firebase `app_config/monetization.premiumEnabled`: false means Mock 3–100 rewarded; true means Mock 3–49 rewarded and 50–100 ₹25 premium **coming soon**, pending secure payment integration. The Firestore rules from V9 must actually be deployed to enforce premium-mode reads.
5. The local rewarded unlock is not tamper-proof or server verified; production content access should use AdMob SSV and server-side entitlements. Keep premium purchase unavailable until authenticated, receipt-verified billing is ready.

## References
- https://developers.google.com/admob/android/quick-start
- https://developers.google.com/admob/android/test-ads
- https://support.google.com/admob/answer/9691433
