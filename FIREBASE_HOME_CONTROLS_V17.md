# V17 — One Firebase banner, confirmed exam countdown and cutoff tracker

## Exactly one Home promotion

The app now displays **one** promotion on Home, chosen from Firestore collection
`promo_banners`:

- Eligible documents must have `active=true`, a valid public HTTPS destination (or supported internal action), and, if `imageOnly=true`, a valid public HTTPS image URL.
- The app chooses the eligible banner with the **highest numeric `priority`**; tied priorities sort by document ID.
- Change `priority`, `active`, `imageUrl`, or `targetUrl` in Firebase Console: updates stream into an open app in real time without installing a new APK.
- There is no Featured for You heading, swipe carousel, page dots or image autoplay.
- The previous static officer hero, static mock-series promotional strip, Previous Papers shortcut, and all repetitive AdMob **display banner** placements have been removed.
- **Rewarded unlock ads and the naturally timed post-result interstitial remain enabled.** AdMob banner ID stays registered but unused by V17's visible UI.

Sample fields (Firestore types in parentheses):
```
promo_banners/my_campaign
  active: true               (boolean)
  priority: 100              (number)
  imageOnly: true            (boolean)
  imageUrl: https://YOUR_PUBLIC_HOST/banner.png      (string)
  targetUrl: https://YOUR_SITE_OR_YOUTUBE_LINK       (string)
  buttonEn: Visit             (string)
  buttonHi: देखें             (string)
```
The one active banner is removed from the screen if no eligible document exists.

If multiple old banners are still active in the live database, the app
**selects only one**. You can deactivate the redundant documents in the Console,
but doing so is not required for the app to look clean.

## Exam date — nothing displayed until officially confirmed

Firebase Console → project `bssc-cgl` → Firestore → collection `exam_events` →
document ID `cgl4_prelims`:

```
published: true                                (boolean)
nameEn: BSSC CGL 4 Prelims                      (string)
nameHi: बीएसएससी सीजीएल 4 प्रारंभिक           (string)
officialConfirmed: false                       (boolean)
examAt: [absent / null, until date announced]  (timestamp)
sourceUrl: https://bssc.bihar.gov.in/NoticeBoard.htm  (string)
```

When you have checked the **actual official BSSC notification**, set
`examAt` to the announced **Firestore Timestamp** (not a text date, not
Unix seconds), then set `officialConfirmed=true`. The app immediately
shows an attractive countdown to that date in **IST** (days, hours, minutes),
plus a link to the official notice. Until then, no exam countdown and no
guess or generic waiting card appears. The countdown hides after the date
passes. You can revise or withdraw the date in Firebase.

The date validation requires an official `https://bssc.bihar.gov.in/` source,
but an official-domain URL does not automatically verify that the notice
actually confirms the exam date. You must verify the notice content.

## Category-wise cutoff scores — configured separately

On the **same** `exam_events/cgl4_prelims` document set:

```
cutoffPublished: false             (boolean)
cutoffType: expected               (string: expected or official)
cutoffMarks: {}                    (map; category strings → number)
cutoffSourceUrl:                   (string: optional for expected, required official BSSC URL for official)
cutoffNoteEn:                      (string: short explanation, optional)
cutoffNoteHi:                      (string: short Hindi explanation, optional)
```

The Home **Cut-off tracker** card stays visible with "No cut-off published
yet" until valid data is published. No values are invented or hardcoded.

To populate it, add your *verified or deliberately marked estimated* scores
as number fields under map `cutoffMarks`, for example:
```
cutoffMarks:
  UR: [your verified value]
  EWS: [your verified value]
  EBC: [your verified value]
  BC: [your verified value]
  SC: [your verified value]
  ST: [your verified value]
```
Do not copy square-bracket placeholders literally into Firestore.

Set `cutoffType='expected'` for an estimate. The app clearly displays
**Expected · Not official** and labels the numbers as estimates.
Use `cutoffType='official'` only after official BSSC cutoff documents
are released, and provide an official BSSC `cutoffSourceUrl`.
Set `cutoffPublished=true` to display your configured values. It updates
in real time; setting false hides the numbers and restores the placeholder.
The date countdown and cutoff are **independent**: publishing one does not
force the other to appear.

The admin validation script supports these fields, and CI tests both
confirmed-date and official/expected cutoff constraints.
Dry run: `python3 firebase/manage_engagement.py --file firebase/engagement.example.json`
Actual deployment requires authenticated Firebase Admin credentials outside
GitHub; the default script never writes data.

## Security and release note

The Firestore `exam_events` and `promo_banners` read rules are already in
`firebase/firestore.rules`: authenticated students can only read published/
active public records and can never edit them. Admin SDK or Firebase Console
must set values. No Firebase live state/rules were changed by these code commits.

Debug APKs use Google demo ad units; a signed, approved Play release is still
required for real monetization. Existing rewards remain: mocks 3–50 need
one voluntary earned ad, mocks 51–100 need two, and the exam itself is ad-free.
