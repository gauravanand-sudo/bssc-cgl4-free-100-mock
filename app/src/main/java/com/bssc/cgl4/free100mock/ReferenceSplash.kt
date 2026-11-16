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

@Composable fun ReferenceSplash(hindi:Boolean) {
    val pulse=rememberInfiniteTransition(label="aspirant loading")
    val scale by pulse.animateFloat(1f,1.045f,infiniteRepeatable(tween(2100),RepeatMode.Reverse),label="splash image pulse")
    var quote by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while(true) { delay(2100);quote=(quote+1)%5 } }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(RoyalDeep,Royal,Color(0xFF4A147A))))
        .padding(horizontal=28.dp,vertical=30.dp),horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.SpaceBetween) {
        Spacer(Modifier.height(4.dp))
        Column(horizontalAlignment=Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.brand_icon),
                "BSSC CGL 4 target and exam-book symbol",
                Modifier.size(100.dp).clip(RoundedCornerShape(22.dp)))
            Spacer(Modifier.height(10.dp))
            Text("बीएसएससी सीजीएल 4",color=Color.White,
                fontSize=22.sp,fontWeight=FontWeight.Black)
            Text("फ्री मॉक",color=Gold,
                fontSize=21.sp,fontWeight=FontWeight.Black)
            Text("BSSC CGL 4 Free Mock",color=Color.White,
                fontSize=17.sp,fontWeight=FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("लक्ष्य: 15,000 प्रश्न",color=Gold,
                fontSize=15.sp,fontWeight=FontWeight.Black)
            Text(t(hindi,"100 planned mock tests","100 नियोजित मॉक टेस्ट"),
                color=Color.White.copy(alpha=.86f),fontSize=11.sp)
        }
        Box(Modifier.fillMaxWidth().height(244.dp)
            .clip(RoundedCornerShape(23.dp)).background(Color.White),
            contentAlignment=Alignment.Center) {
            Image(painterResource(R.drawable.bssc_bilingual_logo),
                "बीएसएससी सीजीएल 4 फ्री मॉक · BSSC CGL 4 Free Mock · लक्ष्य: 15,000 प्रश्न",
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
        Column(horizontalAlignment=Alignment.CenterHorizontally) {
            Text(t(hindi,"Part of OneShotGS Learning Apps",
                "OneShotGS लर्निंग ऐप्स का हिस्सा"),
                color=Gold,fontSize=12.sp,fontWeight=FontWeight.Black)
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth(.80f).height(76.dp)
                .clip(RoundedCornerShape(15.dp)).background(Color.White)
                .padding(9.dp),contentAlignment=Alignment.Center) {
                Image(painterResource(R.drawable.oneshotgs_logo),
                    "OneShotGS brand logo",Modifier.fillMaxSize(),contentScale=ContentScale.Fit)
            }
        }
    }
}
