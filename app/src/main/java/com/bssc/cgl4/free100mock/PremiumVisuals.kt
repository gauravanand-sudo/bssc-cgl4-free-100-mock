package com.bssc.cgl4.free100mock

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Data-true, light-weight product visuals. No fake streaks/ranks or endless animation. */
@Composable
fun GlowingStat(emoji:String,label:String,number:Int?,fallback:String="—",
                accent:Color=Violet,modifier:Modifier=Modifier) {
    var reveal by remember(number) { mutableStateOf(false) }
    LaunchedEffect(number) { reveal=true }
    val counter by animateIntAsState(if(reveal) (number ?: 0) else 0,
        animationSpec=tween(650),label="metric count up")
    // Clean WHITE foundation replaces the old translucent/muddy gradients.
    Column(modifier.clip(RoundedCornerShape(20.dp))
        .background(CardInk).border(1.dp,Hairline,RoundedCornerShape(20.dp))
        .padding(horizontal=13.dp,vertical=12.dp),
        verticalArrangement=Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp))
                .background(accent.copy(alpha=.12f)),
                contentAlignment=Alignment.Center) {
                Text(emoji,fontSize=19.sp)
            }
            Spacer(Modifier.weight(1f))
        }
        Text(if(number==null) fallback else counter.toString(),
            fontWeight=FontWeight.Bold,color=SoftWhite,fontSize=25.sp)
        Text(label,fontWeight=FontWeight.Medium,color=Muted,
            fontSize=11.sp,maxLines=2,lineHeight=15.sp)
    }
}

/** Compact horizontal stats: one scrollable row, less vertical than a grid. */
@Composable
fun HomeMomentum(attempts:List<Attempt>,hindi:Boolean) {
    val completed=attempts.map{it.testId}.distinct().size
    val streak=PrepMomentum.currentStreak(attempts)
    val best=attempts.maxOfOrNull{it.score}
    val accuracy=PrepMomentum.accuracyPercent(attempts)
    LazyRow(horizontalArrangement=Arrangement.spacedBy(9.dp),
        contentPadding=PaddingValues(vertical=3.dp)) {
        item {
            GlowingStat("📚",t(hindi,"Mocks finished","मॉक पूरे"),completed,
                accent=Violet,modifier=Modifier.width(132.dp))
        }
        item {
            GlowingStat("🔥",t(hindi,"Day streak","दिन की स्ट्रीक"),streak,
                accent=Color(0xFFCB631A),modifier=Modifier.width(132.dp))
        }
        item {
            GlowingStat("🏅",t(hindi,"Best score","सर्वश्रेष्ठ स्कोर"),best,
                accent=Color(0xFF86600A),modifier=Modifier.width(132.dp))
        }
        item {
            GlowingStat("🎯",t(hindi,"Accuracy %","सटीकता %"),accuracy,
                accent=Aqua,modifier=Modifier.width(132.dp))
        }
    }
}

