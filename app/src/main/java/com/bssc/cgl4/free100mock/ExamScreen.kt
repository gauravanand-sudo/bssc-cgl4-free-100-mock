package com.bssc.cgl4.free100mock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ExamScreen(initial: ExamSession, questions: List<Question>, hindi: Boolean,
               onSave: (ExamSession)->Unit, onExit: ()->Unit, onFinish: (ExamSession)->Unit) {
    var session by remember(initial.testId,initial.startedAt) { mutableStateOf(initial) }
    var timeLeft by remember { mutableIntStateOf(ExamConfig.DURATION_SECONDS) }
    var showPalette by remember { mutableStateOf(false) }
    var confirmSubmit by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    fun update(value: ExamSession) { session=value;onSave(value) }
    fun finish() { if(!submitted) { submitted=true;onFinish(session) } }
    LaunchedEffect(initial.startedAt) {
        while(!submitted) {
            timeLeft=((initial.startedAt+ExamConfig.DURATION_SECONDS*1000L-System.currentTimeMillis()+999L)/1000L)
                .toInt().coerceAtLeast(0)
            if(timeLeft<=0) { finish(); break }
            delay(1000L)
        }
    }
    val i=session.currentIndex.coerceIn(0,questions.lastIndex)
    val q=questions[i]
    val choice=session.answers[i]
    val attempted=session.answers.count { it>=0 }
    Scaffold(containerColor=Ink,
        topBar={
            Column(Modifier.background(Brush.horizontalGradient(listOf(RoyalDeep,Royal,Color(0xFF6C27B5))))
                .padding(horizontal=15.dp,vertical=10.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    IconButton(onClick=onExit,modifier=Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.ArrowBack,
                            t(hindi,"Leave exam","परीक्षा छोड़ें"),tint=Color.White)
                    }
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text("📝 "+ExamConfig.title(session.testId),color=Color.White,fontSize=15.sp,
                            fontWeight=FontWeight.Black)
                        Text(t(hindi,"BSSC CGL 4 · FOCUS MODE","BSSC CGL 4 · एकाग्रता मोड"),color=Color.White.copy(alpha=.86f),fontSize=10.sp)
                    }
                    Surface(shape=RoundedCornerShape(12.dp),
                        color=if(timeLeft<600) Color(0xFFFFECEA) else Color.White) {
                        Row(Modifier.padding(horizontal=11.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Timer,null,tint=if(timeLeft<600) Red else Violet,modifier=Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(formatDuration(timeLeft),color=if(timeLeft<600) Red else Violet,
                                fontSize=13.sp,fontWeight=FontWeight.Black)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text("🎯 Q ${i+1} / ${questions.size}",color=Color.White,
                        fontWeight=FontWeight.Black,fontSize=12.sp)
                    Spacer(Modifier.weight(1f))
                    Text(t(hindi,"✅ $attempted answered","✅ $attempted हल किए"),
                        color=Gold,fontWeight=FontWeight.Bold,fontSize=11.sp)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress={attempted.toFloat()/questions.size},
                    modifier=Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(6.dp)),
                    color=Gold,trackColor=Color.White.copy(alpha=.23f),gapSize=0.dp,
                    drawStopIndicator={})
                AnimatedVisibility(visible=timeLeft in 1..599) {
                    Text(t(hindi,"⚠ Less than 10 minutes remaining",
                        "⚠ 10 मिनट से कम समय बचा है"),
                        color=Gold,fontSize=10.sp,fontWeight=FontWeight.Black,
                        modifier=Modifier.padding(top=5.dp))
                }
            }
        },
        bottomBar={
            Column(Modifier.background(CardInk)
                .padding(horizontal=12.dp,vertical=8.dp)) {
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically) {
                    OutlinedButton(onClick={showPalette=true},shape=RoundedCornerShape(14.dp),
                        border=androidx.compose.foundation.BorderStroke(1.dp,Color(0xFFE2E5EE))) {
                        Icon(Icons.Rounded.Apps,null,Modifier.size(18.dp),tint=Muted)
                        Spacer(Modifier.width(6.dp))
                        Text(t(hindi,"Palette","प्रश्न सूची"),color=SoftWhite)
                    }
                    OutlinedButton(onClick={if(i>0)update(session.copy(currentIndex=i-1))},enabled=i>0,
                        shape=RoundedCornerShape(14.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Color(0xFFE2E5EE))) {
                        Icon(Icons.Rounded.ChevronLeft,null,tint=SoftWhite)
                    }
                    PrimaryAction(if(i==questions.lastIndex) BrandCopy.screen("exam","submit",hindi,t(hindi,"Submit Test","टेस्ट जमा करें"))
                        else t(hindi,"Save & Next →","सहेजें और आगे →"),
                        Modifier.weight(1f),onClick={
                            if(i==questions.lastIndex) confirmSubmit=true
                            else update(session.copy(currentIndex=i+1))
                        })
                }
            }
        }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            .padding(horizontal=16.dp,vertical=16.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Tag(q.section,Blue)
                Spacer(Modifier.width(7.dp))
                Tag(q.topic,Violet)
                Spacer(Modifier.weight(1f))
                IconButton(onClick={update(session.copy(marked=if(i in session.marked) session.marked-i else session.marked+i))}) {
                    Icon(if(i in session.marked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        BrandCopy.screen("exam","mark_review",hindi,t(hindi,"Mark for Review","समीक्षा के लिए चिन्हित करें")),tint=if(i in session.marked) Amber else Muted)
                }
            }
            Spacer(Modifier.height(10.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(21.dp))
                .background(Brush.linearGradient(listOf(Color.White,Color(0xFFF1E7FF))))
                .border(1.dp,Violet.copy(alpha=.19f),RoundedCornerShape(21.dp))
                .padding(17.dp)) {
                Text(t(hindi,"QUESTION ${i+1}","प्रश्न ${i+1}"),
                    color=Violet,fontSize=10.sp,fontWeight=FontWeight.Black,letterSpacing=1.sp)
                Spacer(Modifier.height(9.dp))
                AnimatedContent(targetState=i,
                    transitionSpec={fadeIn(tween(180)) togetherWith fadeOut(tween(110))},
                    label="question change") { idx ->
                    Text(if(hindi)questions[idx].questionHi else questions[idx].questionEn,
                        color=SoftWhite,fontSize=18.sp,lineHeight=27.sp,
                        fontWeight=FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(t(hindi,"👇 Choose your answer","👇 अपना उत्तर चुनें"),
                color=Violet,fontWeight=FontWeight.Bold,fontSize=12.sp)
            Spacer(Modifier.height(13.dp))
            val options=if(hindi) q.optionsHi else q.optionsEn
            options.forEachIndexed { index, opt ->
                val selected=choice==index
                val tile by animateColorAsState(if(selected) SoftMint else CardInk,
                    animationSpec=tween(170),label="answer background")
                val outline by animateColorAsState(if(selected) Aqua else Color(0xFFDDCBEF),
                    animationSpec=tween(170),label="answer outline")
                Row(Modifier.fillMaxWidth().padding(bottom=10.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(tile)
                    .border(if(selected) 2.dp else 1.dp,outline,RoundedCornerShape(18.dp))
                    .clickable {
                        val new=session.answers.toMutableList().apply { set(i,index) }
                        update(session.copy(answers=new))
                    }.padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(37.dp).clip(CircleShape)
                        .background(if(selected) Aqua else SoftLavender),
                        contentAlignment=Alignment.Center) {
                        if(selected) Icon(Icons.Rounded.Check,null,
                            tint=Color.White,modifier=Modifier.size(21.dp))
                        else Text(('A'+index).toString(),fontSize=13.sp,fontWeight=FontWeight.Black,
                            color=Royal)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(opt,color=if(selected) SoftWhite else SoftWhite,
                        fontSize=15.sp,lineHeight=22.sp,fontWeight=if(selected) FontWeight.Black else FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment=Alignment.CenterVertically) {
                TextButton(onClick={
                    val new=session.answers.toMutableList().apply { set(i,-1) }
                    update(session.copy(answers=new))
                }) { Text(t(hindi,"Clear response","उत्तर हटाएँ"),color=Muted) }
                Spacer(Modifier.weight(1f))
                TextButton(onClick={confirmSubmit=true}) { Text(t(hindi,"Finish exam","टेस्ट खत्म करें"),color=Amber) }
            }
            Spacer(Modifier.height(60.dp))
        }
    }
    if(showPalette) AlertDialog(onDismissRequest={showPalette=false},containerColor=CardInk,
        title={Text(t(hindi,"🧭 Question navigator","🧭 प्रश्न सूची"),
            color=SoftWhite,fontWeight=FontWeight.Black)},
        text={
            Column {
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Tag("● Answered",Aqua);Tag("● Review",Amber);Tag("● Unseen",Muted)
                }
                Spacer(Modifier.height(15.dp))
                LazyVerticalGrid(GridCells.Adaptive(42.dp),Modifier.height(330.dp),
                    horizontalArrangement=Arrangement.spacedBy(7.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
                    items((0 until questions.size).toList()) { idx ->
                        val color=if(idx in session.marked) Amber else if(session.answers[idx]>=0) Aqua else Muted
                        Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(11.dp))
                            .background(color.copy(alpha=if(idx==i) 0.32f else 0.13f))
                            .border(if(idx==i)2.dp else 1.dp,color.copy(alpha=if(idx==i) 1f else 0.25f),RoundedCornerShape(11.dp))
                            .clickable { update(session.copy(currentIndex=idx));showPalette=false },contentAlignment=Alignment.Center) {
                            Text("${idx+1}",color=color,fontSize=12.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
        },confirmButton={TextButton(onClick={showPalette=false}) {Text("Close",color=Violet)}},
        dismissButton={TextButton(onClick={showPalette=false;confirmSubmit=true}) {Text("Submit",color=Amber)}})
    if(confirmSubmit) AlertDialog(onDismissRequest={confirmSubmit=false},containerColor=CardInk,
        title={Text(t(hindi,"🏁 Submit this mock?","🏁 टेस्ट जमा करें?"),
            color=SoftWhite,fontWeight=FontWeight.Black)},
        text={Text("Answered: $attempted   •   Unanswered: ${questions.size-attempted}\n\nYour paper will be graded using +4 for correct answers and −1 for wrong answers.",color=Muted)},
        confirmButton={TextButton(onClick={confirmSubmit=false;finish()}) {Text("Submit now",color=Aqua,fontWeight=FontWeight.Bold)}},
        dismissButton={TextButton(onClick={confirmSubmit=false}) {Text("Keep solving",color=Violet)}})
}
