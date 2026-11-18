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
                 onDone: ()->Unit, onAnalytics: ()->Unit, ads: AdController,
                 previousScore:Int?=null) {
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
        item(key="result-summary") {
            val answered=attempt.correct+attempt.wrong
            val accuracy=if(answered==0) 0 else attempt.correct*100/answered
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(CardInk).border(1.dp,Hairline,RoundedCornerShape(24.dp))
                .padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.spacedBy(9.dp)) {
                Text(t(hindi,"MOCK COMPLETE","मॉक पूरा हुआ"),
                    color=Violet,fontSize=11.sp,fontWeight=FontWeight.Bold,
                    letterSpacing=1.sp)
                Text(t(hindi,"Good work. Keep building your score.",
                    "बढ़िया प्रयास। हर दिन सुधार करें।"),
                    color=SoftWhite,fontWeight=FontWeight.SemiBold,
                    fontSize=17.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight=23.sp)
                ScoreMedallion(attempt.score,size=142.dp)
                if(previousScore!=null) {
                    val delta=attempt.score-previousScore
                    val deltaText=when {
                        delta>0 -> t(hindi,"+$delta marks since last attempt",
                            "पिछले प्रयास से +$delta अंक")
                        delta==0 -> t(hindi,"Same score as last attempt",
                            "पिछले प्रयास के बराबर")
                        else -> t(hindi,"$delta marks since last attempt",
                            "पिछले प्रयास से $delta अंक")
                    }
                    Text(deltaText,color=if(delta>=0) Aqua else Muted,
                        fontWeight=FontWeight.SemiBold,fontSize=12.sp)
                } else {
                    Text(t(hindi,"Your first saved attempt on this mock",
                        "इस मॉक में आपका पहला प्रयास"),
                        color=Muted,fontSize=11.sp)
                }
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    Surface(color=SoftLavender,shape=RoundedCornerShape(20.dp)) {
                        Text("$accuracy% "+t(hindi,"accuracy","सटीकता"),
                            modifier=Modifier.padding(horizontal=12.dp,vertical=8.dp),
                            color=RoyalDeep,fontWeight=FontWeight.SemiBold,fontSize=11.sp)
                    }
                    Surface(color=Ink,shape=RoundedCornerShape(20.dp)) {
                        Text(formatDuration(attempt.elapsedSeconds),
                            modifier=Modifier.padding(horizontal=12.dp,vertical=8.dp),
                            color=SoftWhite,fontWeight=FontWeight.Medium,fontSize=11.sp)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                Panel(Modifier.weight(1f)) { Metric(t(hindi,"Correct","सही"),"${attempt.correct}",Aqua) }
                Panel(Modifier.weight(1f)) { Metric(t(hindi,"Wrong","गलत"),"${attempt.wrong}",Red) }
                Panel(Modifier.weight(1f)) { Metric(t(hindi,"Skipped","छोड़े"),"${attempt.skipped}",Amber) }
            }
        }
        if(ads.canShowBanner()) item(key="post-result-banner") { SponsoredBanner(ads) }
        item { SectionTitle(t(hindi,"📊 Performance Analysis","📊 प्रदर्शन विश्लेषण"),
            t(hindi,"Accuracy is based on answered questions","सटीकता दिए गए उत्तरों पर आधारित है")) }
        item(key="result-subject-donuts") {
            Panel {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
                    subjectScores.forEach{s ->
                        SubjectDonut(s.section,s.accuracy,Modifier.weight(1f))
                    }
                }
            }
        }
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
        itemsIndexed(reviewIndices.toList()) { position,index ->
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
        val options=if(hindi)q.optionsHi else q.optionsEn
        Text(t(hindi,"✅ Correct: ","✅ सही: ")+options[q.correctIndex],
            color=Aqua,fontSize=12.sp,fontWeight=FontWeight.Bold)
        if(selected in 0..3 && selected!=q.correctIndex)
            Text(t(hindi,"Your choice: ","आपका उत्तर: ")+options[selected],
                color=Red,fontSize=12.sp)
        Spacer(Modifier.height(9.dp))
        DividerLine()
        Spacer(Modifier.height(9.dp))
        Text(if(hindi)q.explanationHi else q.explanationEn,color=Muted,fontSize=12.sp,lineHeight=19.sp)
    }
}

