package com.bssc.cgl4.free100mock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * The single approved BSSC CGL 4 / OneShotGS badge appears as supplied,
 * without a second goal caption, an overlay, or an old competing app icon.
 */
@Composable fun ReferenceSplash(hindi:Boolean) {
    val pulse=rememberInfiniteTransition(label="aspirant loading")
    val scale by pulse.animateFloat(1f,1.025f,infiniteRepeatable(tween(2100),RepeatMode.Reverse),label="approved logo pulse")
    var quote by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while(true) { delay(2100);quote=(quote+1)%5 } }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(RoyalDeep,Royal,Color(0xFF4A147A))))
        .padding(horizontal=22.dp,vertical=28.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text("OneShotGS Learning Apps",color=Gold,
            fontSize=16.sp,fontWeight=FontWeight.Black)
        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(23.dp))
            .background(Color.White).padding(5.dp),contentAlignment=Alignment.Center) {
            Image(painterResource(R.drawable.bssc_bilingual_logo),
                "OneShotGS BSSC CGL 4 Free Mock approved bilingual logo",
                Modifier.fillMaxSize().scale(scale),contentScale=ContentScale.Fit)
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally) {
            Text(t(hindi,"Preparing your officer journey…","अधिकारी बनने का सफर तैयार हो रहा है…"),
                color=Color.White,fontSize=13.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(modifier=Modifier.fillMaxWidth().height(6.dp)
                .clip(RoundedCornerShape(16.dp)),color=Gold,trackColor=Color.White.copy(alpha=.20f))
            Spacer(Modifier.height(13.dp))
            AnimatedContent(targetState=quote,label="motivational splash text") {idx->
                Text(BrandCopy.loading(idx,hindi),color=Color.White.copy(alpha=.9f),
                    fontSize=12.sp,lineHeight=18.sp)
            }
        }
        Text(t(hindi,"Part of OneShotGS Learning Apps",
            "OneShotGS लर्निंग ऐप्स का हिस्सा"),
            color=Gold,fontSize=12.sp,fontWeight=FontWeight.Black)
    }
}
