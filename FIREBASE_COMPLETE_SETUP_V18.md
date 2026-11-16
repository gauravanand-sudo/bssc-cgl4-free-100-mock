# Firebase operations — BSSC CGL 4 / OneShotGS · V18

Project: **bssc-cgl** · Android application ID: **com.bssc.cgl4.free100mock**

This guide matches the V18 Android code. Live Firebase documents and security rules
are not deployed by a GitHub commit. Use Firebase Console or a privately authenticated
Admin SDK deployment. Students must not have Firestore write permissions.

## 1. Show a mock as UPCOMING (no questions required)

Firebase Console → Firestore Database → Data →
collection `mock_catalog` → document `mock_055` (replace 055).

| Field | Firestore type | Example |
| --- | --- | --- |
| `published` | Boolean | `true` |
| `status` | String | `upcoming` |
| `testId` | Number/integer | `55` |
| `sortOrder` | Number/integer | `55` |
| `title` | String | `Mock Test - 055` |
| `titleHi` | String | `मॉक टेस्ट - 055` |
| `subtitle` | String | `Coming soon` |
| `difficulty` | String | `Balanced` |
| `questionCount` | Number/integer | `150` |
| `durationMinutes` | Number/integer | `135` |
| `isFree` | Boolean | `false` (legacy field; access depends on testId) |
| `availableAt` | Timestamp | optional, an *estimated display date* |
| `comingSoonTextEn` | String | `New mock arriving this week` |
| `comingSoonTextHi` | String | `नया मॉक इस सप्ताह आएगा` |
| `version` | Number/integer | `1` |

The four offline starter mocks remain listed even if you publish only a new
Upcoming card. Where a remote catalogue document has the same test ID as an
offline starter, the Firebase status overrides the starter card.

**Critical distinction:** `published=true` makes the *catalogue card visible*,
including upcoming cards. `status=upcoming` makes the card non-playable and
disables rewarded ads. There is no need for a `mock_papers/mock_055` document
until the questions are ready. An old locally saved reward unlock is not enough
to open a mock marked upcoming.

`availableAt` is informative, **not an automatic launch switch**. A date may
change. The administrator must explicitly change `status` to `available`
after uploading and verifying the full paper.

**To release Mock 55:**
1. Validate the 150 English/Hindi questions, four options, correct answers
   and explanations. Review duplicates and difficulty.
2. Upload the approved question JSON to `mock_papers/mock_055` as
   `questionsJson` (String). It must contain a 150-question JSON array.
3. Update `mock_catalog/mock_055.status` from `upcoming` to `available`.
4. In the app's Tests tab, tap Refresh, or relaunch the app.
5. The standard access model now applies: 1–2 free, 3–50 one
   completed rewarded ad, 51–100 two. Already earned local unlocks remain.
6. The V18 `firebase/firestore.rules` blocks new paper reads for
   `status=upcoming`. Deploy it in Firebase Console → Firestore → Rules.

If `status` is missing on an older published catalogue document, V18 assumes
`available` for backward compatibility. Invalid nonempty status values fail
closed in the Android client; use only `upcoming` or `available`.

### Safe admin command (dry run by default)

```bash
python3 firebase/manage_mock_release.py --mock 55 --status upcoming \
  --message-en "Coming next week" --message-hi "अगले सप्ताह उपलब्ध"
python3 firebase/manage_mock_release.py --mock 55 --status upcoming \
  --available-at "2027-01-10T10:00:00+05:30"
python3 firebase/manage_mock_release.py --mock 55 --status available
```

Add `--execute` only when a private Firebase Admin credential has been
configured in `GOOGLE_APPLICATION_CREDENTIALS`. The release command refuses
to mark a mock available unless its 150-question paper is uploaded and
structurally valid. Never place that credential in GitHub or the Android APK.
To import a reviewed 150-question paper, use `firebase/import_content.py`.
The importer now merges catalogue metadata: uploading questions **does not
silently overwrite an existing `status=upcoming`**. After review and upload,
explicitly release the mock in Firebase Console or the admin helper.
A new document without a status uses legacy default `available` once published.