@Composable
fun AnalyticsScreen(attempts:List<Attempt>, repo:QuestionRepository, modifier:Modifier, hindi:Boolean,
                    onViewResult:(Attempt)->Unit, ads: AdController) {
    val details=remember(attempts) { Grading.breakdown(attempts,repo) }
    val subject=remember(attempts) { Grading.subjectBreakdown(attempts,repo) }
    LazyColumn(modifier.fillMaxSize().background(Ink),contentPadding=PaddingValues(20.dp),
        verticalArrangement=Arrangement.spacedBy(18.dp)) {
        item(key="analytics-premium-hero") {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp))
                .background(Brush.horizontalGradient(listOf(RoyalDeep,Royal,Violet)))
                .padding(17.dp),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("🧠 "+t(hindi,"SMART INSIGHTS","स्मार्ट विश्लेषण"),
                        color=Gold,fontSize=10.sp,fontWeight=FontWeight.Black)
                    Text(BrandCopy.screen("insights","title",hindi,
                        t(hindi,"Grow Stronger Every Day","हर दिन और बेहतर बनें")),
                        color=Color.White,fontSize=22.sp,lineHeight=27.sp,
                        fontWeight=FontWeight.Black)
                    Text(BrandCopy.screen("insights","subtitle",hindi,
                        t(hindi,"Find the topics that need more practice.",
                            "कमजोर विषयों पर अधिक अभ्यास करें।")),
                        color=Color.White.copy(alpha=.91f),fontSize=11.sp)
                }
                Text("📊",fontSize=38.sp)
            }
        }
        if(ads.canShowNative()) item(key="native-analytics") { SponsoredNative(ads) }
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
            item(key="chart-score-journey") {
                Panel {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text("📈 "+t(hindi,"Score journey","स्कोर की प्रगति"),
                            color=SoftWhite,fontWeight=FontWeight.Black,fontSize=16.sp,
                            modifier=Modifier.weight(1f))
                        Tag("${attempts.size} "+t(hindi,"attempts","प्रयास"),Violet)
                    }
                    Spacer(Modifier.height(9.dp))
                    ScoreTrendChart(PrepMomentum.chronologicalScores(attempts))
                    Spacer(Modifier.height(3.dp))
                    Text(t(hindi,"Earlier → latest · scale −150…600 marks",
                        "पुराने → नए · −150 से 600 अंक का पैमाना"),
                        color=Muted,fontSize=10.sp)
                }
            }
            item(key="real-time-management") {
                val avg=attempts.map{it.elapsedSeconds}.average().toInt()
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
                    .background(SoftMint).padding(13.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text("⏱",fontSize=26.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(t(hindi,"Average mock duration","औसत मॉक अवधि"),
                            color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=12.sp)
                        Text(formatDuration(avg),color=Aqua,
                            fontSize=19.sp,fontWeight=FontWeight.Black)
                    }
                }
            }
            item {SectionTitle(t(hindi,"🎯 Subject accuracy","🎯 विषयवार सटीकता"),
                t(hindi,"Correct / answered across your attempts","आपके प्रयासों में सही / दिए गए उत्तर"))}
            item(key="subject-donut-dashboard") {
                Panel {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
                        subject.forEach{s -> SubjectDonut(s.section,s.accuracy,Modifier.weight(1f))}
                    }
                }
            }
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
            item { SectionTitle("⚡ "+BrandCopy.screen("insights","focus_next",hindi,t(hindi,"Focus Next","अगला फोकस")),t(hindi,"Topics with the lowest accuracy","सबसे कम सटीकता वाले विषय")) }
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