@Composable
fun TodayMissionCard(attempts:List<Attempt>,hindi:Boolean,onPractice:()->Unit) {
    val done=PrepMomentum.completedToday(attempts)
    val streak=PrepMomentum.currentStreak(attempts)
    val progress by animateFloatAsState(if(done) 1f else 0f,
        animationSpec=tween(650),label="daily goal")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
        .background(CardInk).border(1.dp,Hairline,RoundedCornerShape(22.dp))
        .padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                .background(SoftLavender),contentAlignment=Alignment.Center) {
                Icon(if(done) Icons.Rounded.CheckCircle else Icons.Rounded.Bolt,
                    contentDescription=null,tint=Violet,modifier=Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(t(hindi,"DAILY TARGET","आज का लक्ष्य"),
                    fontSize=10.sp,color=Violet,fontWeight=FontWeight.Bold,
                    letterSpacing=.9.sp)
                Spacer(Modifier.height(4.dp))
                Text(if(done)t(hindi,"Mission completed!","आज का लक्ष्य पूरा!")
                    else t(hindi,"Complete one mock today","आज एक मॉक पूरा करें"),
                    fontSize=16.sp,color=SoftWhite,fontWeight=FontWeight.SemiBold)
            }
            Surface(shape=RoundedCornerShape(12.dp),color=Color(0xFFFFF3DD)) {
                Text("🔥 $streak",color=Amber,
                    modifier=Modifier.padding(horizontal=9.dp,vertical=7.dp),
                    fontSize=12.sp,fontWeight=FontWeight.SemiBold)
            }
        }
        LinearProgressIndicator(progress={progress},modifier=Modifier.fillMaxWidth()
            .height(6.dp).clip(RoundedCornerShape(9.dp)),color=Violet,
            trackColor=SoftLavender,gapSize=0.dp,drawStopIndicator={})
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text(if(done)t(hindi,"Keep your streak going tomorrow.",
                "कल भी अपनी तैयारी जारी रखें।")
                else t(hindi,"A focused practice session builds confidence.",
                    "नियमित अभ्यास से आत्मविश्वास बढ़ता है।"),
                color=Muted,fontSize=11.sp,lineHeight=16.sp,
                modifier=Modifier.weight(1f))
            TextButton(onClick=onPractice,
                contentPadding=PaddingValues(horizontal=7.dp,vertical=4.dp)) {
                Text(t(hindi,"Practice","अभ्यास"),color=Violet,
                    fontWeight=FontWeight.SemiBold,fontSize=12.sp)
                Icon(Icons.Rounded.ArrowForward,contentDescription=null,tint=Violet,
                    modifier=Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ScoreMedallion(score:Int,modifier:Modifier=Modifier,size:Dp=150.dp) {
    var show by remember(score) {mutableStateOf(false)}
    LaunchedEffect(score){show=true}
    val sweep by animateFloatAsState(if(show) ((score.coerceAtLeast(0))/600f).coerceIn(0f,1f) else 0f,
        animationSpec=tween(1100),label="score ring")
    val count by animateIntAsState(if(show) score else 0,
        animationSpec=tween(1050),label="score number")
    Box(modifier.size(size).clip(CircleShape).background(Color.White),
        contentAlignment=Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(11.dp)) {
            val line=10.dp.toPx()
            drawArc(Color(0xFFEADFF8),startAngle=-90f,sweepAngle=360f,
                useCenter=false,style=Stroke(width=line,cap=StrokeCap.Round))
            drawArc(Brush.sweepGradient(listOf(Aqua,Violet,Gold,Aqua)),startAngle=-90f,
                sweepAngle=360f*sweep,useCenter=false,
                style=Stroke(width=line,cap=StrokeCap.Round))
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally) {
            Text("YOUR SCORE",color=Muted,fontWeight=FontWeight.Black,fontSize=9.sp)
            Text("$count",color=RoyalDeep,fontWeight=FontWeight.Black,fontSize=35.sp)
            Text("/ 600",color=Muted,fontSize=11.sp,fontWeight=FontWeight.Bold)
        }
    }
}

@Composable
fun CelebrationParticles(modifier:Modifier=Modifier) {
    val progress=remember {Animatable(0f)}
    LaunchedEffect(Unit) {progress.animateTo(1f,tween(1600))}
    Canvas(modifier.height(60.dp).fillMaxWidth()) {
        val palette=listOf(Gold,Aqua,Color(0xFFFF719A),Violet,Color(0xFF65B5FB))
        for(i in 0 until 32) {
            val x=(size.width*(i+0.5f)/32f)
            val y=size.height*(.48f+(i*17%11)/24f) +
                (1f-progress.value)*(-size.height*.7f) +
                progress.value*((i*13%9)/13f)*size.height
            drawCircle(palette[i%palette.size],radius=(2f+(i%3))*density,
                center=Offset(x,y),alpha=1f-progress.value*.65f)
        }
    }
}

/** Real score history (chronological), charted without pretending missing data exists. */
@Composable
fun ScoreTrendChart(scores:List<Int>,modifier:Modifier=Modifier) {
    if(scores.isEmpty()) return
    Canvas(modifier.fillMaxWidth().height(148.dp)) {
        val left=12.dp.toPx()
        val right=size.width-12.dp.toPx()
        val top=16.dp.toPx()
        val bottom=size.height-16.dp.toPx()
        val count=scores.size
        val step=if(count==1)0f else (right-left)/(count-1)
        // Official scoring can be -150…600; clamp within the scoring scale.
        val points=scores.mapIndexed {index,raw->
            val fraction=((raw.coerceIn(-150,600)+150)/750f).coerceIn(0f,1f)
            Offset(if(count==1)(left+right)/2 else left+index*step,
                bottom-fraction*(bottom-top))
        }
        for(j in 0..3){
            val y=top+(bottom-top)*j/3
            drawLine(Color(0xFFE6DAF2),Offset(left,y),Offset(right,y),1.dp.toPx())
        }
        if(points.size>1){
            val area=Path().apply {
                moveTo(points.first().x,bottom)
                lineTo(points.first().x,points.first().y)
                points.drop(1).forEach{lineTo(it.x,it.y)}
                lineTo(points.last().x,bottom)
                close()
            }
            drawPath(area,brush=Brush.verticalGradient(
                listOf(Violet.copy(alpha=.32f),Violet.copy(alpha=.02f))))
            for(j in 0 until points.lastIndex)
                drawLine(Violet,points[j],points[j+1],3.dp.toPx(),cap=StrokeCap.Round)
        }
        points.forEachIndexed {index,pt->
            drawCircle(if(index==points.lastIndex) Aqua else Violet,
                radius=5.dp.toPx(),center=pt)
            drawCircle(Color.White,radius=2.dp.toPx(),center=pt)
        }
    }
}

@Composable
fun SubjectDonut(name:String,accuracy:Float,modifier:Modifier=Modifier) {
    val value=accuracy.coerceIn(0f,1f)
    var reveal by remember(name,value){mutableStateOf(false)}
    LaunchedEffect(name,value){reveal=true}
    val progress by animateFloatAsState(if(reveal)value else 0f,
        animationSpec=tween(1000),label="subject donut")
    Column(modifier,horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(82.dp),contentAlignment=Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(7.dp)) {
                val thickness=8.dp.toPx()
                drawArc(Color(0xFFE7D7F5),-90f,360f,false,
                    style=Stroke(thickness,cap=StrokeCap.Round))
                drawArc(if(value>=.7f)Aqua else if(value>=.4f)Violet else Red,
                    -90f,360f*progress,false,
                    style=Stroke(thickness,cap=StrokeCap.Round))
            }
            Text("${(value*100).toInt()}%",color=SoftWhite,
                fontWeight=FontWeight.Black,fontSize=16.sp)
        }
        Text(name,color=SoftWhite,fontSize=10.sp,
            fontWeight=FontWeight.Bold,textAlign=TextAlign.Center,
            lineHeight=12.sp,maxLines=2)
    }
}

