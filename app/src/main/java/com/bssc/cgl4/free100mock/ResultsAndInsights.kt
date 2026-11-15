package com.bssc.cgl4.free100mock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun ResultScreen(attempt: Attempt, questions: List<Question>, hindi: Boolean,
                 onDone: ()->Unit, onAnalytics: ()->Unit) {
    var wrongOnly by remember { mutableStateOf(true) }
    val subjectScores=ExamConfig.subjects.map { section ->
        val indices=questions.indices.filter { questions[it].section==section }
        val correct=indices.count { attempt.answers[it]==questions[it].correctIndex }
        val wrong=indices.count { attempt.answers[it]>=0 && attempt.answers[it]!=questions[it].correctIndex }
        val skipped=indices.size-correct-wrong
        TopicPerformance(section,section,correct,wrong,skipped)
    }
    val reviewIndices=questions.indices.filter { !wrongOnly || attempt.answers[it]!=questions[it].correctIndex }
    LazyColumn(Modifier.fillMaxSize().background(Ink),contentPadding=PaddingValues(20.dp),
        verticalArrangement=Arrangement.spacedBy(18.dp)) {
        item {
            Row(verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick=onDone) {Icon(Icons.Rounded.Close,null,tint=SoftWhite)}
                Column(Modifier.weight(1f)) {
                    CapsLabel(t(hindi,"PAPER SUBMITTED","टेस्ट जमा हुआ"),Aqua)
                    Text(ExamConfig.title(attempt.testId),color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=18.sp)
                }
                Tag(t(hindi,"RESULT","परिणाम"),Aqua)
            }
        }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF441071),Color(0xFF8131C5),Color(0xFFF7DFFF))))
                .padding(horizontal=18.dp,vertical=20.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                Text("🏆",fontSize=59.sp)
                Text(t(hindi,"Great Attempt! ✨","शानदार प्रयास! ✨"),color=Gold,
                    fontWeight=FontWeight.Black,fontSize=23.sp)
                Text(t(hindi,"Every mock brings you closer to your officer dream.",
                    "हर मॉक से अधिकारी बनने का सपना करीब आता है।"),
                    color=Color.White.copy(alpha=.92f),fontSize=11.sp,lineHeight=16.sp)
                Spacer(Modifier.height(13.dp))
                Box(Modifier.size(134.dp).clip(CircleShape).background(Color.White),
                    contentAlignment=Alignment.Center) {
                    CircularProgressIndicator(progress={ (attempt.score.coerceAtLeast(0) / 600f).coerceIn(0f,1f) },
                        modifier=Modifier.fillMaxSize().padding(8.dp),color=Aqua,
                        trackColor=Color(0xFFEAE9F1),strokeWidth=9.dp)
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(t(hindi,"Your Score","आपका स्कोर"),color=Muted,fontSize=10.sp,fontWeight=FontWeight.Bold)
                        Text("${attempt.score}",color=Royal,fontSize=37.sp,fontWeight=FontWeight.Black)
                        Text("/ 600",color=Muted,fontSize=12.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(t(hindi,"Completed in ","समय: ")+formatDuration(attempt.elapsedSeconds),
                    color=Color.White,fontSize=11.sp,fontWeight=FontWeight.SemiBold)
            }
        }
        item {
            Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                Panel(Modifier.weight(1f)) { Metric(t(hindi,"Correct","सही"),"${attempt.correct}",Aqua) }
                Panel(Modifier.weight(1f)) { Metric(t(hindi,"Wrong","गलत"),"${attempt.wrong}",Red) }
                Panel(Modifier.weight(1f)) { Metric(t(hindi,"Skipped","छोड़े"),"${attempt.skipped}",Amber) }
            }
        }
        item { SectionTitle(t(hindi,"Performance Analysis","प्रदर्शन विश्लेषण"),t(hindi,"Accuracy is based on answered questions","सटीकता दिए गए उत्तरों पर आधारित है")) }
        subjectScores.forEach { s -> item {
            Panel {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text(s.topic,color=SoftWhite,fontSize=14.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
                    Text(if(s.attempted==0) "—" else "${(s.accuracy*100).toInt()}%",color=Violet,fontWeight=FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                ProgressTrack(s.accuracy,if(s.accuracy>=.7f) Aqua else if(s.accuracy>=.4f) Amber else Red)
                Spacer(Modifier.height(7.dp))
                Text("${s.correct} correct · ${s.wrong} wrong · ${s.skipped} skipped",color=Muted,fontSize=11.sp)
            }
        } }
        item {
            PrimaryAction(BrandCopy.screen("results","weak_areas",hindi,t(hindi,"Areas to Improve","सुधार के विषय")) + " →",
                Modifier.fillMaxWidth(),onClick=onAnalytics)
        }
        item {
            SectionTitle(t(hindi,"Answer review","उत्तर समीक्षा"),t(hindi,"Understand each mistake with explanations","व्याख्या के साथ गलतियों को समझें"))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                SelectablePill("Incorrect & skipped",wrongOnly) {wrongOnly=true}
                SelectablePill("All 150",!wrongOnly) {wrongOnly=false}
            }
        }
        itemsIndexed(reviewIndices.toList()) { _,index ->
            ReviewQuestion(index,questions[index],attempt.answers[index],hindi)
        }
        item { Spacer(Modifier.height(28.dp)) }
    }
}

