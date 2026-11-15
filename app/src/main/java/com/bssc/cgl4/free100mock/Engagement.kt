package com.bssc.cgl4.free100mock

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

fun t(hindi: Boolean, english: String, hindiText: String) = if (hindi) hindiText else english

/** Managed by Firebase admin; reads require silent anonymous auth. No student client can edit these records. */
data class PromoBanner(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val buttonEn: String,
    val buttonHi: String,
    val url: String,
    val imageUrl: String,
    val priority: Int,
    val internalTarget: String = ""
)
data class ExamEvent(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val scheduledAtMs: Long?,
    val confirmed: Boolean,
    val sourceUrl: String
)

class EngagementRepository(private val context: Context) {
    private var db: FirebaseFirestore? = null
    private fun ready(block: (FirebaseFirestore) -> Unit, onError: () -> Unit) {
        db?.let { block(it); return }
        CloudBackend.withIdentity(context, { store -> db = store; block(store) }, { onError() })
    }
    fun banners(callback: (List<PromoBanner>) -> Unit) {
        ready({ store ->
            store.collection("promo_banners").whereEqualTo("active", true).limit(20).get()
                .addOnSuccessListener { query ->
                    val now=System.currentTimeMillis()
                    callback(query.documents.mapNotNull { d ->
                        val start=d.getTimestamp("startsAt")?.toDate()?.time ?: 0L
                        val end=d.getTimestamp("endsAt")?.toDate()?.time ?: Long.MAX_VALUE
                        val targetUrl=d.getString("targetUrl").orEmpty()
                        // Supports the existing flat admin schema and the nested bilingual schema from the branding pack.
                        fun localized(field:String, language:String, legacy:String):String {
                            val map=d.get(field) as? Map<*,*>
                            return (map?.get(language) as? String).orEmpty().ifBlank { d.getString(legacy).orEmpty() }
                        }
                        val actionMap=d.get("action") as? Map<*,*>
                        val type=(actionMap?.get("type") as? String ?: d.getString("action_type")).orEmpty()
                        val value=(actionMap?.get("value") as? String ?: d.getString("action_value")).orEmpty()
                        val internal=if(type=="internal" && value in setOf("mock_list","insights","ranking","home")) value else ""
                        if(now !in start..end || (internal.isBlank() && !safeHttps(targetUrl))) return@mapNotNull null
                        PromoBanner(d.id, localized("title","en","titleEn"),localized("title","hi","titleHi"),
                            localized("subtitle","en","descriptionEn"),localized("subtitle","hi","descriptionHi"),
                            localized("cta","en","buttonEn").ifBlank { "Explore" },
                            localized("cta","hi","buttonHi").ifBlank { "देखें" },
                            targetUrl,d.getString("imageUrl").orEmpty(),(d.getLong("priority") ?: 0L).toInt(),internal)
                    }.filter { it.titleEn.isNotBlank() || it.titleHi.isNotBlank() }
                        .sortedByDescending { it.priority }.take(5))
                }.addOnFailureListener { callback(emptyList()) }
        }, { callback(emptyList()) })
    }
    fun events(callback: (List<ExamEvent>) -> Unit) {
        ready({ store ->
            store.collection("exam_events").whereEqualTo("published", true).limit(20).get()
                .addOnSuccessListener { query ->
                    callback(query.documents.map { d ->
                        val source=d.getString("sourceUrl").orEmpty()
                        val date=d.getTimestamp("examAt")?.toDate()?.time
                        val confirmed=d.getBoolean("officialConfirmed") == true &&
                            source.startsWith("https://bssc.bihar.gov.in/") && date != null
                        ExamEvent(d.id,d.getString("nameEn") ?: "BSSC CGL 4 examination",
                            d.getString("nameHi") ?: "बीएसएससी सीजीएल 4 परीक्षा",
                            if (confirmed) date else null, confirmed, source)
                    }.sortedWith(compareBy<ExamEvent> { it.scheduledAtMs ?: Long.MAX_VALUE }.thenBy { it.id }))
                }.addOnFailureListener { callback(emptyList()) }
        }, { callback(emptyList()) })
    }
}

fun safeHttps(value: String): Boolean = try {
    val uri = Uri.parse(value)
    uri.scheme.equals("https",ignoreCase=true) && !uri.host.isNullOrBlank() &&
        uri.encodedAuthority?.contains("@") != true && uri.host!!.length <= 253
} catch (_:Exception) { false }

