package com.bssc.cgl4.free100mock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/** Clearly labeled Google banner for non-exam screens only. */
@Composable
fun SponsoredBanner(ads: AdController, modifier: Modifier = Modifier) {
    if (!ads.canShowBanner()) return
    Column(modifier.fillMaxWidth().padding(vertical = 12.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color.White)
        .padding(horizontal = 9.dp, vertical = 10.dp)) {
        Text("ADVERTISEMENT", fontSize = 10.sp, color = Muted)
        AndroidView(
            factory = { context -> ads.banner(context) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            onRelease = { view -> view.destroy() }
        )
    }
}
