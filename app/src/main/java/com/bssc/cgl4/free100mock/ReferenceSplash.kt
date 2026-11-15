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
            Image(painterResource(R.drawable.brand_icon),"Original Bihar recruitment exam prep app icon",
                Modifier.size(128.dp).clip(RoundedCornerShape(24.dp)))
            Spacer(Modifier.height(14.dp))
            Text("BSSC CGL 4",color=Color.White,fontSize=27.sp,fontWeight=FontWeight.Black)
            Text("Free 100 Mock",color=Gold,fontSize=24.sp,fontWeight=FontWeight.Black)
            Spacer(Modifier.height(9.dp))
            Text(BrandCopy.tagline(hindi),color=Gold,fontSize=19.sp,fontWeight=FontWeight.Bold)
        }
        Image(painterResource(R.drawable.officer_journey),"Journey from aspirant to future officer",
            Modifier.fillMaxWidth().height(200.dp).scale(scale).clip(RoundedCornerShape(23.dp)),
            contentScale=ContentScale.Crop)
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
        Text(BrandCopy.footer(hindi),color=Color.White,fontWeight=FontWeight.Bold,fontSize=13.sp)
    }
}
