# V13: higher ad exposure without interrupting exams

The Android app now has well-spaced adaptive banners on Home (2), Tests (bottom + after mock 8/20/32 in long lists), Results (after performance summary + after review entries 20/50 if scrolled), Analysis (bottom), Ranking (after intro), and Profile (between activity and privacy).

Interstitials occur only on the user-initiated exit from a newly completed mock's Result screen, either going Home or to Analysis. They require a loaded ad and a 3-minute minimum gap. They are NOT shown when starting exams, navigating tabs, submitting questions, or answering questions.

Rewarded ads remain opt-in for each mock 3–100, and only the earned-reward callback unlocks that mock locally. The quick rewarded-interstitial explicitly offers Skip. All adaptive banners are marked ADVERTISEMENT and separated from interactive controls.

The timed exam remains ad-free. No payment system is included. Firebase premium mode is retired and current Firestore rules must be deployed separately.

DEBUG builds ALWAYS use Google demo ads, regardless of configured production IDs. Live ads can monetize only in approved signed production releases. Do not click live ads, solicit clicks, or create artificially repeated impressions.

## Revenue model (purely illustrative)
For 100 daily active users: 4 banner impressions at ₹15 eCPM, 0.8 rewarded impressions at ₹100 eCPM and 0.3 interstitial impressions at ₹60 eCPM each day => approx ₹474 for 30 days. Values are hypotheses; AdMob eCPM Trends, traffic and match rates determine actual revenue.

Official guidance:
https://support.google.com/admob/answer/6066980
https://support.google.com/admob/answer/3342054
https://developers.google.com/admob/android/test-ads