@Composable private fun ReviewQuestion(index:Int,q:Question,selected:Int,hindi:Boolean) {
    Panel {
        Row(verticalAlignment=Alignment.CenterVertically) {
            CapsLabel("QUESTION ${index+1}")
            Spacer(Modifier.weight(1f))
            Tag(if(selected<0) "SKIPPED" else if(selected==q.correctIndex) "CORRECT" else "INCORRECT",
                if(selected==q.correctIndex) Aqua else if(selected<0) Amber else Red)
        }
        Spacer(Modifier.height(10.dp))
        Text(if(hindi) q.questionHi else q.questionEn,color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=14.sp,lineHeight=22.sp)
        Spacer(Modifier.height(12.dp))
        Text("Correct: ${q.optionsEn[q.correctIndex]}",color=Aqua,fontSize=12.sp,fontWeight=FontWeight.SemiBold)
        if(selected>=0 && selected!=q.correctIndex)
            Text("Your choice: ${q.optionsEn[selected]}",color=Red,fontSize=12.sp)
        Spacer(Modifier.height(9.dp))
        DividerLine()
        Spacer(Modifier.height(9.dp))
        Text(if(hindi)q.explanationHi else q.explanationEn,color=Muted,fontSize=12.sp,lineHeight=19.sp)
    }
}

