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
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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
    val internalTarget: String = "",
    val imageOnly: Boolean = false
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
    /**
     * Firestore-owned SINGLE Home promotion: highest priority eligible active
     * document. Snapshot listeners update the screen on console changes without
     * APK updates or extra swipes, and are removed when UI is disposed.
     */
    fun observeBanner(callback: (PromoBanner?) -> Unit): () -> Unit {
        var disposed=false
        var listener:ListenerRegistration?=null
        ready({ store ->
            if(!disposed) {
                listener=store.collection("promo_banners")
                    .whereEqualTo("active",true).limit(20)
                    .addSnapshotListener { query,error ->
                        if(!disposed) {
                            if(error!=null || query==null) callback(null)
                            else {
                                val now=System.currentTimeMillis()
                                val banners=query.documents.mapNotNull { d ->
                                    val start=d.getTimestamp("startsAt")?.toDate()?.time ?: 0L
                                    val end=d.getTimestamp("endsAt")?.toDate()?.time ?: Long.MAX_VALUE
                                    val targetUrl=d.getString("targetUrl").orEmpty()
                                    fun localized(field:String,language:String,legacy:String):String {
                                        val map=d.get(field) as? Map<*,*>
                                        return (map?.get(language) as? String).orEmpty()
                                            .ifBlank {d.getString(legacy).orEmpty()}
                                    }
                                    val actionMap=d.get("action") as? Map<*,*>
                                    val actionType=(actionMap?.get("type") as? String
                                        ?: d.getString("action_type")).orEmpty()
                                    val value=(actionMap?.get("value") as? String
                                        ?: d.getString("action_value")).orEmpty()
                                    val internal=if(actionType=="internal" &&
                                        value in setOf("mock_list","insights","ranking","home"))
                                        value else ""
                                    val image=d.getString("imageUrl").orEmpty()
                                    val imageOnly=d.get("imageOnly")==true
                                    if(now !in start..end ||
                                        (internal.isBlank() && !safeHttps(targetUrl)) ||
                                        (imageOnly && !safeHttps(image))) null
                                    else PromoBanner(d.id,
                                        localized("title","en","titleEn"),
                                        localized("title","hi","titleHi"),
                                        localized("subtitle","en","descriptionEn"),
                                        localized("subtitle","hi","descriptionHi"),
                                        localized("cta","en","buttonEn").ifBlank{"Explore"},
                                        localized("cta","hi","buttonHi").ifBlank{"देखें"},
                                        targetUrl,image,(d.getLong("priority")?:0L).toInt(),
                                        internal,imageOnly)
                                }.filter{it.imageOnly || it.titleEn.isNotBlank() || it.titleHi.isNotBlank()}
                                callback(banners.sortedWith(
                                    compareByDescending<PromoBanner>{it.priority}.thenBy{it.id}
                                ).firstOrNull())
                            }
                        }
                    }
            }
        }, {if(!disposed)callback(null)})
        return {disposed=true;listener?.remove()}
    }

    /**
     * Exam date is controlled by exam_events; category cutoffs are now
     * independent home_cards documents. No hardcoded cutoff widget.
     */
    fun observeEvents(callback: (List<ExamEvent>) -> Unit): () -> Unit {
        var disposed=false
        var listener:ListenerRegistration?=null
        ready({store ->
            if(!disposed) {
                listener=store.collection("exam_events")
                    .whereEqualTo("published",true).limit(20)
                    .addSnapshotListener{query,error ->
                        if(!disposed) {
                            if(query==null || error!=null) callback(emptyList())
                            else callback(query.documents.map{d ->
                                val source=d.getString("sourceUrl").orEmpty()
                                val examDate=d.getTimestamp("examAt")?.toDate()?.time
                                val confirmed=d.getBoolean("officialConfirmed")==true &&
                                    source.startsWith("https://bssc.bihar.gov.in/") &&
                                    examDate!=null
                                ExamEvent(d.id,
                                    d.getString("nameEn")?:"BSSC CGL 4 Prelims",
                                    d.getString("nameHi")?:"बीएसएससी सीजीएल 4 प्रारंभिक",
                                    if(confirmed)examDate else null,
                                    confirmed,source)
                            }.sortedWith(compareBy<ExamEvent>{
                                it.scheduledAtMs?:Long.MAX_VALUE
                            }.thenBy{it.id}))
                        }
                    }
            }
        },{if(!disposed)callback(emptyList())})
        return{disposed=true;listener?.remove()}
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
        .padding(start=13.dp,end=13.dp,top=5.dp,bottom=5.dp),verticalAlignment=Alignment.CenterVertically) {
        Image(painterResource(R.drawable.brand_icon),"Independent Bihar exam-preparation logo",
            Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)))
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text("BSSC CGL 4",fontWeight=FontWeight.Black,color=Color.White,fontSize=16.sp, lineHeight=18.sp)
            Text("FREE 100 MOCK · OneShotGS",color=Gold,fontSize=9.sp,fontWeight=FontWeight.Black,
                letterSpacing=.3.sp,maxLines=1)
        }
        if(showNotifications) IconButton(onClick=onNotifications,modifier=Modifier.size(44.dp)) {
            Icon(if(notificationsEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                contentDescription=t(hindi,"Notification settings","नोटिफिकेशन सेटिंग"),
                tint=Gold,modifier=Modifier.size(20.dp))
        }
        Surface(onClick=onLanguage,modifier=Modifier.heightIn(min=42.dp),
            shape=RoundedCornerShape(100.dp),color=Color.White) {
            Row(Modifier.padding(horizontal=10.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(if(hindi) "हिंदी" else "HI",fontWeight=FontWeight.Black,color=if(hindi)Violet else Muted,fontSize=10.sp)
                Text(" | ",color=Muted,fontSize=10.sp)
                Text("EN",fontWeight=FontWeight.Black,color=if(hindi)Muted else Violet,fontSize=10.sp)
                Icon(Icons.Rounded.SwapHoriz,null,tint=Violet,modifier=Modifier.size(13.dp))
            }
        }
    }
}

