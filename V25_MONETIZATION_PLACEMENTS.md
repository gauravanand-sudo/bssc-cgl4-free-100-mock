# V25 — More visible, policy-aware AdMob

The Android app includes:
- One always-visible **anchored adaptive banner** above navigation across Home, Tests, Insights, Rankings and More; not on timed exam, login or mobile-number entry.
- One **native advertisement** at a natural break in Home, after the fourth mock test card, Insights and Rankings, whenever a valid native ad-unit ID is configured.
- One AdMob banner in the **finished mock result review** (never on timed questions).
- Existing rewarded ad unlock, rewarded-interstitial option and post-result interstitial retained; 3-minute interstitial cooldown, no per-tap full-screen ads.
- Existing app-open ads only after ≥2 minutes backgrounding and ≥5 minutes between displays.
- Google sample test ads only in debug; these earn **no revenue**.

Production gradle.properties currently has real publisher banner/rewarded/interstitial IDs but **ADMOB_NATIVE_ID and ADMOB_APP_OPEN_ID are blank**. Create those ad units and configure them in the signing/release environment before expecting paid native/app-open ads. Never ship Google's sample IDs in a production release.

Never show ad overlays in timed questions or cover navigation/answer choices. Google forbids repeated surprise interstitials and accidental click traps.
Sources: https://support.google.com/admob/answer/6201362 ; https://developers.google.com/admob/android/banner ; https://developers.google.com/admob/android/native/advanced

The exact OneShotGS publisher logo must be included in packaged review APK and Android resources correctly aligned and signed.
