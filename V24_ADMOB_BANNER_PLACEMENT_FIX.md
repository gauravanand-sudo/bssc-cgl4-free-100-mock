# V24: fix test banner ad visibility

The app declared a `SponsoredBanner` Compose component but never placed it on an on-screen destination. Therefore existing Google AdMob banner test unit IDs could not display. The V24 changes:

- Home: show one clearly labeled AdMob banner below the daily mission.
- Tests: show one labeled banner below filter tabs, outside question/timed-exam UI.
- Compose `adAllowed` tracks consent/SDK initialization and triggers redraw.
- Banner SDK failure codes are visible on debug builds and logged under `OneShotGSAds`.
- Do not invent ads or replace failed real SDK requests with fake advertisements.
- Existing unlock-rewarded, post-result interstitial (3-minute cooldown), and app-return after 2+ minute app-open (5-minute cooldown) placements remain.
- Debug builds show Google's sample ads only, never earn revenue. Test ad display still depends on ad network, consent, region, and device.
- Firebase promotion is not an AdMob ad.
- The original publisher-approved logo stays in the post-build aligned/resigned review APK.
