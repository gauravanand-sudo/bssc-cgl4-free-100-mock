# Firebase switch: rewarded vs ₹25 premium — V9

**Default:** Mock 1–2 free; **Mock 3–100 rewarded ad**.

**Firebase ON (premiumEnabled=true):** Mock 3–49 rewarded; **Mock 50–100 premium ₹25 one-time** (including Mock 50).

**Firebase OFF (premiumEnabled=false):** Mock 3–100 rewarded again.

## How to switch without releasing an APK
Firebase Console → project `bssc-cgl` → Firestore → `app_config` collection → `monetization` document.
Create/edit the boolean field `premiumEnabled`, set `false` for rewarded, `true` for premium.
Optional metadata `premiumPriceInr=25`, `premiumStartTest=50`, `premiumEndTest=100`; these are fixed by app policy, the boolean is the switch.
The app subscribes to this document in real time. If missing, its server-confirmed default is rewarded. If offline or permissions block config reads, mocks 50–100 show "Checking online access" rather than incorrectly granting access.

**Critical:** You MUST DEPLOY updated `firebase/firestore.rules` to project `bssc-cgl`. Committing the rules to GitHub does NOT deploy them. The new rules let anonymously authenticated students read the switch but never write it. When premium is ON, the rules block all new reads of Mock 50–100 papers until purchase verification is implemented. When OFF, they allow published papers; reward unlocks currently rely on UI/local storage, NOT server-verified AdMob rewarded SSV.

## Admin script
```bash
python firebase/set_monetization.py --mode rewarded
python firebase/set_monetization.py --mode premium
# Set GOOGLE_APPLICATION_CREDENTIALS to a private Admin SDK JSON outside the repo, then:
python firebase/set_monetization.py --mode premium --execute
python firebase/set_monetization.py --mode rewarded --execute
```

## Not yet live checkout
The ₹25 payment gateway/Play-compliant billing has NOT been implemented. Turning premium mode ON displays ₹25 Premium Pack Coming Soon and blocks access; it cannot accept or verify payments yet. We must implement verified payment and server entitlements before enabling paid purchases.

Older ad unlocks remain stored on the device but are ignored in premium mode; turning premium OFF makes them valid again. Already downloaded local papers cannot be remotely erased and an exam already underway is not interrupted. Do not claim this is a DRM system. In production, implement Server-Side Verification for rewarded ads and server-verified purchase entitlements.
