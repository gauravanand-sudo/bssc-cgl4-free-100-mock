# V19 · Flexible Firebase Home cards (OneShotGS)

**Current Android app: BSSC CGL 4 Free 100 Mock v0.19.0**
**Firebase project: `bssc-cgl`**
**Live changes: Firestore Console → Data → `home_cards`**

You can now publish and reorder as many as **80 visible Home cards** without releasing a new APK. Cards can be arranged in swipeable horizontal rows or full-width stacks, and placed near the **top**, **middle** or **bottom** of Home. Everything is optional; **zero active cards means zero extra boxes.**

This replaces the previous always-visible Cut-off tracker. No cutoff figures are hardcoded, and the old `exam_events.cutoffPublished`, `cutoffType`, and `cutoffMarks` fields no longer control the V19 Home screen.

## 1. Make a horizontal card in Firebase Console

Open [Firestore Database](https://console.firebase.google.com/project/bssc-cgl/firestore), choose **Start collection** or existing collection `home_cards`, and create document ID `study_tip_01`.

Add these fields using exactly the shown Firestore types:

| Field | Type | Example |
|---|---|---|
| `active` | Boolean | `true` |
| `sectionId` | String | `study` |
| `sectionTitleEn` | String | `📚 Study resources` |
| `sectionTitleHi` | String | `📚 अध्ययन सामग्री` |
| `sectionOrder` | Number (integer) | `30` |
| `sortOrder` | Number (integer) | `1` |
| `placement` | String | `middle` |
| `layout` | String | `horizontal` |
| `type` | String | `standard` |
| `theme` | String | `violet` |
| `iconEmoji` | String | `📚` |
| `titleEn` | String | `Practice 30 minutes daily` |
| `titleHi` | String | `रोज़ 30 मिनट अभ्यास करें` |
| `bodyEn` | String | `Keep your exam momentum going.` |
| `bodyHi` | String | `अपनी तैयारी जारी रखें।` |
| `buttonEn` | String | `Open mocks` |
| `buttonHi` | String | `मॉक खोलें` |
| `actionType` | String | `internal` |
| `actionValue` | String | `mock_list` |

**Save**. Your card appears on an open V19 Home screen automatically after Firebase authentication and the Firestore listener update. No app reinstall needed. To hide it instantly, change `active` to Boolean `false`.

The fields not used by that card may be omitted; defaults are supplied. All fields are **case sensitive**.

## 2. All editable options

| Field | Values / guidance |
|---|---|
| `type` | `standard` (text/emoji/link), `metric` (large `value`), `image` (prominent `imageUrl`), `cutoff` (source-labeled numeric metric) |
| `layout` | `horizontal` (swipeable cards) or `stacked` (full-width rows). Use the same layout for all cards in one section. |
| `placement` | `top` (before daily mission), `middle` (after official countdown), `bottom` (after latest mocks) |
| `theme` | `white`, `violet`, `blue`, `mint`, `gold`, `sunset` |
| `iconEmoji` | Any brief emoji, e.g. `🔥`, `🏆`, `🎯`, `📢`. If supplied, takes precedence over `iconName`. |
| `iconName` | `calendar`, `school`, `book`, `target`, `chart`, `trophy`, `news`, `gift`, `link`, `mail` |
| `titleEn`, `titleHi` | Bilingual headline; at least one title or an image required |
| `bodyEn`, `bodyHi` | Bilingual description; up to 400 characters each |
| `value` | Big number/summary, e.g. `135 min`, `100 mocks`, `330 / 600` (admin supplied, not automatically generated) |
| `badgeEn`, `badgeHi` | Optional capsule: `NEW`, `UPDATE`, `FREE`, etc. For cutoff, system supplies the mandatory claim badge instead. |
| `buttonEn`, `buttonHi` | Optional bilingual action text; a button label appears only when an actionable link is configured |
| `imageUrl` | Public direct HTTPS image, e.g. Firebase Storage download URL; use ≤400 KB, about 1000×500 px. Don't enter `gs://` paths or Drive preview pages. |
| `actionType` | `none`, `internal`, `external` |
| `actionValue` | For internal: `mock_list`, `insights`, `ranking`, `home`, `profile`; for external: public HTTPS URL |
| `sourceUrl` | Optional HTTPS source link; **required if cutoff claim is official** |
| `startsAt`, `endsAt` | Optional Firestore **Timestamp** fields to show/hide on schedule. No automatic deletion. |
| `sectionId` | Group slug (e.g. `news`, `cutoff`, `study`); cards sharing a slug form one section |
| `sectionTitleEn`, `sectionTitleHi` | Group heading; may be blank to show only cards, no hardcoded title |
| `sectionOrder` | Lower section numbers come first within each placement |
| `sortOrder` | Lower card numbers come first inside a section |

To add another horizontal card, create a *different Firestore document ID*, reuse the same `sectionId`, `placement` and `layout`, and increase `sortOrder`. The section becomes horizontally swipeable. You can make as many distinct sections as you need within the **80 visible-card performance limit**.

The Android client skips invalid card types, missing required images, unsafe links and invalid official-cutoff attribution instead of opening them.

## 3. How to publish cutoff cards (no fixed panel!)

Create one card per category in `home_cards`. Example ID `cutoff_ur`:

| Field | Type | Example |
|---|---|---|
| `active` | Boolean | `false` until reviewed |
| `type` | String | `cutoff` |
| `sectionId` | String | `cutoff` |
| `sectionTitleEn` | String | `🎯 Cut-off watch` |
| `sectionTitleHi` | String | `🎯 कट-ऑफ जानकारी` |
| `layout` | String | `horizontal` |
| `placement` | String | `middle` |
| `sortOrder` | Number | `1` |
| `theme` | String | `gold` |
| `iconEmoji` | String | `🎯` |
| `titleEn` | String | `UR cut-off` |
| `titleHi` | String | `सामान्य श्रेणी कट-ऑफ` |
| `value` | String | Your **actual validated/identified estimate** like `342 / 600` |
| `claim` | String | `expected` or `official` |
| `sourceUrl` | String | For official: URL of the genuine BSSC publication |
| `bodyEn`, `bodyHi` | String | Explain year/exam, source, methodology and applicability |

**No figures are pre-entered in the sample.** The sample cutoff card stays inactive until you supply your own reviewable numbers.

For `claim=expected`, the UI automatically says **EXPECTED · NOT OFFICIAL** (Hindi equivalent), even if you customize other fields. For `claim=official`, the UI displays **OFFICIAL · BSSC SOURCE** only when `sourceUrl` is a valid `https://bssc.bihar.gov.in/` link. Verify the official notice genuinely contains the shown value. Clicking a sourced cutoff opens the source. Firebase admins remain responsible for truthfulness.

If you haven't published any cutoff cards, there is **no cutoff panel or empty placeholder** on Home.

## 4. Links, images, announcements and custom promotions

- To add a YouTube/website link: `actionType=external`, `actionValue=https://YOUR_URL`, plus optional image/emoji and CTA labels.
- To link to a screen inside the app: `actionType=internal`, `actionValue=mock_list` (or `insights`, `ranking`, `home`, `profile`).
- To publish informational-only content: `actionType=none`, leave `actionValue` blank; no misleading clickable control appears.
- To add a graphical card: `type=image` and a direct public HTTPS `imageUrl`.
- To add a single full-width announcement: `layout=stacked`, `placement=top` or `middle`.
- To add a stat tile: `type=metric`, `value` set to your editorial fact, and a short subtitle.

The **existing single Firebase image promotion** (`promo_banners`) remains a different Home element: it shows one highest-priority active promo. `home_cards` adds optional editorial sections; **it does not create extra AdMob ads**, new paid ad placements or a second static promo banner.

## 5. Import several cards together (optional)

Example file: [firebase/home_cards.example.json](firebase/home_cards.example.json) contains **five inactive examples** (news, cutoff, metric, in-app link, image), so importing it cannot clutter your published Home accidentally.

```bash
# No credentials or writes; validates everything
python3 firebase/manage_home_cards.py --file firebase/home_cards.example.json

# After editing a private copy and configuring Firebase Admin:
export GOOGLE_APPLICATION_CREDENTIALS=/private/path/firebase-admin.json
python3 firebase/manage_home_cards.py --file my_home_cards.json --execute
```

Never upload the service-account key to the Android project or a public repository. For routine changes, the **Firebase Console** is simpler: edit fields or set `active=false` to hide cards. Avoid deleting historical records unless you intend to lose them.

## 6. Mandatory Firestore Rules deployment

The app queries `home_cards` where `active==true`. Its Firestore security rule permits authenticated users (silent Firebase Anonymous Auth) to read **active** cards, while all student client writes are denied.

1. Open [Firestore Rules](https://console.firebase.google.com/project/bssc-cgl/firestore/rules).
2. Copy the **entire latest** [firebase/firestore.rules](firebase/firestore.rules) from GitHub main, not just the new `home_cards` block.
3. Paste, review and **Publish**. Do not overwrite your other app rules with a fragment.
4. Create a test card and verify it appears on the phone; set `active=false` and confirm it disappears.

**I can update GitHub code, but cannot deploy your live Firestore Console changes from this session.**

## 7. Existing Firebase features remain

- `exam_events/cgl4_prelims`: exam countdown appears only when a confirmed date and genuine official BSSC notice URL have been supplied. Old cutoff fields in this collection are ignored by V19.
- `promo_banners`: one priority-controlled promotional image/link only.
- `mock_catalog`: published mocks can be `status=available` or `status=upcoming`; upcoming papers cannot be unlocked. See [FIREBASE_COMPLETE_SETUP_V18.md](FIREBASE_COMPLETE_SETUP_V18.md).
- Branding and support details: **OneShotGS**, **ruhionlabs@gmail.com**, 30-minute *best-effort* support target remain bundled and unchanged.
- Rewarded unlock tiers unchanged: 1–2 free; 3–50 one earned ad; 51–100 two earned ads. Timed exams remain ad-free.

## 8. Phone test and release limitations

Open V19 Home, activate two cards with identical `sectionId` and `layout=horizontal`, check scrolling, links, English/Hindi labels and image load on cellular data. Test a `stacked` section, an expected cutoff, an officially sourced cutoff, start/end timestamps and switching `active` off.

This is a DEBUG APK using Google demo ads. It is not a signed Google Play release. Available paper answer keys still require server-side ad-reward verification to be securely protected; four bundled offline mocks have documented repeated questions requiring editorial correction.
