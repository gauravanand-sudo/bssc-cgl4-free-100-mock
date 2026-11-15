package com.bssc.cgl4.free100mock

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/** V5 home: hierarchy, colors, cards and navigation follow the two user-supplied phone mockups. */
@Composable
fun DashboardScreen(
    name: String, hindi: Boolean, attempts: List<Attempt>, session: ExamSession?,
    tests: List<MockTest>, catalogStatus: String?, loadingCatalog: Boolean,
    unlocked: Set<Int>, adReady: Boolean, quickReady: Boolean, adAllowed: Boolean, ads: AdController,
    banners: List<PromoBanner>, examEvents: List<ExamEvent>,
    modifier: Modifier, onStart: (Int)->Unit, onUnlock: (Int)->Unit, onQuickUnlock: (Int)->Unit,
    onResume: ()->Unit, onViewResult: (Attempt)->Unit, onToggleLanguage: ()->Unit,
    onRefresh: ()->Unit, onBannerAction: (String)->Unit,
    onOpenTests: ()->Unit={}, onOpenAnalytics: ()->Unit={}, onOpenRanks: ()->Unit={}
) {
    val completed=attempts.map { it.testId }.distinct().size
    val progress by animateFloatAsState(if(tests.isEmpty()) 0f else completed.toFloat()/tests.size,label="mock progress")
    val firstFree=tests.firstOrNull { it.isFree }
    LazyColumn(modifier.fillMaxSize().background(Ink),
        contentPadding=PaddingValues(start=14.dp,end=14.dp,top=12.dp,bottom=22.dp),
        verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item {
            Column {
                Text(t(hindi,"नमस्कार, Future Officer 👋","नमस्कार, भावी अधिकारी 👋"),
                    color=SoftWhite,fontSize=17.sp,fontWeight=FontWeight.Black)
                Text(BrandCopy.tagline(hindi),color=Violet,fontSize=14.sp,fontWeight=FontWeight.ExtraBold)
            }
        }
        item { AnimatedStudyHero(hindi) {
            if(firstFree!=null) onStart(firstFree.id) else onOpenTests()
        } }
        item { ExamCountdown(examEvents,hindi) }
        if(banners.isNotEmpty()) item {
            FirebasePromotion(banners.first(),hindi) { destination ->
                if(destination=="mock_list") onOpenTests() else onBannerAction(destination)
            }
        }
        item {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF630B62),Color(0xFF8C1666),Royal)))
                .clickable {onOpenTests()}.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
                Text("📋",fontSize=31.sp)
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(t(hindi,"BSSC CGL 4 Mock Series","बीएसएससी सीजीएल 4 मॉक सीरीज़"),
                        color=Color.White,fontWeight=FontWeight.Black,fontSize=14.sp)
                    Text(t(hindi,"Up to 100 tests · new papers added online","100 टेस्ट तक · नए पेपर ऑनलाइन जुड़ेंगे"),
                        color=Color.White.copy(alpha=.80f),fontSize=10.sp)
                }
                Text("➜",color=Gold,fontSize=22.sp,fontWeight=FontWeight.Black)
            }
        }
        item {
            Column(verticalArrangement=Arrangement.spacedBy(9.dp)) {
                Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                    ReferenceShortcut("📝",t(hindi,"Mock Tests","मॉक टेस्ट"),Color(0xFFECE1FF),Modifier.weight(1f),onOpenTests)
                    ReferenceShortcut("📚",t(hindi,"Previous Papers","पुराने पेपर"),Color(0xFFFFE8D8),Modifier.weight(1f),onOpenTests)
                    ReferenceShortcut("✅",t(hindi,"Subject Practice","विषय अभ्यास"),Color(0xFFE1F9EE),Modifier.weight(1f),onOpenTests)
                }
                Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                    ReferenceShortcut("📊",t(hindi,"My Results","मेरे परिणाम"),Color(0xFFE2F2FF),Modifier.weight(1f),onOpenAnalytics)
                    ReferenceShortcut("🏆",t(hindi,"Ranking","रैंकिंग"),Color(0xFFFFF0CF),Modifier.weight(1f),onOpenRanks)
                    ReferenceShortcut("🎯",t(hindi,"Weak Areas","कमज़ोर विषय"),Color(0xFFFFE3F0),Modifier.weight(1f),onOpenAnalytics)
                }
            }
        }
        if(session!=null) item {
            Panel(Modifier.clickable(onClick=onResume)) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Restore,null,tint=Violet)
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t(hindi,"Continue your mock test","अपना मॉक जारी रखें"),fontWeight=FontWeight.ExtraBold,color=SoftWhite)
                        Text(ExamConfig.title(session.testId),color=Muted,fontSize=11.sp)
                    }
                    Icon(Icons.Rounded.ArrowForward,null,tint=Violet)
                }
            }
        }
        item {
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                ReferenceMetric("🏅",attempts.maxOfOrNull {it.score}?.toString() ?: "—",
                    t(hindi,"Best / 600","सर्वश्रेष्ठ / 600"),Modifier.weight(1f))
                ReferenceMetric("📈","$completed",t(hindi,"Mocks done","मॉक पूर्ण"),Modifier.weight(1f))
            }
        }
        item {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(t(hindi,"Latest Mock Tests","नवीनतम मॉक टेस्ट"),color=SoftWhite,
                    fontWeight=FontWeight.Black,fontSize=18.sp,modifier=Modifier.weight(1f))
                TextButton(onClick=onOpenTests) { Text(t(hindi,"View all →","सभी देखें →"),color=Violet,fontWeight=FontWeight.Bold) }
            }
            ProgressTrack(progress,Violet)
            Spacer(Modifier.height(4.dp))
            Text(t(hindi,"$completed of ${tests.size} available mocks completed","${tests.size} में से $completed मॉक पूरे किए"),
                color=Muted,fontSize=10.sp)
        }
        items(tests.take(3), key={ it.id }) { test ->
            ReferenceMockRow(test,attempts.firstOrNull {it.testId==test.id},
                !test.isFree && test.id !in unlocked,adReady,quickReady,hindi,
                onStart={onStart(test.id)},onUnlock={onUnlock(test.id)},onQuickUnlock={onQuickUnlock(test.id)})
        }
        if(loadingCatalog) item { LinearProgressIndicator(modifier=Modifier.fillMaxWidth(),color=Violet) }
        if(catalogStatus!=null) item {Text(catalogStatus,color=Muted,fontSize=10.sp)}
        item { OfficerMotivationCard(hindi) }
        if(attempts.isNotEmpty()) item {
            Panel(Modifier.clickable {onViewResult(attempts.first())}) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text("🏆",fontSize=27.sp)
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t(hindi,"Your latest result","आपका पिछला परिणाम"),fontWeight=FontWeight.Bold,color=SoftWhite)
                        Text("${attempts.first().score} / 600",color=Violet,fontWeight=FontWeight.ExtraBold)
                    }
                    Icon(Icons.Rounded.ChevronRight,null,tint=Violet)
                }
            }
        }
        if(adAllowed) item {
            Text("SPONSORED",color=Muted,fontSize=9.sp)
            AndroidView(factory={ads.banner(it)},modifier=Modifier.fillMaxWidth().heightIn(min=55.dp),
                onRelease={it.destroy()})
        }
        item { Text(t(hindi,"Unofficial practice app · Verify exam dates with BSSC","स्वतंत्र अभ्यास ऐप · आधिकारिक तिथि BSSC से जाँचें"),
            fontSize=10.sp,color=Muted) }
    }
}

