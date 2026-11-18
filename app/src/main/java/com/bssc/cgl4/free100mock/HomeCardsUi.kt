package com.bssc.cgl4.free100mock

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

/** Fully editable sections with horizontal carousels or stacked full-width cards.
 * Empty Firestore → no placeholder or hardcoded Cutoff section.
 */
@Composable
fun HomeCardSections(cards:List<HomeCard>,placement:String,hindi:Boolean,
                     onInternalAction:(String)->Unit) {
    if(cards.isEmpty()) return
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(cards) {
        while(true) { delay(60_000L); now=System.currentTimeMillis() }
    }
    val sections=remember(cards,now,placement) {
        cards.filter{it.placement==placement && it.visibleAt(now)}
            .groupBy{it.sectionId}
            .values.sortedWith(compareBy<List<HomeCard>>(
                {it.minOf{card->card.sectionOrder}},
                {it.first().sectionId}))
    }
    if(sections.isEmpty()) return
    Column(verticalArrangement=Arrangement.spacedBy(15.dp)) {
        sections.forEach {group ->
            val heading=t(hindi,group.first().sectionTitleEn,
                group.first().sectionTitleHi.ifBlank{group.first().sectionTitleEn})
            val layout=group.first().layout
            Column(verticalArrangement=Arrangement.spacedBy(9.dp)) {
                if(heading.isNotBlank()) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text(heading,color=SoftWhite,fontWeight=FontWeight.Black,
                            fontSize=17.sp,modifier=Modifier.weight(1f),
                            maxLines=2,overflow=TextOverflow.Ellipsis)
                        if(layout=="horizontal" && group.size>1)
                            Text(t(hindi,"Swipe →","स्वाइप →"),color=Violet,
                                fontSize=10.sp,fontWeight=FontWeight.Bold)
                    }
                }
                if(layout=="horizontal") {
                    LazyRow(horizontalArrangement=Arrangement.spacedBy(10.dp),
                        contentPadding=PaddingValues(end=14.dp)) {
                        items(group,key={it.id}) {card ->
                            FlexibleHomeCard(card,hindi,Modifier.width(236.dp),onInternalAction)
                        }
                    }
                } else {
                    group.forEach {card ->
                        key(card.id) {
                            FlexibleHomeCard(card,hindi,Modifier.fillMaxWidth(),onInternalAction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlexibleHomeCard(card:HomeCard,hindi:Boolean,modifier:Modifier,
                             onInternalAction:(String)->Unit) {
    val context=LocalContext.current
    // Firebase still controls the theme, but only as an accent; avoiding
    // unrelated full-card gradients keeps the feed visually consistent.
    val base=CardInk
    val secondary=CardInk
    val foreground=SoftWhite
    val secondaryText=Muted
    val accent=when(card.theme) {
        "blue" -> Blue
        "mint" -> Aqua
        "gold" -> Amber
        "sunset" -> Red
        else -> Violet
    }
    val heading=t(hindi,card.titleEn,card.titleHi.ifBlank{card.titleEn})
    val body=t(hindi,card.bodyEn,card.bodyHi.ifBlank{card.bodyEn})
    val badge=t(hindi,card.badgeEn,card.badgeHi.ifBlank{card.badgeEn})
    val button=t(hindi,card.buttonEn,card.buttonHi.ifBlank{card.buttonEn})
    val cutoff=card.type=="cutoff"
    val official=cutoff && card.claim=="official"
    val marker=if(cutoff) {
        if(official) t(hindi,"OFFICIAL · BSSC SOURCE","आधिकारिक · BSSC स्रोत")
        else t(hindi,"EXPECTED · NOT OFFICIAL","अनुमानित · आधिकारिक नहीं")
    } else badge
    val effectiveUrl=if(cutoff && safeHttps(card.sourceUrl))card.sourceUrl
        else if(card.actionType=="external")card.actionValue else ""
    val canOpen=(card.actionType=="internal" &&
        card.actionValue in HomeCardPolicy.routes) || safeHttps(effectiveUrl)
    val openAction:()->Unit={
        if(cutoff && safeHttps(card.sourceUrl)) {
            try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(card.sourceUrl)))}
            catch (_:Exception) {
                Toast.makeText(context,t(hindi,"Cannot open source","स्रोत नहीं खुला"),
                    Toast.LENGTH_SHORT).show()
            }
        } else if(card.actionType=="internal") onInternalAction(card.actionValue)
        else if(safeHttps(effectiveUrl)) {
            try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(effectiveUrl)))}
            catch (_:Exception) {
                Toast.makeText(context,t(hindi,"No app to open this link","लिंक खोलने वाला ऐप नहीं मिला"),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    val shape=RoundedCornerShape(22.dp)
    Column(modifier.clip(shape)
        .background(base)
        .border(1.dp,Hairline,shape)
        .then(if(canOpen) Modifier.clickable(onClick=openAction) else Modifier)
        .padding(13.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        if(card.type=="image" && safeHttps(card.imageUrl)) {
            AsyncImage(model=card.imageUrl,contentDescription=heading,
                modifier=Modifier.fillMaxWidth().height(106.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale=ContentScale.Crop)
        }
        Row(verticalAlignment=Alignment.CenterVertically) {
            if(card.iconEmoji.isNotBlank()) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp))
                    .background(foreground.copy(alpha=.10f)),
                    contentAlignment=Alignment.Center) {
                    Text(card.iconEmoji,fontSize=23.sp,maxLines=1)
                }
            } else {
                val vector=when(card.iconName) {
                    "calendar" -> Icons.Rounded.Event
                    "school" -> Icons.Rounded.School
                    "book" -> Icons.Rounded.MenuBook
                    "target" -> Icons.Rounded.TrackChanges
                    "chart" -> Icons.Rounded.Insights
                    "trophy" -> Icons.Rounded.EmojiEvents
                    "news" -> Icons.Rounded.Campaign
                    "gift" -> Icons.Rounded.CardGiftcard
                    "link" -> Icons.Rounded.OpenInNew
                    "mail" -> Icons.Rounded.Email
                    else -> Icons.Rounded.AutoAwesome
                }
                Icon(vector,contentDescription=null,tint=accent,
                    modifier=Modifier.size(29.dp))
            }
            Spacer(Modifier.weight(1f))
            if(marker.isNotBlank()) Text(marker,
                modifier=Modifier.clip(RoundedCornerShape(100.dp))
                    .background(foreground.copy(alpha=.12f))
                    .padding(horizontal=9.dp,vertical=5.dp),
                color=foreground,fontWeight=FontWeight.Black,
                fontSize=9.sp,maxLines=2)
        }
        if(heading.isNotBlank()) Text(heading,color=foreground,
            fontSize=15.sp,fontWeight=FontWeight.SemiBold,lineHeight=21.sp,
            maxLines=2,overflow=TextOverflow.Ellipsis)
        if(card.value.isNotBlank()) Text(card.value,color=accent,
            fontWeight=FontWeight.Bold,fontSize=25.sp,maxLines=1)
        if(body.isNotBlank()) Text(body,color=secondaryText,
            fontSize=11.sp,lineHeight=16.sp,
            maxLines=if(card.layout=="stacked")5 else 3,
            overflow=TextOverflow.Ellipsis)
        if(card.type!="image" && safeHttps(card.imageUrl)) {
            AsyncImage(model=card.imageUrl,contentDescription=heading,
                modifier=Modifier.fillMaxWidth().height(84.dp)
                    .clip(RoundedCornerShape(11.dp)),contentScale=ContentScale.Crop)
        }
        if(canOpen) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(if(cutoff && safeHttps(card.sourceUrl))
                    t(hindi,"View source","स्रोत देखें")
                    else button.ifBlank{t(hindi,"Open link","लिंक खोलें")},
                    color=accent,fontSize=11.sp,fontWeight=FontWeight.Black,
                    maxLines=1,overflow=TextOverflow.Ellipsis)
                Spacer(Modifier.width(5.dp))
                Icon(Icons.Rounded.ArrowForwardIos,null,tint=accent,
                    modifier=Modifier.size(11.dp))
            }
        }
    }
}
