# BSSC CGL 4 — ad-only launch (V12)

## Current launch plan
- **Mock 1 and 2:** Free, no ad required to start.
- **Mock 3–100:** One voluntary rewarded ad unlocks that mock on the current device. Repeat attempts remain unlocked on that device.
- **No purchase screen, ₹25 pack, Razorpay, Google Play Billing, or Firebase premium switch** is active in this build.
- **Future payments:** Keep for a separate, carefully reviewed app update if demand warrants. Google Play digital-content billing requirements must be addressed then.

## AdMob
The real SDK AdMob App ID and four production ad-unit IDs remain configured
in Gradle. The GitHub debug APK uses Google demo ads (not revenue-generating).
A signed production app and successful AdMob store-readiness checks are needed
before earning live ad revenue. Test on device with demo ads only.

## Firebase
The student app no longer reads `app_config/monetization`. Updated
`firebase/firestore.rules` no longer uses `premiumEnabled` and permits
published Mock 1–100 paper reads for signed-in anonymous users.
**Deploy these rules to project `bssc-cgl`**; changing GitHub files does not
automatically deploy Firestore rules. Until updated rules are deployed, your
previous premium switch could still block Mock 50–100 on the server.
If the old document exists, changing `premiumEnabled` to `false` is a
temporary compatibility workaround, not a substitute for deploying the rules.

## Security and content
Rewarded unlock completion is currently stored in local preferences. Firestore
does NOT verify rewarded ad completion. Students with modified clients may
download published answer keys. For production content protection, use AdMob
server-side verification (SSV) and a backend-verified entitlement/token system.
Do not describe the existing reward gate as unbreakable.

Only upload and publish complete 150-question papers you've reviewed; the
project currently bundles just four starter papers. Future papers can be
published through Firebase without a new app release.

## Revenue expectations
Downloads alone create no ad revenue. Track daily active users, ad
requests/fill, rewarded completions, region-specific eCPM and retention in
AdMob after the real release, and adjust forecasts using measured results.
₹20,000 from 500 downloads would require an average of ₹40 in earned ad
revenue per download, which is not something to assume in advance.
