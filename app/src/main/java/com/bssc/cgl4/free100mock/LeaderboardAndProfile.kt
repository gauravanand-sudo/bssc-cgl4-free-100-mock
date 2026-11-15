package com.bssc.cgl4.free100mock

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable fun LeaderboardScreen(board: LeaderboardRepository, tests: List<MockTest>, modifier: Modifier, hindi: Boolean) {
    var selected by remember { mutableIntStateOf(tests.firstOrNull()?.id ?: 1) }
    var rows by remember { mutableStateOf<List<LeaderRow>?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var revision by remember { mutableIntStateOf(0) }
    LaunchedEffect(selected,revision) {
        loading=true;rows=null;message=null
        board.load(selected) { data,error -> rows=data;message=error;loading=false }
    }
    LazyColumn(modifier.fillMaxSize().background(Ink), contentPadding=PaddingValues(20.dp),
        verticalArrangement=Arrangement.spacedBy(18.dp)) {
        item {
            CapsLabel(t(hindi,"COMMUNITY LEADERBOARD","अभ्यर्थियों की रैंकिंग"),Amber)
            Spacer(Modifier.height(7.dp))
            Headline(BrandCopy.screen("ranking","title",hindi,t(hindi,"Learn. Improve. Rise.","सीखें, सुधारें, आगे बढ़ें।")))
            Text(BrandCopy.screen("ranking","subtitle",hindi,t(hindi,"Compare with aspirants who took the same mock","उसी मॉक के अभ्यर्थियों से तुलना करें")),color=Muted,fontSize=12.sp)
        }
        item {
            LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                items(tests) { test ->
                    SelectablePill("Mock %03d".format(test.id),selected==test.id) {selected=test.id}
                }
            }
        }
        item {
            Panel {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Rounded.VerifiedUser,null,tint=Aqua)
                    Spacer(Modifier.width(10.dp))
                    Text(t(hindi,"Rankings appear after connecting Firebase. No fake competitors; choose a nickname.","Firebase जुड़ने पर रैंक दिखेगी। नकली प्रतियोगी नहीं; उपनाम चुनें।"),
                        color=Muted,fontSize=12.sp,lineHeight=18.sp)
                }
            }
        }
        if(loading) item { CircularProgressIndicator(color=Violet) }
        else if(rows==null) item {
            EmptyNotice(t(hindi,"Live ranks are not connected","लाइव रैंकिंग कनेक्ट नहीं है"),message ?: t(hindi,"Connect Firebase to enable live rankings.","लाइव रैंकिंग के लिए Firebase जोड़ें।"))
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick={revision++}) {Text("Try again")}
        }
        else if(rows!!.isEmpty()) item {
            EmptyNotice("Be the first on the leaderboard",
                "Complete Mock $selected and your best score can appear here when Firebase is connected.")
        }
        else {
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(21.dp))
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFF6E7FF),Color.White)))
                    .padding(15.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    Text("🏆",fontSize=45.sp)
                    Text(t(hindi,"Top Aspirants","शीर्ष अभ्यर्थी"),color=Royal,
                        fontWeight=FontWeight.Black,fontSize=19.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),
                        verticalAlignment=Alignment.Bottom) {
                        listOf(1,0,2).forEach {rank ->
                            if(rank<rows!!.size) {
                                val person=rows!![rank]
                                Column(Modifier.weight(1f).clip(RoundedCornerShape(15.dp))
                                    .background(if(rank==0) Color(0xFFFFEFBE) else Color.White)
                                    .padding(vertical=12.dp,horizontal=5.dp),
                                    horizontalAlignment=Alignment.CenterHorizontally) {
                                    Text(listOf("🥇","🥈","🥉")[rank],fontSize=29.sp)
                                    Text(person.name,color=Royal,fontWeight=FontWeight.Bold,
                                        fontSize=10.sp,maxLines=1)
                                    Text("${person.score}/600",color=Violet,fontSize=11.sp,
                                        fontWeight=FontWeight.Black)
                                }
                            } else Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            itemsIndexed(rows!!) { idx,row ->
                Panel {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text(if(idx==0) "🥇" else if(idx==1) "🥈" else if(idx==2) "🥉" else "#${idx+1}",
                            color=if(idx<3) Amber else Muted,fontSize=19.sp,
                            fontWeight=FontWeight.Black,modifier=Modifier.width(46.dp))
                        Column(Modifier.weight(1f)) {
                            Text(row.name,color=SoftWhite,fontSize=14.sp,fontWeight=FontWeight.Bold)
                            Text("${row.correct} correct",color=Muted,fontSize=11.sp)
                        }
                        Text("${row.score}",color=Aqua,fontWeight=FontWeight.Black,fontSize=21.sp)
                        Text(" / 600",color=Muted,fontSize=10.sp)
                    }
                }
            }
            item {Text("Leaderboard scores are client-reported in this MVP and are not anti-cheat verified.",
                color=Muted,fontSize=10.sp)}
        }
        item {Spacer(Modifier.height(15.dp))}
    }
}

