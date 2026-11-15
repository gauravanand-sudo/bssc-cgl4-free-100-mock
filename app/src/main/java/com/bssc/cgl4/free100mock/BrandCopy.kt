package com.bssc.cgl4.free100mock

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import org.json.JSONArray
import org.json.JSONObject

/** Local bilingual branding copy. Edits in assets/app_copy_en_hi.json are reflected in the installed app. */
object BrandCopy {
    private var root = JSONObject()
    fun initialize(context: Context) {
        root = runCatching { JSONObject(context.assets.open("app_copy_en_hi.json").bufferedReader().use { it.readText() }) }
            .getOrDefault(JSONObject())
    }
    private fun l(hindi: Boolean) = if (hindi) "hi" else "en"
    fun screen(page: String, key: String, hindi: Boolean, fallback: String): String =
        root.optJSONObject("screen")?.optJSONObject(page)?.optJSONObject(l(hindi))?.optString(key)
            ?.takeIf { it.isNotBlank() } ?: fallback
    fun app(key: String, hindi: Boolean, fallback: String): String =
        root.optJSONObject("app")?.optJSONObject(key)?.optString(l(hindi))
            ?.takeIf { it.isNotBlank() } ?: fallback
    fun tagline(hindi: Boolean) = app("tagline", hindi,
        t(hindi,"Future ASO Starts Here","भविष्य के ASO की शुरुआत यहीं से"))
    fun footer(hindi: Boolean) = app("footer",hindi,
        t(hindi,"Made with ❤️ for Bihar","बिहार के लिए ❤️ से बनाया गया"))
    private fun fromArray(array: String, index: Int, hindi: Boolean, fallback: String): String {
        val entries: JSONArray = root.optJSONArray(array) ?: return fallback
        if (entries.length() == 0) return fallback
        return entries.optJSONObject(index.mod(entries.length()))?.optString(l(hindi))
            ?.takeIf { it.isNotBlank() } ?: fallback
    }
    fun motivation(index: Int, hindi: Boolean) = fromArray("motivational_lines",index,hindi,
        t(hindi,"From aspirant to officer — one day at a time.","अभ्यर्थी से अधिकारी — हर दिन एक कदम आगे।"))
    fun loading(index: Int, hindi: Boolean) = fromArray("loading_lines",index,hindi,
        t(hindi,"Preparing your next step toward your goal…","आपके लक्ष्य की ओर अगला कदम तैयार हो रहा है…"))
}

@Composable
fun BiharLoveFooter(hindi: Boolean) {
    Column(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(RoyalDeep,Royal,Color(0xFF551080))))
        .padding(start=14.dp,end=14.dp,top=7.dp,bottom=7.dp),
        horizontalAlignment=Alignment.CenterHorizontally) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text(t(hindi,"Made with ","बिहार के लिए "),fontSize=11.sp,color=Color.White,fontWeight=FontWeight.SemiBold)
            Icon(Icons.Rounded.Favorite,contentDescription="Love",tint=Color(0xFFE84F6D),modifier=Modifier.size(13.dp))
            Text(t(hindi," for Bihar"," से बनाया गया"),fontSize=11.sp,color=Color.White,fontWeight=FontWeight.SemiBold)
        }
        Text(t(hindi,"Independent exam preparation · Not an official BSSC app",
            "स्वतंत्र परीक्षा तैयारी ऐप · आधिकारिक BSSC ऐप नहीं"),
            color=Color.White.copy(alpha=.75f),fontSize=9.sp,lineHeight=12.sp)
    }
}

@Composable
fun OfficerMotivationCard(hindi: Boolean) {
    var line by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while(true) { delay(5300L); line=(line+1)%8 } }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
        .background(Brush.horizontalGradient(listOf(Color(0xFFFFF2D7),Color(0xFFF0EBFF))))
        .padding(17.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Icon(Icons.Rounded.AutoAwesome,null,tint=Amber,modifier=Modifier.size(21.dp))
            Spacer(Modifier.width(8.dp))
            Text(t(hindi,"THE OFFICER MINDSET","अधिकारी बनने का संकल्प"),
                fontSize=10.sp,fontWeight=FontWeight.Black,color=Violet,letterSpacing=.6.sp)
        }
        Spacer(Modifier.height(8.dp))
        AnimatedContent(targetState=line,transitionSpec={
            androidx.compose.animation.fadeIn(tween(380)) togetherWith androidx.compose.animation.fadeOut(tween(230))
        },label="officer-journey-messages") { i ->
            Text(BrandCopy.motivation(i,hindi),color=SoftWhite,fontWeight=FontWeight.Bold,
                fontSize=14.sp,lineHeight=21.sp,modifier=Modifier.fillMaxWidth())
        }
    }
}