@Composable
fun ExamCountdown(events:List<ExamEvent>,hindi:Boolean) {
    var now by remember {mutableLongStateOf(System.currentTimeMillis())}
    val confirmed=events.filter {
        it.confirmed && it.scheduledAtMs!=null && it.scheduledAtMs>now
    }.minByOrNull{it.scheduledAtMs?:Long.MAX_VALUE}
    // No placeholder countdown and no guessed date. Firebase alone activates it.
    if(confirmed==null) return
    LaunchedEffect(confirmed.id,confirmed.scheduledAtMs) {
        while(true) {delay(30_000L);now=System.currentTimeMillis()}
    }
    val remaining=(confirmed.scheduledAtMs?:now)-now
    val days=TimeUnit.MILLISECONDS.toDays(remaining).coerceAtLeast(0)
    val hours=TimeUnit.MILLISECONDS.toHours(remaining)%24
    val minutes=TimeUnit.MILLISECONDS.toMinutes(remaining)%60
    val dateText=remember(confirmed.scheduledAtMs) {
        SimpleDateFormat("dd MMM yyyy · hh:mm a",Locale.ENGLISH).apply {
            timeZone=TimeZone.getTimeZone("Asia/Kolkata")
        }.format(Date(confirmed.scheduledAtMs!!))+" IST"
    }
    val context=LocalContext.current
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
        .background(Brush.horizontalGradient(listOf(RoyalDeep,Royal,Violet)))
        .padding(14.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text("⏳",fontSize=25.sp)
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(t(hindi,"OFFICIAL EXAM COUNTDOWN","आधिकारिक परीक्षा काउंटडाउन"),
                    color=Gold,fontWeight=FontWeight.Black,fontSize=10.sp)
                Text(t(hindi,confirmed.nameEn,confirmed.nameHi),
                    color=Color.White,fontWeight=FontWeight.Black,fontSize=14.sp)
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            listOf(days.toString() to t(hindi,"DAYS","दिन"),
                "%02d".format(hours) to t(hindi,"HOURS","घंटे"),
                "%02d".format(minutes) to t(hindi,"MINS","मिनट")
            ).forEach {(value,label)->
                Column(Modifier.weight(1f).clip(RoundedCornerShape(13.dp))
                    .background(Color.White.copy(alpha=.16f))
                    .padding(vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    Text(value,color=Gold,fontWeight=FontWeight.Black,fontSize=22.sp)
                    Text(label,color=Color.White.copy(alpha=.92f),fontSize=10.sp)
                }
            }
        }
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text("📅 $dateText",color=Color.White,fontSize=11.sp,
                fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
            TextButton(onClick={
                if(safeHttps(confirmed.sourceUrl)) {
                    try { context.startActivity(
                        Intent(Intent.ACTION_VIEW,Uri.parse(confirmed.sourceUrl))) }
                    catch (_:Exception) {}
                }
            }) { Text(t(hindi,"Notice ↗","नोटिस ↗"),color=Gold,fontSize=11.sp) }
        }
    }
}

