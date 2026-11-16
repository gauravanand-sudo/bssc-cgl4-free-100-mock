# ARCHIVED — this V9 payment switch has been retired in V12

The previously implemented Firebase premium/rewarded toggle is intentionally
**not used by the current Android app**. Current policy: Mock 1–2 free,
Mock 3–100 unlocked with rewarded ads only.

Do not use the old `firebase/set_monetization.py` script or premiumEnabled
field to control the V12 app. Deploy the latest `firebase/firestore.rules`
so older premium mode data will no longer block mock papers.

This file remains as a record of the earlier experiment. For active details
see `AD_ONLY_LAUNCH_V12.md`.
