package com.bssc.cgl4.free100mock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/** Clearly labeled Google banner for non-exam screens only. */
@Composable
fun SponsoredBanner(ads: AdController, modifier: Modifier = Modifier) {
    if (!ads.canShowBanner()) return
    Column(modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text("ADVERTISEMENT", fontSize = 10.sp, color = Muted)
        AndroidView(
            factory = { context -> ads.banner(context) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 55.dp),
            onRelease = { view -> view.destroy() }
        )
    }
}
