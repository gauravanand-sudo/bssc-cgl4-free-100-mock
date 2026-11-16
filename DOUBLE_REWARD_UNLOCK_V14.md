# V14: two rewarded ads per Mock 51–100

- **Mock 1–2:** free, no ad gate.
- **Mock 3–50:** one completed rewarded ad unlocks that particular mock on this device.
- **Mock 51–100:** two completed rewarded ads unlock that particular mock on this device.
- The second ad is **not automatically launched**; the student must explicitly opt in again when the next ad becomes available.
- After Ad 1 of 2 earns its reward, the app saves `ad_credit_<mock id>=1` in local preferences and displays `1 of 2 completed`. They may leave and return later for Ad 2. No credit is granted if an ad is closed before Google's `onUserEarnedReward` callback.
- Once completed, the existing `unlocked` preference persists and repeat attempts do not require new ads.
- Existing unlocked mocks from V13 remain unlocked, including Mock 51–100. This prevents retroactively taking away an already-earned unlock.
- Both standard Rewarded and optional Rewarded Interstitial grant **one credit per fully-earned ad**, using the same counter; the quick option has a disclosure and Skip choice.
- UMP consent, banners, and the limited post-result interstitial remain unchanged; timed exams remain ad-free.
- Google explicitly requires publishers to disclose the fixed number of ads needed to obtain a reward: https://support.google.com/admob/answer/7313578
- Google AdMob SDK reward callback guidance: https://developers.google.com/admob/android/rewarded

## Backend limitation

As before, local preferences can be modified on a tampered device. Firestore anonymous reads also do not enforce rewarded-ad completion. Before treating the papers as protected commercial content, introduce signed AdMob Server-Side Verification (SSV) and Firebase-verified entitlements.

## Tests

The unit tests verify that every mock 3–50 requires one credit and every mock 51–100 requires two credits. Current repo only bundles 4 starter mocks; to test Mock 51 end-to-end on a real phone, publish a reviewed Mock 51 in Firebase and refresh the catalogue.

Debug APKs always use Google test ads; real AdMob IDs remain configured only in release builds.