@Composable fun ProfileScreen(name:String, hindi:Boolean, attempts:Int, modifier:Modifier,
                               notificationsEnabled:Boolean,onNotifications:()->Unit,
                               onSetName:(String)->Unit,onSetHindi:(Boolean)->Unit,onPrivacy:()->Unit, onDelete:()->Unit) {
    var draft by remember(name) {mutableStateOf(name)}
    var confirmDelete by remember { mutableStateOf(false) }
    val context= LocalContext.current
    Column(modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState())
        .padding(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        CapsLabel("YOUR SPACE",Violet)
        Headline(BrandCopy.screen("profile","title",hindi,t(hindi,"My Preparation","मेरी तैयारी")))
        Panel {
            Text("DISPLAY NAME",color=Muted,fontSize=11.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value=draft,onValueChange={if(it.length<=28) draft=it},singleLine=true,
                modifier=Modifier.fillMaxWidth(),label={Text("Name on rankings")},
                shape=RoundedCornerShape(15.dp))
            Spacer(Modifier.height(11.dp))
            PrimaryAction("Save name",Modifier.fillMaxWidth(),onClick={onSetName(draft)})
            Spacer(Modifier.height(10.dp))
            Text("Please use a nickname. Your name may be public on the leaderboard.",
                color=Muted,fontSize=11.sp,lineHeight=18.sp)
        }
        Panel {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Rounded.Language,null,tint=Aqua)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("हिंदी प्रश्न / Hindi questions",color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=14.sp)
                    Text("Switch question and interface labels",color=Muted,fontSize=11.sp)
                }
                Switch(checked=hindi,onCheckedChange=onSetHindi)
            }
        }
        Panel {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Rounded.NotificationsActive,null,tint=Violet)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(t(hindi,"Exam alerts & new mocks","परीक्षा अपडेट और नए मॉक"),
                        color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=14.sp)
                    Text(t(hindi,"Optional push alerts · no signup","बिना साइनअप के वैकल्पिक सूचना"),color=Muted,fontSize=11.sp)
                }
                Switch(checked=notificationsEnabled,onCheckedChange={onNotifications()})
            }
        }
        Panel {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Rounded.DashboardCustomize,null,tint=Amber)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Your activity",color=SoftWhite,fontWeight=FontWeight.Bold)
                    Text("$attempts mock attempts saved on this device",color=Muted,fontSize=12.sp)
                }
            }
        }
        Panel {
            Text("ADS & PRIVACY",color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=14.sp)
            Spacer(Modifier.height(8.dp))
            Text("Rewarded ads are optional and unlock a selected mock permanently. Consent choices may be changed here where applicable.",
                color=Muted,fontSize=12.sp,lineHeight=19.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick=onPrivacy) { Text("Privacy / ad choices",color=Violet) }
        }
        Panel {
            Text(t(hindi,"About BSSC CGL 4 Free 100 Mock","BSSC CGL 4 Free 100 Mock के बारे में"),color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=14.sp)
            Spacer(Modifier.height(6.dp))
            Text("Independent BSSC CGL 4 practice tool. No registration is required. Not affiliated with or endorsed by the Bihar Staff Selection Commission. Exam details should be verified at the official website.",
                color=Muted,fontSize=12.sp,lineHeight=20.sp)
            Spacer(Modifier.height(10.dp))
            TextButton(onClick={
                context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://bssc.bihar.gov.in/NoticeBoard.htm")))
            }) { Text("Official BSSC notice board ↗",color=Aqua) }
        }
        Panel {
            Text("Delete my data",color=SoftWhite,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text("Delete saved attempts, unlocked mocks and your online leaderboard identity (if configured). Requires connectivity when using rankings.",
                color=Muted,fontSize=12.sp,lineHeight=19.sp)
            Spacer(Modifier.height(9.dp))
            OutlinedButton(onClick={confirmDelete=true}) { Text("Delete app & ranking data",color=Red) }
        }
        Text("BSSC CGL 4 Free 100 Mock · v0.4 · " + BrandCopy.tagline(hindi),color=Muted,fontSize=11.sp)
    }
    if(confirmDelete) AlertDialog(onDismissRequest={confirmDelete=false},containerColor=CardInk,
        title={Text("Permanently delete your data?",color=SoftWhite)},
        text={Text("This removes saved results and mock unlocks. It cannot be undone.",color=Muted)},
        confirmButton={TextButton(onClick={confirmDelete=false;onDelete()}) {Text("Delete data",color=Red)}},
        dismissButton={TextButton(onClick={confirmDelete=false}) {Text("Cancel",color=Violet)}})
}