@Composable
fun GlobalExamHeader(hindi: Boolean, onLanguage: () -> Unit, notificationsEnabled:Boolean,
                     showNotifications:Boolean, onNotifications:()->Unit) {
    Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(RoyalDeep,Royal,Color(0xFF641697))))
        .padding(start=15.dp,end=14.dp,top=10.dp,bottom=10.dp),verticalAlignment=Alignment.CenterVertically) {
        Image(painterResource(R.drawable.brand_icon),"Independent Bihar exam-preparation logo",
            Modifier.size(43.dp).clip(RoundedCornerShape(12.dp)))
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text("BSSC CGL 4",fontWeight=FontWeight.Black,color=Color.White,fontSize=16.sp, lineHeight=18.sp)
            Text("FREE 100 MOCK",color=Gold,fontSize=10.sp,fontWeight=FontWeight.Black,
                letterSpacing=.3.sp,maxLines=1)
        }
        if(showNotifications) IconButton(onClick=onNotifications,modifier=Modifier.size(48.dp)) {
            Icon(if(notificationsEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                contentDescription=t(hindi,"Notification settings","नोटिफिकेशन सेटिंग"),
                tint=Gold,modifier=Modifier.size(20.dp))
        }
        Surface(onClick=onLanguage,modifier=Modifier.heightIn(min=44.dp),
            shape=RoundedCornerShape(100.dp),color=Color.White) {
            Row(Modifier.padding(horizontal=11.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(if(hindi) "हिंदी" else "HI",fontWeight=FontWeight.Black,color=if(hindi)Violet else Muted,fontSize=10.sp)
                Text(" | ",color=Muted,fontSize=10.sp)
                Text("EN",fontWeight=FontWeight.Black,color=if(hindi)Muted else Violet,fontSize=10.sp)
                Icon(Icons.Rounded.SwapHoriz,null,tint=Violet,modifier=Modifier.size(13.dp))
            }
        }
    }
}

@Composable
fun AnimatedStudyHero(hindi:Boolean,onStart:()->Unit) {
    val movement=rememberInfiniteTransition(label="officer journey")
    val drift by movement.animateFloat(initialValue=1f,targetValue=1.045f,
        animationSpec=infiniteRepeatable(tween(3500),RepeatMode.Reverse),label="subtle officer artwork movement")
    Box(Modifier.fillMaxWidth().height(194.dp).clip(RoundedCornerShape(20.dp))
        .background(Brush.horizontalGradient(listOf(RoyalDeep,Color(0xFF591596),Color(0xFFA95BD2))))) {
        Image(painterResource(R.drawable.officer_journey),"Aspirant studying and imagined future officer",
            Modifier.fillMaxSize().scale(drift),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(
            RoyalDeep.copy(alpha=.98f),Royal.copy(alpha=.89f),Royal.copy(alpha=.04f)))))
        Column(Modifier.fillMaxHeight().fillMaxWidth(.67f).padding(15.dp),
            verticalArrangement=Arrangement.SpaceBetween) {
            Column {
                Text(t(hindi,"YOUR OFFICER JOURNEY ✦","अधिकारी बनने का सपना ✦"),
                    color=Gold,fontSize=9.sp,fontWeight=FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text(t(hindi,"Future ASO\nStarts Here","भविष्य के ASO\nकी शुरुआत यहाँ"),
                    color=Gold,fontSize=21.sp,lineHeight=25.sp,fontWeight=FontWeight.Black)
                Spacer(Modifier.height(7.dp))
                Text(t(hindi,"Practice Today.\nServe Bihar Tomorrow.","आज मेहनत करें,\nकल बिहार की सेवा करें।"),
                    color=Color.White,fontSize=10.sp,lineHeight=14.sp,fontWeight=FontWeight.SemiBold)
            }
            Surface(onClick=onStart,color=Gold,shape=RoundedCornerShape(40.dp)) {
                Row(Modifier.padding(horizontal=13.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(t(hindi,"Start Mock →","मॉक शुरू करें →"),color=RoyalDeep,fontWeight=FontWeight.Black,fontSize=11.sp)
                }
            }
        }
    }
}

