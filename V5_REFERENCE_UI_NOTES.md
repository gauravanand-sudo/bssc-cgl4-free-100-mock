# V5 reference UI — design mapping

Two user-provided PNG reference posters: `BSSC CGL 4 App Branding Showcase-2.png` and `BSSC CGL 4 Mock Test App Showcase-3.png`.

| User reference visual | Android implementation |
|---|---|
| BSSC-style prep badge / logo | Cropped owner-supplied poster concept `brand_icon.png` + launcher mipmaps; not official BSSC attribution |
| Purple-gold top bar + language selector | `Engagement.kt::GlobalExamHeader` |
| Motivating officer and study hero | `Engagement.kt::AnimatedStudyHero` |
| Countdowns and promo banners | `Engagement.kt::ExamCountdown` + `FirebasePromotion` |
| Six colorful dashboard shortcuts | `Dashboard.kt::DashboardScreen` |
| White mock list with FREE/AD UNLOCK | `Dashboard.kt::TestCatalogueScreen` |
| Active timed paper and review palette | `ExamScreen.kt::ExamScreen` |
| Trophy / score / weak analysis | `ResultsAndInsights.kt` |
| Ranking podium using actual data only | `LeaderboardAndProfile.kt::LeaderboardScreen` |
| Five purple bottom tabs | `MainActivity.kt` |
| Motivating launch with logo | `ReferenceSplash.kt` |
| Made with ❤️ for Bihar | `BrandCopy.kt::BiharLoveFooter` |

The source does not embed screenshots as non-interactive screen backgrounds. This is a faithful **adaptation**, not a guaranteed pixel-exact clone across device sizes.