## 2. Home promotional banner — exactly ONE

Firestore → `promo_banners` → create/edit any document:

| Field | Type | Example |
| --- | --- | --- |
| `active` | Boolean | `true` |
| `priority` | Number | `100` |
| `imageOnly` | Boolean | `true` |
| `imageUrl` | String | `https://example.com/banner.png` |
| `targetUrl` | String | `https://example.com` |
| `buttonEn` / `buttonHi` | String | `Visit` / `देखें` |

The app shows **one eligible active banner**, highest `priority`.
Use a real, publicly accessible direct HTTPS image URL, not a `gs://` path,
and a destination you control. Other active records do not create
duplicates. Banner changes are delivered to the open app in real time.
Custom promotions are *not* Google AdMob paid banners.

## 3. Confirmed exam countdown (hidden until confirmed)

Firestore → `exam_events/cgl4_prelims`:

| Field | Type | Value |
| --- | --- | --- |
| `published` | Boolean | `true` |
| `nameEn` / `nameHi` | String | BSSC CGL 4 Prelims / बीएसएससी सीजीएल 4 प्रारंभिक |
| `officialConfirmed` | Boolean | `false` until verified |
| `examAt` | Timestamp | set to official exam date/time |
| `sourceUrl` | String | actual BSSC notice URL on `https://bssc.bihar.gov.in/` |

Set `officialConfirmed=true` only after reviewing the genuine announcement
and entering `examAt` as a Firestore Timestamp. It then displays IST countdown
days/hours/minutes. No confirmed date → no countdown. Editing fields changes
the open app in real time.

## 4. Category-wise cutoff (independently controlled)

Use the same `exam_events/cgl4_prelims` document:

| Field | Type | Example |
| --- | --- | --- |
| `cutoffPublished` | Boolean | `false` (until ready) |
| `cutoffType` | String | `expected` or `official` |
| `cutoffMarks` | Map: string → number | `UR: <verified value>, SC: <verified value>` |
| `cutoffSourceUrl` | String | actual source, official BSSC URL for official claims |
| `cutoffNoteEn` / `cutoffNoteHi` | String | explanation, bilingual |

No cut-off values are supplied or invented here. Setting `cutoffPublished=true`
with valid category values displays the tracker. Expected values are
explicitly labeled **not official**.

## 5. OneShotGS brand and support information

V18 includes the supplied OneShotGS logo as a local drawable in the splash
screen and the About section (Profile/More). This is intentionally
**bundled**, not downloaded from Firebase, so a database edit cannot swap
the official brand/support identity.

Brand: **OneShotGS Learning Apps**.
Email: **ruhionlabs@gmail.com** (tap the About support button to open email).
Published wording: **Resolution target within 30 minutes, best effort; complex
issues may take longer**. This is not a guaranteed service-level agreement.

The app remains independent and does **not** claim BSSC or Bihar-government
affiliation. For future cross-app changes, use a new signed app release.

## 6. Verify deployment

1. Deploy `firebase/firestore.rules` from the latest GitHub `main` commit.
2. In Firestore, create `mock_catalog/mock_055` with `published=true` and
   `status=upcoming` (no paper).
3. Install V18 debug APK, open Tests, Refresh, find Mock 55, confirm
   **Upcoming** badge, disabled action, bilingual message/date.
4. Confirm no rewarded ad is offered for Mock 55, even if previously unlocked.
5. Upload a reviewed paper and set status `available`, Refresh, verify the
   correct unlock tier and question access.
6. Check OneShotGS logo, email launch, support wording, and Home momentum
   white cards in both Hindi and English.
7. The APK remains a DEBUG build using Google demo ads and must not be
   mistaken for a signed/approved Google Play release.

**Security note:** Firebase rules for available published papers still do not
verify earned AdMob rewards. They prevent reads of Upcoming papers, not
scraping of available paid-with-ad content. Server-side reward verification
and entitlement enforcement remain production hardening work.
