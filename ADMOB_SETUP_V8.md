# AdMob setup — BSSC CGL 4 (V8)

## Current integration
The Android project already uses Google Mobile Ads SDK + UMP consent. Demo ads are enabled in **debug builds only**, regardless of configured production IDs.

### Access rules
- Mock 001–002: Free; no rewarded ad to start.
- Mock 003–050: Watch an opted-in rewarded ad to unlock that selected mock on this device. Reward granted ONLY by the Google earned-reward callback.
- Mock 051–100: Display ₹25 Premium Pack coming soon; NEVER offer a rewarded ad or paper access. Purchase verification/restore will be added later.

**Important security:** The client stores rewarded unlock state locally, which is not tamper-proof; Firestore anonymous rules for mocks 003–050 don't prove ad completion. Before production, add a signed AdMob Server Side Verification (SSV) callback + backend entitlement enforcement if server-level protection is needed. Premium paper reads must be restricted by Firestore until secure purchase entitlements exist. See `firebase/firestore.rules`. Rules committed to GitHub must also be DEPLOYED to your Firebase project; a GitHub commit does not deploy rules.

## In AdMob website
1. Sign in at https://admob.google.com
2. Apps > Add app > Android > **No** (not listed in a supported store yet), give app name **BSSC CGL 4 Free 100 Mock**, click Add.
3. Under app settings, copy your **AdMob App ID**. It has a tilde format `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY`.
4. Ad units > Add ad unit > **Rewarded**. Name it `Mock Unlock 03-50`, choose a relevant reward label/amount, save. Copy Ad unit ID (`ca-app-pub-XXXXXXXXXXXXXXXX/NNNNNNNNNN`).
5. Ad units > Add ad unit > **Banner**. Name it `Home / Tests Banner`, save, copy its Ad unit ID.
6. Optional production placements already implemented: Interstitial (after completed results), Native (test list), Rewarded Interstitial (explicit opt-in alternative), and App Open (safe home return only). To build a release with the current Gradle configuration, create all of their ad-unit IDs too. You can defer these and keep using test APKs.

Production properties (for private local/CI configuration, NOT a service-account or AdMob password):
```properties
ADMOB_APP_ID=ca-app-pub-...~...
ADMOB_REWARDED_ID=ca-app-pub-.../...
ADMOB_BANNER_ID=ca-app-pub-.../...
ADMOB_INTERSTITIAL_ID=ca-app-pub-.../...
ADMOB_REWARDED_INTERSTITIAL_ID=ca-app-pub-.../...
ADMOB_NATIVE_ID=ca-app-pub-.../...
ADMOB_APP_OPEN_ID=ca-app-pub-.../...
```

Production releases require all seven values to be real, valid IDs for their correct ad formats, and never Google's sample identifiers. They are used in the app manifest and BuildConfig at build time; never change debug IDs to live production IDs for personal testing. Keep production values out of debug builds.

## Validate
1. Install the V8 debug APK and navigate to Tests.
2. Mocks 001 and 002: Start immediately.
3. Mocks 003 and 004 (bundled): choose Watch Ad to Unlock; wait for demo rewarded ad completion; then begin mock.
4. Exit ad early: no unlock should be granted.
5. If mock 051+ is published in Firestore, its card should show **₹25 pack · Coming soon**, and no ad option.
6. During an exam, no banner or interstitial should interrupt.
7. After tests, check Profile > Privacy / ad choices for UMP where applicable.

Read https://support.google.com/admob/answer/9388275 to avoid invalid ad traffic. When the app goes live, it must be linked to an eligible store and pass app readiness review. Do not click your own live ads.

## Separate Firebase feature
Firestore `promo_banners` are editorial promotional messages, not paid Google AdMob ad units. They are configured separately.