@Composable
fun AnalyticsScreen(attempts:List<Attempt>, repo:QuestionRepository, modifier:Modifier, hindi:Boolean,
                    onViewResult:(Attempt)->Unit) {
    val details=remember(attempts) { Grading.breakdown(attempts,repo) }
    val subject=remember(attempts) { Grading.subjectBreakdown(attempts,repo) }
    LazyColumn(modifier.fillMaxSize().background(Ink),contentPadding=PaddingValues(20.dp),
        verticalArrangement=Arrangement.spacedBy(18.dp)) {
        item {
            CapsLabel(t(hindi,"PERSONAL PERFORMANCE","व्यक्तिगत प्रदर्शन"),Violet)
            Spacer(Modifier.height(7.dp))
            Headline(BrandCopy.screen("insights","title",hindi,t(hindi,"Grow Stronger Every Day","हर दिन और बेहतर बनें")))
            Text(BrandCopy.screen("insights","subtitle",hindi,t(hindi,"Find the topics that need more practice.","कमजोर विषयों पर अधिक अभ्यास करें।")),color=Muted,fontSize=13.sp)
        }
        if(attempts.isEmpty()) {
            item {EmptyNotice(t(hindi,"Insights unlock after your first mock","पहले मॉक के बाद विश्लेषण मिलेगा"),
                t(hindi,"Complete a free paper to unlock progress and weak-topic insights.",
                    "अपनी प्रगति और कमजोर विषय देखने के लिए एक मॉक पूरा करें।")) }
        } else {
            item {
                val best=attempts.maxOf {it.score}
                val avg=attempts.map {it.score}.average().toInt()
                Row(horizontalArrangement=Arrangement.spacedBy(11.dp)) {
                    Panel(Modifier.weight(1f)) { Metric(t(hindi,"Best score","सर्वश्रेष्ठ स्कोर"),"$best",Aqua) }
                    Panel(Modifier.weight(1f)) { Metric(t(hindi,"Average score","औसत स्कोर"),"$avg",Violet) }
                }
            }
            item {SectionTitle(t(hindi,"Subject accuracy","विषयवार सटीकता"),t(hindi,"Right answers / attempted answers","सही उत्तर / दिए गए उत्तर"))}
            subject.forEach { s -> item {
                Panel {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text(s.section,color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=14.sp,modifier=Modifier.weight(1f))
                        Text(if(s.attempted>0) "${(s.accuracy*100).toInt()}%" else "—",color=Violet,fontWeight=FontWeight.Bold)
                    }
                    Spacer(Modifier.height(9.dp))
                    ProgressTrack(s.accuracy,if(s.accuracy>=.7f) Aqua else Amber)
                    Spacer(Modifier.height(7.dp))
                    Text("${s.correct} correct · ${s.wrong} incorrect · ${s.skipped} not attempted",color=Muted,fontSize=11.sp)
                }
            } }
            item { SectionTitle(BrandCopy.screen("insights","focus_next",hindi,t(hindi,"Focus Next","अगला फोकस")),t(hindi,"Topics with the lowest accuracy","सबसे कम सटीकता वाले विषय")) }
            val weakest=details.filter { it.attempted>0 }.take(6)
            if(weakest.isEmpty()) item { EmptyNotice("No attempted topics yet","Try answering questions in a mock to receive personalized recommendations.") }
            else weakest.forEachIndexed { index, s -> item {
                Panel {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Box(Modifier.size(37.dp).clip(RoundedCornerShape(13.dp))
                            .background(Red.copy(alpha=.13f)),contentAlignment=Alignment.Center) {
                            Text("${index+1}",color=Red,fontWeight=FontWeight.Black)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.topic,color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=14.sp)
                            Text(s.section,color=Muted,fontSize=11.sp)
                        }
                        Text("${(100*s.accuracy).toInt()}%",color=if(s.accuracy<.5f) Red else Amber,
                            fontWeight=FontWeight.Black)
                    }
                    Spacer(Modifier.height(9.dp))
                    Text(t(hindi,"Revise this chapter, then retake a full mock.","यह अध्याय पढ़ें और फिर मॉक टेस्ट दें।"),color=Muted,fontSize=11.sp)
                }
            } }
            item { SectionTitle(t(hindi,"Score history","स्कोर इतिहास"),t(hindi,"Your most recent attempts","हाल के मॉक")) }
            item {
                Panel {
                    val recent=attempts.take(8).reversed()
                    Row(Modifier.fillMaxWidth().height(128.dp),verticalAlignment=Alignment.Bottom,
                        horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        recent.forEachIndexed { index,a ->
                            val height=(a.score.coerceAtLeast(0)/600f).coerceAtLeast(.045f)
                            Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) {
                                Text("${a.score}",color=Muted,fontSize=10.sp)
                                Spacer(Modifier.height(5.dp))
                                Box(Modifier.fillMaxWidth().height((95*height).dp)
                                    .clip(RoundedCornerShape(topStart=7.dp,topEnd=7.dp))
                                    .background(if(index==recent.lastIndex) Aqua else Violet.copy(alpha=.65f)))
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Earlier attempts → latest attempts",color=Muted,fontSize=10.sp)
                }
            }
            item {SectionTitle(t(hindi,"Attempt history","पिछले प्रयास"))}
            itemsIndexed(attempts) { _,a ->
                Panel(Modifier.clickable {onViewResult(a)}) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ExamConfig.title(a.testId),color=SoftWhite,fontWeight=FontWeight.Bold)
                            Text(formatDate(a.completedAt),color=Muted,fontSize=11.sp)
                        }
                        Text("${a.score} / 600",color=Aqua,fontWeight=FontWeight.ExtraBold)
                        Icon(Icons.Rounded.ChevronRight,null,tint=Muted)
                    }
                }
            }
        }
        item {Spacer(Modifier.height(16.dp))}
    }
}