@Composable
private fun ReferenceShortcut(emoji:String,title:String,color:Color,modifier:Modifier=Modifier,onClick:()->Unit) {
    Column(modifier.height(80.dp).clip(RoundedCornerShape(17.dp)).background(color).clickable(onClick=onClick)
        .padding(horizontal=5.dp,vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.Center) {
        Text(emoji,fontSize=23.sp)
        Spacer(Modifier.height(6.dp))
        Text(title,color=SoftWhite,fontSize=10.sp,lineHeight=12.sp,fontWeight=FontWeight.Black,maxLines=2,
            textAlign=androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun ReferenceMetric(emoji:String,value:String,label:String,modifier:Modifier=Modifier) {
    Row(modifier.clip(RoundedCornerShape(16.dp)).background(CardInk)
        .border(1.dp,Color(0xFFE8DDF3),RoundedCornerShape(16.dp)).padding(12.dp),
        verticalAlignment=Alignment.CenterVertically) {
        Text(emoji,fontSize=24.sp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(value,color=Violet,fontWeight=FontWeight.Black,fontSize=20.sp)
            Text(label,color=Muted,fontSize=10.sp)
        }
    }
}

/** Dedicated compact catalogue with real Firestore data, no fabricated 100-paper listing. */
@Composable
fun TestCatalogueScreen(tests:List<MockTest>,attempts:List<Attempt>,unlocked:Set<Int>,
    adReady:Boolean,quickReady:Boolean,loading:Boolean,status:String?,hindi:Boolean,
    adAllowed:Boolean,ads:AdController,onStart:(Int)->Unit,onUnlock:(Int)->Unit,
    onQuickUnlock:(Int)->Unit,onRefresh:()->Unit) {
    var filter by remember { mutableStateOf("all") }
    val filtered=remember(tests,unlocked,filter) {
        when(filter) {
            "free" -> tests.filter { it.isFree }
            "unlocked" -> tests.filter { it.isFree || it.id in unlocked }
            else -> tests
        }
    }
    LazyColumn(Modifier.fillMaxSize().background(Ink),
        contentPadding=PaddingValues(horizontal=14.dp,vertical=15.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            Text(t(hindi,"Mock Tests","मॉक टेस्ट")+"  (${tests.size})",fontWeight=FontWeight.Black,
                fontSize=22.sp,color=SoftWhite)
            Text(t(hindi,"150 questions · 600 marks · 2h 15m · +4 / −1","150 प्रश्न · 600 अंक · 2 घं 15 मि · +4 / −1"),
                color=Muted,fontSize=11.sp)
        }
        item {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf(Triple("all",t(hindi,"All (${tests.size})","सभी (${tests.size})"),Violet),
                    Triple("free",t(hindi,"Free","मुफ़्त"),Aqua),
                    Triple("unlocked",t(hindi,"Unlocked","अनलॉक"),Violet)).forEach { (key,label,accent) ->
                    Surface(onClick={filter=key},shape=RoundedCornerShape(16.dp),
                        color=if(filter==key) accent else Color.White,
                        border=BorderStroke(1.dp,if(filter==key)accent else Color(0xFFE9DCF5))) {
                        Text(label,Modifier.padding(horizontal=12.dp,vertical=10.dp),
                            color=if(filter==key) Color.White else SoftWhite,fontSize=11.sp,fontWeight=FontWeight.Bold)
                    }
                }
                IconButton(onClick=onRefresh) {Icon(Icons.Rounded.Refresh,"Refresh tests",tint=Violet)}
            }
        }
        if(loading) item { LinearProgressIndicator(modifier=Modifier.fillMaxWidth(),color=Violet) }
        if(status!=null) item {Text(status,color=Muted,fontSize=11.sp)}
        if(filtered.isEmpty()) item {EmptyNotice(t(hindi,"No mocks in this section","यहाँ कोई मॉक नहीं है"),
            t(hindi,"Refresh to check for new published papers.","नए प्रकाशित पेपर के लिए रिफ़्रेश करें।"))}
        itemsIndexed(filtered,key={_,test->test.id}) {index,test ->
            ReferenceMockRow(test,attempts.firstOrNull {it.testId==test.id},
                !test.isFree && test.id !in unlocked,adReady,quickReady,hindi,
                onStart={onStart(test.id)},onUnlock={onUnlock(test.id)},onQuickUnlock={onQuickUnlock(test.id)})
            if(index==4 && adAllowed) {
                Spacer(Modifier.height(12.dp))
                Text("SPONSORED",color=Muted,fontSize=9.sp)
                AndroidView(factory={ads.nativeContainer(it)},modifier=Modifier.fillMaxWidth().heightIn(min=24.dp),
                    onRelease={ads.releaseNative(it)})
            }
        }
        if(adAllowed) item {
            AndroidView(factory={ads.banner(it)},modifier=Modifier.fillMaxWidth().heightIn(min=55.dp),
                onRelease={it.destroy()})
        }
        item {Text(t(hindi,"Only published mocks are shown. More papers will appear when uploaded.",
            "केवल प्रकाशित मॉक दिखते हैं। नए प्रश्नपत्र अपलोड होने पर दिखेंगे।"),fontSize=10.sp,color=Muted)}
    }
}

@Composable
private fun ReferenceMockRow(test:MockTest,previous:Attempt?,locked:Boolean,
    adReady:Boolean,quickReady:Boolean,hindi:Boolean,onStart:()->Unit,onUnlock:()->Unit,onQuickUnlock:()->Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp)).background(Color.White)
        .border(1.dp,Color(0xFFECDFF5),RoundedCornerShape(19.dp)).animateContentSize().padding(13.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                .background(if(locked) Color(0xFFFFE9D9) else SoftLavender),contentAlignment=Alignment.Center) {
                Text(if(locked)"🔒" else "📝",fontSize=20.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(t(hindi,"Full Mock Test","पूर्ण मॉक टेस्ट")+" - %02d".format(test.id),
                    color=SoftWhite,fontSize=14.sp,fontWeight=FontWeight.Black)
                Text("150 Q  •  600 " + t(hindi,"marks","अंक") + "  •  2h 15m",color=Muted,fontSize=10.sp)
            }
            Tag(if(locked)t(hindi,"AD UNLOCK","विज्ञापन") else if(test.isFree)t(hindi,"FREE","मुफ़्त")
                else t(hindi,"READY","तैयार"),if(locked) Amber else Aqua)
        }
        if(previous!=null) {
            Spacer(Modifier.height(7.dp))
            Text(t(hindi,"Your last score: ","पिछला स्कोर: ")+"${previous.score}/600",
                color=Violet,fontWeight=FontWeight.Bold,fontSize=10.sp)
        }
        Spacer(Modifier.height(11.dp))
        if(locked) {
            Button(onClick=onUnlock,enabled=adReady,shape=RoundedCornerShape(14.dp),
                modifier=Modifier.fillMaxWidth().height(43.dp),
                colors=ButtonDefaults.buttonColors(containerColor=Violet)) {
                Icon(Icons.Rounded.PlayCircleOutline,null,modifier=Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text(if(adReady)t(hindi,"Watch Ad to Unlock","विज्ञापन देखकर खोलें")
                    else t(hindi,"Ad loading…","विज्ञापन लोड हो रहा है…"),fontSize=12.sp,fontWeight=FontWeight.Bold)
            }
            if(quickReady) TextButton(onClick=onQuickUnlock,modifier=Modifier.fillMaxWidth()) {
                Text(t(hindi,"Quick rewarded ad option","दूसरा विज्ञापन विकल्प"),fontSize=10.sp,color=Violet)
            }
        } else {
            Button(onClick=onStart,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth().height(43.dp),
                colors=ButtonDefaults.buttonColors(containerColor=Violet)) {
                Text(if(previous!=null)t(hindi,"Retake Mock","मॉक दोबारा दें")
                    else t(hindi,"Start Mock Test","मॉक शुरू करें"),fontSize=12.sp,fontWeight=FontWeight.Black)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Rounded.PlayArrow,null,modifier=Modifier.size(18.dp))
            }
        }
    }
}
