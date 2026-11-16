package com.bssc.cgl4.free100mock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/** Labeled real AdMob display banner, shown only outside the exam. */
@Composable
fun SponsoredBanner(ads: AdController, modifier: Modifier = Modifier) {
    if (!ads.canShowBanner()) return
    var bannerFailure by remember { mutableStateOf<Int?>(null) }
    Column(modifier.fillMaxWidth().padding(vertical=7.dp)
        .clip(RoundedCornerShape(13.dp)).background(Color.White)
        .padding(horizontal=9.dp, vertical=9.dp)) {
        Text("ADVERTISEMENT", fontSize=10.sp, color=Muted)
        AndroidView(factory={ context ->
            ads.banner(context,
                onLoaded={ bannerFailure=null },
                onFailed={ code -> bannerFailure=code })
        }, modifier=Modifier.fillMaxWidth().heightIn(min=60.dp),
            onRelease={ view -> view.destroy() })
        if (BuildConfig.DEBUG && bannerFailure!=null)
            Text("Google sample ad could not load (error " + bannerFailure +
                "). Check internet / consent / ad blocker.",
                color=Muted, fontSize=10.sp)
    }
}

/** SDK-backed native ad, labeled; never fabricated when fill is missing. */
@Composable
fun SponsoredNative(ads: AdController, modifier: Modifier = Modifier) {
    if(!ads.canShowNative()) return
    Column(modifier.fillMaxWidth().padding(vertical=11.dp)
        .clip(RoundedCornerShape(15.dp)).background(Color.White).padding(10.dp)) {
        Text("ADVERTISEMENT · SPONSORED", color=Muted, fontSize=11.sp,
            modifier=Modifier.padding(bottom=6.dp))
        AndroidView(factory={ context -> ads.nativeContainer(context) },
            modifier=Modifier.fillMaxWidth().heightIn(min=120.dp),
            onRelease={ view -> ads.releaseNative(view) })
    }
}