/** Non-blocking in-app celebration of one legitimately earned ad reward. */
@Composable
fun UnlockProgressCard(id:Int,earned:Int,required:Int,hindi:Boolean,modifier:Modifier=Modifier) {
    val finished=earned>=required
    Row(modifier.fillMaxWidth().shadow(12.dp,RoundedCornerShape(22.dp))
        .clip(RoundedCornerShape(22.dp))
        .background(Brush.horizontalGradient(
            if(finished) listOf(Color(0xFF086B51),Aqua)
            else listOf(RoyalDeep,Violet)))
        .padding(horizontal=15.dp,vertical=12.dp),
        verticalAlignment=Alignment.CenterVertically) {
        Text(if(finished)"🎉" else "🎁",fontSize=27.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(if(finished)t(hindi,"Mock %03d unlocked!".format(id),
                "मॉक %03d अनलॉक हुआ!".format(id))
                else t(hindi,"Great! Ad $earned of $required complete",
                    "शानदार! $required में से $earned विज्ञापन पूरे"),
                color=Color.White,fontSize=14.sp,fontWeight=FontWeight.Black)
            Text(if(finished)t(hindi,"You're ready to practice. 🚀",
                "अब अभ्यास शुरू करें! 🚀")
                else t(hindi,"Progress saved on this device.",
                    "प्रगति इसी डिवाइस पर सहेजी गई।"),
                color=Color.White.copy(alpha=.92f),fontSize=11.sp)
        }
        Icon(if(finished)Icons.Rounded.CheckCircle else Icons.Rounded.PlayCircleOutline,
            null,tint=Gold,modifier=Modifier.size(26.dp))
    }
}

@Composable
fun MockPreflightDialog(id:Int,hindi:Boolean,onStart:()->Unit,onDismiss:()->Unit) {
    AlertDialog(
        onDismissRequest=onDismiss,
        containerColor=Color.White,
        shape=RoundedCornerShape(24.dp),
        icon={Text("🎯",fontSize=33.sp)},
        title={
            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                Text(t(hindi,"Ready, Future Officer?","तैयार हैं, भावी अधिकारी?"),
                    color=RoyalDeep,fontWeight=FontWeight.Black,fontSize=20.sp)
                Text("MOCK %03d".format(id),color=Violet,
                    fontWeight=FontWeight.Black,fontSize=12.sp)
            }
        },
        text={
            Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text(t(hindi,"Your focus session starts when you tap Begin.",
                    "शुरू करें दबाने पर आपकी परीक्षा का समय शुरू होगा।"),
                    color=Muted,fontSize=12.sp)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    Tag("📝 150 Q",Violet)
                    Tag("⏱ 135 min",Blue)
                }
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    Tag("✅ +4",Aqua)
                    Tag("❌ −1",Red)
                }
                Text(t(hindi,
                    "A timed attempt will begin. You can save and resume, but the timer keeps running.",
                    "समयबद्ध टेस्ट शुरू होगा। बीच में रुक सकते हैं, लेकिन टाइमर चलता रहेगा।"),
                    color=SoftWhite,fontSize=12.sp,lineHeight=18.sp)
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Rounded.VerifiedUser,null,tint=Aqua,modifier=Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(t(hindi,"No ads during your exam","परीक्षा के दौरान कोई विज्ञापन नहीं"),
                        color=Aqua,fontWeight=FontWeight.Bold,fontSize=11.sp)
                }
            }
        },
        confirmButton={
            Button(onClick=onStart,colors=ButtonDefaults.buttonColors(containerColor=Violet),
                shape=RoundedCornerShape(14.dp)) {
                Text(t(hindi,"🚀 Begin timed mock","🚀 टेस्ट शुरू करें"),
                    fontWeight=FontWeight.Black)
            }
        },
        dismissButton={TextButton(onClick=onDismiss){
            Text(t(hindi,"Not now","अभी नहीं"),color=Muted)
        }}
    )
}
