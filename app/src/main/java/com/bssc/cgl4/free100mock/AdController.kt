package com.bssc.cgl4.free100mock

import android.app.Activity
import android.content.Context
import android.graphics.Color as AndroidColor
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/** Consent-aware AdMob controller. Production supports four configured formats;
 * Native and App Open remain optional until their own ad-unit IDs are issued.
 * Debug builds always use Google's demo units.
 */
class AdController(private val activity: Activity) {
    private val consent = UserMessagingPlatform.getConsentInformation(activity)
    private var rewarded: RewardedAd? = null
    private var rewardedInterstitial: RewardedInterstitialAd? = null
    private var interstitial: InterstitialAd? = null
    private var appOpen: AppOpenAd? = null
    private var initialized = false
    private var showing = false
    private var loadRewarded = false
    private var loadRewardedInterstitial = false
    private var lastInterstitial = 0L
    private var lastAppOpen = 0L
    val ready: Boolean get() = rewarded != null
    val quickRewardReady: Boolean get() = rewardedInterstitial != null

    fun initialize(onChange: () -> Unit) {
        consent.requestConsentInfoUpdate(activity, ConsentRequestParameters.Builder().build(), {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                if (consent.canRequestAds()) start(onChange)
                onChange()
            }
            if (consent.canRequestAds()) start(onChange)
        }, {
            if (consent.canRequestAds()) start(onChange)
            onChange()
        })
    }
    private fun start(onChange: () -> Unit) {
        if (initialized) return
        initialized = true
        MobileAds.initialize(activity) { activity.runOnUiThread {
            preload(onChange)
            preloadQuick(onChange)
            preloadInterstitial()
            preloadAppOpen()
        } }
    }
    fun canShowBanner() = initialized && consent.canRequestAds()
    fun banner(context: Context): AdView = AdView(context).apply {
        val metrics = context.resources.displayMetrics
        val deviceWidthDp = (metrics.widthPixels / metrics.density).toInt().coerceAtLeast(280)
        setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, deviceWidthDp - 40))
        adUnitId = BuildConfig.ADMOB_BANNER_ID
        loadAd(AdRequest.Builder().build())
    }
    fun preload(onChange: () -> Unit) {
        if (!canShowBanner() || loadRewarded || rewarded != null) return
        loadRewarded = true
        RewardedAd.load(activity, BuildConfig.ADMOB_REWARDED_ID, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loadRewarded = false; rewarded = ad; onChange()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadRewarded = false; onChange()
                }
            })
    }
    /** Grant only from Google's on-earned callback; closing an ad is not an unlock. */
    fun showRewarded(onEarned: () -> Unit, onUnavailable: () -> Unit, onChange: () -> Unit) {
        if (showing) { onUnavailable(); return }
        val ad = rewarded ?: run { onUnavailable(); preload(onChange); return }
        rewarded = null; showing = true; onChange()
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { showing = false; preload(onChange) }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                showing = false; onUnavailable(); preload(onChange)
            }
        }
        ad.show(activity) { onEarned() }
    }
    private fun preloadQuick(onChange: () -> Unit) {
        if (!canShowBanner() || loadRewardedInterstitial || rewardedInterstitial != null) return
        loadRewardedInterstitial = true
        RewardedInterstitialAd.load(activity, BuildConfig.ADMOB_REWARDED_INTERSTITIAL_ID,
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    loadRewardedInterstitial = false; rewardedInterstitial = ad; onChange()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadRewardedInterstitial = false; onChange()
                }
            })
    }
    /** Caller MUST first show intro with reward details + a skip option. */
    fun showQuickReward(onEarned: () -> Unit, onUnavailable: () -> Unit, onChange: () -> Unit) {
        if (showing) { onUnavailable(); return }
        val ad = rewardedInterstitial ?: run { onUnavailable(); preloadQuick(onChange); return }
        rewardedInterstitial = null; showing = true; onChange()
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { showing = false; preloadQuick(onChange) }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                showing = false; onUnavailable(); preloadQuick(onChange)
            }
        }
        ad.show(activity) { onEarned() }
    }
    private fun preloadInterstitial() {
        if (!canShowBanner() || interstitial != null) return
        InterstitialAd.load(activity,BuildConfig.ADMOB_INTERSTITIAL_ID,AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
                override fun onAdFailedToLoad(error: LoadAdError) { interstitial = null }
            })
    }
    /** User-initiated completed-result exit only; never in an exam.
     * One interstitial per completed mock if available and >= 3 minutes apart.
     */
    fun afterResult(onContinue: () -> Unit) {
        val now = System.currentTimeMillis()
        val ad = interstitial
        if (ad == null || showing || now - lastInterstitial < 180_000L) {
            onContinue()
            if (!showing) preloadInterstitial()
            return
        }
        interstitial = null; showing = true; lastInterstitial = now
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { showing = false; onContinue(); preloadInterstitial() }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                showing = false; onContinue(); preloadInterstitial()
            }
        }
        ad.show(activity)
    }
    private fun preloadAppOpen() {
        if (!canShowBanner() || appOpen != null || BuildConfig.ADMOB_APP_OPEN_ID.isBlank()) return
        AppOpenAd.load(activity, BuildConfig.ADMOB_APP_OPEN_ID, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) { appOpen = ad }
                override fun onAdFailedToLoad(error: LoadAdError) { appOpen = null }
            })
    }
    /** Foreground re-entry only, after >=2 minutes away, on the home tab, NEVER on fresh startup. */
    fun onReturnToHome(afterBackgroundMs: Long) {
        val ad = appOpen ?: run { preloadAppOpen(); return }
        val now = System.currentTimeMillis()
        if (!canShowBanner() || showing || afterBackgroundMs < 120_000L ||
            now - lastAppOpen < 300_000L) return
        appOpen = null; lastAppOpen = now; showing = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { showing = false; preloadAppOpen() }
            override fun onAdFailedToShowFullScreenContent(error: AdError) { showing = false; preloadAppOpen() }
        }
        ad.show(activity)
    }
    fun showPrivacyOptions(onComplete: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { onComplete() }
    }
    /** A real SDK native placement; clearly labelled and not disguised as exam content. */
    fun nativeContainer(context: Context): FrameLayout = FrameLayout(context).apply {
        if (!canShowBanner() || BuildConfig.ADMOB_NATIVE_ID.isBlank()) return@apply
        val host = this
        val loader = AdLoader.Builder(context, BuildConfig.ADMOB_NATIVE_ID)
            .forNativeAd { native ->
                if (!host.isAttachedToWindow) { native.destroy(); return@forNativeAd }
                val container = NativeAdView(context)
                val column = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(22,16,22,16)
                    setBackgroundColor(AndroidColor.rgb(241,244,249))
                }
                val label = TextView(context).apply {
                    text = "Ad · Sponsored"; textSize = 10f; setTextColor(AndroidColor.DKGRAY)
                }
                val headline = TextView(context).apply {
                    text = native.headline; textSize = 17f; setTextColor(AndroidColor.rgb(30,39,60))
                }
                val body = TextView(context).apply {
                    text = native.body.orEmpty(); textSize = 12f; setTextColor(AndroidColor.DKGRAY)
                }
                val cta = Button(context).apply { text = native.callToAction ?: "Learn more" }
                column.addView(label);column.addView(headline)
                if (native.body != null) column.addView(body)
                if (native.callToAction != null) column.addView(cta)
                container.addView(column,ViewGroup.LayoutParams(-1,-2))
                container.headlineView = headline
                if (native.body != null) container.bodyView = body
                if (native.callToAction != null) container.callToActionView = cta
                container.setNativeAd(native)
                host.removeAllViews()
                host.addView(container, FrameLayout.LayoutParams(-1,-2,Gravity.CENTER))
                host.setTag(native)
            }.build()
        loader.loadAd(AdRequest.Builder().build())
    }
    fun releaseNative(view: FrameLayout) { (view.tag as? NativeAd)?.destroy(); view.removeAllViews() }
}