/** Every part of a promotional image or text card opens the configured destination.
 *  Arbitrary public HTTPS links are allowed; no implicit WebView execution.
 */
@Composable
fun FirebasePromotion(banner:PromoBanner,hindi:Boolean,onInternalAction:(String)->Unit={}) {
    val context=LocalContext.current
    val title=t(hindi,banner.titleEn,banner.titleHi.ifBlank {banner.titleEn})
    val description=t(hindi,banner.descriptionEn,
        banner.descriptionHi.ifBlank {banner.descriptionEn})
    val button=t(hindi,banner.buttonEn,banner.buttonHi)
    val imageAvailable=safeHttps(banner.imageUrl)
    val openBanner: () -> Unit = {
        if(banner.internalTarget.isNotBlank()) {
            onInternalAction(banner.internalTarget)
        } else if(safeHttps(banner.url)) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(banner.url)))
            } catch (_:android.content.ActivityNotFoundException) {
                android.widget.Toast.makeText(context,
                    t(hindi,"No app can open this link","लिंक खोलने वाला ऐप नहीं मिला"),
                    android.widget.Toast.LENGTH_SHORT).show()
            } catch (_:SecurityException) {
                android.widget.Toast.makeText(context,
                    t(hindi,"Unable to open this link","लिंक नहीं खुल सकता"),
                    android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    if(banner.imageOnly && imageAvailable) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
            .background(Royal).clickable(onClick=openBanner)) {
            AsyncImage(model=banner.imageUrl,
                contentDescription=title.ifBlank { t(hindi,"Open featured image link",
                    "फीचर्ड तस्वीर का लिंक खोलें") },
                modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
            Row(Modifier.align(Alignment.BottomEnd).padding(10.dp)
                .clip(RoundedCornerShape(10.dp)).background(RoyalDeep.copy(alpha=.93f))
                .padding(horizontal=11.dp,vertical=8.dp),
                verticalAlignment=Alignment.CenterVertically) {
                Text(button,color=Color.White,fontSize=11.sp,fontWeight=FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Rounded.OpenInNew,null,tint=Gold,modifier=Modifier.size(14.dp))
            }
        }
    } else {
        Row(Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(RoyalDeep,Royal,Color(0xFF742CBA))))
            .clickable(onClick=openBanner).padding(16.dp),
            verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(7.dp)) {
                Text(t(hindi,"FEATURED · OPEN LINK","खास अपडेट · लिंक खोलें"),
                    color=Gold,fontSize=9.sp,fontWeight=FontWeight.Black,letterSpacing=.6.sp)
                Text(title,color=Color.White,fontWeight=FontWeight.Black,
                    fontSize=16.sp,lineHeight=20.sp,maxLines=2,
                    overflow=TextOverflow.Ellipsis)
                if(description.isNotBlank()) Text(description,color=Color.White.copy(alpha=.92f),
                    fontSize=11.sp,lineHeight=15.sp,maxLines=2,overflow=TextOverflow.Ellipsis)
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text(button,color=Gold,fontSize=12.sp,fontWeight=FontWeight.Black)
                    Spacer(Modifier.width(5.dp))
                    Icon(Icons.Rounded.OpenInNew,null,tint=Gold,modifier=Modifier.size(15.dp))
                }
            }
            Spacer(Modifier.width(11.dp))
            if(imageAvailable) AsyncImage(model=banner.imageUrl,
                contentDescription=title,
                modifier=Modifier.size(108.dp).clip(RoundedCornerShape(15.dp)),
                contentScale=ContentScale.Crop)
            else Box(Modifier.size(76.dp).clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha=.16f)),
                contentAlignment=Alignment.Center) {
                Icon(Icons.Rounded.Campaign,null,tint=Gold,modifier=Modifier.size(39.dp))
            }
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