@Composable
fun ExamCountdown(events: List<ExamEvent>,hindi:Boolean) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(events) { while(true) { delay(30_000L); now=System.currentTimeMillis() } }
    val display=events.ifEmpty { listOf(ExamEvent("default","BSSC CGL 4 Prelims","बीएसएससी सीजीएल 4 प्रारंभिक",null,false,"https://bssc.bihar.gov.in/NoticeBoard.htm")) }
    Panel {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Icon(Icons.Rounded.EventAvailable,null,tint=Violet)
            Spacer(Modifier.width(8.dp))
            Text(t(hindi,"Exam countdown","परीक्षा काउंटडाउन"),fontWeight=FontWeight.ExtraBold,color=SoftWhite,fontSize=16.sp,
                modifier=Modifier.weight(1f))
            Text("${events.size} " + t(hindi,"tracked","ट्रैक"),color=Muted,fontSize=10.sp)
        }
        Spacer(Modifier.height(13.dp))
        display.take(3).forEachIndexed { index,event ->
            if(index>0) { HorizontalDivider(color=Color(0xFFEAEFF7)); Spacer(Modifier.height(12.dp)) }
            Text(t(hindi,event.nameEn,event.nameHi),fontSize=12.sp,fontWeight=FontWeight.Bold,color=SoftWhite)
            Spacer(Modifier.height(8.dp))
            val difference = event.scheduledAtMs?.minus(now)
            if(!event.confirmed || difference==null) {
                Text(t(hindi,"Official date awaited · We'll update when confirmed",
                    "आधिकारिक तिथि की प्रतीक्षा · पुष्टि होने पर अपडेट होगा"),fontSize=11.sp,color=Amber)
            } else if(difference <= 0L) {
                Text(t(hindi,"Exam date reached · Check official notices","परीक्षा की तारीख आ गई · आधिकारिक सूचना देखें"),
                    color=Aqua,fontSize=11.sp)
            } else {
                val days=TimeUnit.MILLISECONDS.toDays(difference)
                val hours=TimeUnit.MILLISECONDS.toHours(difference)%24
                val mins=TimeUnit.MILLISECONDS.toMinutes(difference)%60
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    listOf("${days}" to t(hindi,"DAYS","दिन"),"%02d".format(hours) to t(hindi,"HRS","घंटे"),
                        "%02d".format(mins) to t(hindi,"MIN","मिनट")).forEach { (value,caption) ->
                        Column(Modifier.clip(RoundedCornerShape(12.dp)).background(SoftLavender)
                            .padding(horizontal=13.dp,vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                            Text(value,fontWeight=FontWeight.Black,color=Violet,fontSize=21.sp)
                            Text(caption,color=Muted,fontSize=9.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(9.dp))
        Text(t(hindi,"Dates are shown only when verified against BSSC notices.",
            "तिथियाँ केवल बीएसएससी नोटिस से पुष्टि के बाद दिखाई जाती हैं।"),fontSize=10.sp,color=Muted)
    }
}

@Composable
fun FirebasePromotion(banner: PromoBanner,hindi:Boolean,onInternalAction:(String)->Unit={}) {
    val context=LocalContext.current
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
        .background(Brush.linearGradient(listOf(Color(0xFFFFF1D8),Color(0xFFFFF8EB))))
        .clickable {
            if(banner.internalTarget.isNotBlank()) onInternalAction(banner.internalTarget)
            else if(safeHttps(banner.url)) context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(banner.url)))
        }
        .padding(17.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(t(hindi,"FEATURED · APP UPDATE","खास अपडेट"),color=Color(0xFFAF751B),
                    fontSize=9.sp,letterSpacing=1.sp,fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(t(hindi,banner.titleEn,banner.titleHi.ifBlank {banner.titleEn}),color=SoftWhite,
                    fontSize=16.sp,fontWeight=FontWeight.Black,maxLines=2,overflow=TextOverflow.Ellipsis)
                Text(t(hindi,banner.descriptionEn,banner.descriptionHi.ifBlank {banner.descriptionEn}),
                    color=Muted,fontSize=11.sp,lineHeight=15.sp,maxLines=2)
                Spacer(Modifier.height(9.dp))
                Text(t(hindi,banner.buttonEn,banner.buttonHi)+"  ↗",color=Color(0xFF8A4F0B),
                    fontSize=11.sp,fontWeight=FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(9.dp))
            if (safeHttps(banner.imageUrl)) AsyncImage(model=banner.imageUrl,contentDescription=null,
                modifier=Modifier.size(78.dp).clip(RoundedCornerShape(16.dp)),contentScale=ContentScale.Crop)
            else Icon(Icons.Rounded.Campaign,null,tint=Amber,modifier=Modifier.size(53.dp))
        }
    }
}

@Composable
fun MotivationalLoading(hindi:Boolean,description:String=t(hindi,"Preparing your mock...","मॉक तैयार हो रहा है...")) {
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while(true) { delay(2800L); index=(index+1)%3 } }
    val pulse=rememberInfiniteTransition(label="loading pulse")
    val scale by pulse.animateFloat(initialValue=.92f,targetValue=1.06f,
        animationSpec=infiniteRepeatable(tween(1300),RepeatMode.Reverse),label="pulse")
    Column(Modifier.fillMaxWidth().padding(7.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.officer_journey),"Motivating study illustration",
            Modifier.size(145.dp).scale(scale).clip(RoundedCornerShape(26.dp)),contentScale=ContentScale.Crop)
        Spacer(Modifier.height(15.dp))
        Text(description,color=SoftWhite,fontWeight=FontWeight.Black,fontSize=15.sp)
        Spacer(Modifier.height(10.dp))
        AnimatedContent(targetState=index,label="study quote") { i ->
            Text(BrandCopy.loading(i,hindi),color=Violet,fontSize=12.sp,lineHeight=18.sp)
        }
        Spacer(Modifier.height(18.dp))
        LinearProgressIndicator(modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),color=Violet)
    }
}
