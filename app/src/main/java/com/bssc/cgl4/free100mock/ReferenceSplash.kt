package com.bssc.cgl4.free100mock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Clean, centered, native loading screen without full-height empty logo panels. */
@Composable
fun ReferenceSplash(hindi:Boolean) {
    val pulse=rememberInfiniteTransition(label="welcome")
    val scale by pulse.animateFloat(0.985f,1.015f,
        infiniteRepeatable(tween(1600),RepeatMode.Reverse),
        label="gentle brand pulse")
    var quote by remember {mutableIntStateOf(0)}
    LaunchedEffect(Unit) {
        while(true) {
            delay(2800L)
            quote=(quote+1)%5
        }
    }
    Box(Modifier.fillMaxSize().background(Ink).padding(horizontal=28.dp),
        contentAlignment=Alignment.Center) {
        Column(horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Surface(shape=RoundedCornerShape(28.dp),color=Color.White,
                shadowElevation=3.dp,
                border=androidx.compose.foundation.BorderStroke(1.dp,Hairline)) {
                Image(painterResource(R.drawable.bssc_bilingual_logo),
                    contentDescription="OneShotGS BSSC CGL 4 identity",
                    modifier=Modifier.size(182.dp).padding(12.dp).scale(scale),
                    contentScale=ContentScale.Fit)
            }
            Text("OneShotGS",color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=27.sp)
            Text(t(hindi,"BSSC CGL 4 · Free Mock Practice",
                "बीएसएससी सीजीएल 4 · निःशुल्क अभ्यास"),
                color=Violet,fontWeight=FontWeight.SemiBold,fontSize=13.sp,
                textAlign=TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(modifier=Modifier.width(128.dp)
                .height(5.dp).clip(RoundedCornerShape(5.dp)),
                color=Violet,trackColor=SoftLavender)
            Text(t(hindi,"Preparing your practice space…","आपका अभ्यास तैयार हो रहा है…"),
                color=Muted,fontSize=12.sp,textAlign=TextAlign.Center)
            AnimatedContent(targetState=quote,label="welcome message"){idx->
                Text(BrandCopy.loading(idx,hindi),color=Muted,
                    fontSize=11.sp,lineHeight=17.sp,textAlign=TextAlign.Center)
            }
        }
    }
}
