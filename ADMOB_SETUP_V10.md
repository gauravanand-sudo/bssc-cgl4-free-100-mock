# AdMob V10 — production IDs configured

Publisher: `ca-app-pub-3094504988564015`.

| Production placement | Ad unit |
|---|---|
| Banner (home and catalogue) | `ca-app-pub-3094504988564015/6356919461` |
| Interstitial (after results, frequency-capped) | `ca-app-pub-3094504988564015/7237776357` |
| Rewarded (mock unlocks) | `ca-app-pub-3094504988564015/8248760642` |
| Rewarded interstitial (optional opt-in alternative) | `ca-app-pub-3094504988564015/3188005659` |

## Required to finish

The **AdMob SDK App ID** has **not been provided**. It looks like
`ca-app-pub-3094504988564015~YYYYYYYYYY` (the exact suffix must be copied
from **AdMob → Apps → BSSC CGL 4 Free 100 Mock → App settings → App ID**).
The website URL's `/apps/2693006627/` number is NOT the SDK App ID, and the
ad-unit IDs cannot tell us the missing number. Do not guess it.

Add the real `ADMOB_APP_ID` as a Gradle property in private build settings or
as an approved repo configuration value; then build a properly signed release
for Play Console. Production builds validate the App ID and the four required
ad units, including matching publisher prefixes.

Native and App Open units have NOT been supplied. Both placements are
automatically **disabled in production** if their values remain blank, rather
than using Google demo units. No need to create them now.

## Phone testing and production behavior

DEBUG builds **always** use Google's demo ads, regardless of the four
configured production IDs. They do not earn money. Do not click your real
ads. For real-ad placement testing, first register the phone as a test device
in AdMob.

Access model in V9/V10: mocks 1–2 free; mocks 3–100 rewarded ads by
default. Firestore's `app_config/monetization.premiumEnabled` switches mocks
50–100 from rewarded to the ₹25 pack (checkout is NOT integrated yet).
Existing rewarded unlocks are local and do not constitute server-verified
entitlements. Use AdMob rewarded Server Side Verification (SSV) and a secure
backend before relying on ad unlocks to protect paid question content.

The app also shows editorial Firebase `promo_banners`; these are NOT
AdMob paid ads.

## Ad unit caps

The app itself shows interstitials only after leaving a completed result,
at most once every two finished mocks, at least four minutes apart.
Consider enabling a suitable ad unit-level cap for more control, but
AdMob frequency caps are not required to compile the app.

Official Google docs:
- https://developers.google.com/admob/android/quick-start
- https://developers.google.com/admob/android/test-ads
