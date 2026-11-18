package com.bssc.cgl4.free100mock

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable fun LeaderboardScreen(board: LeaderboardRepository, tests: List<MockTest>, modifier: Modifier, hindi: Boolean, ads: AdController) {
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
        item(key="leaderboard-hero") {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f),
                    verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(t(hindi,"THE PRACTICE BOARD","अभ्यास रैंकिंग"),
                        fontSize=10.sp,fontWeight=FontWeight.Bold,color=Violet,
                        letterSpacing=1.sp)
                    Text(t(hindi,"Leaderboard","रैंकिंग"),
                        fontSize=26.sp,fontWeight=FontWeight.Bold,color=SoftWhite)
                    Text(t(hindi,"Track your progress with fellow learners.",
                        "अन्य छात्रों के साथ अपनी प्रगति देखें।"),
                        fontSize=12.sp,color=Muted,lineHeight=17.sp)
                }
                Box(Modifier.size(58.dp).clip(RoundedCornerShape(19.dp))
                    .background(Color(0xFFFFF4DA)),contentAlignment=Alignment.Center) {
                    Icon(Icons.Rounded.EmojiEvents,contentDescription=null,
                        tint=Amber,modifier=Modifier.size(28.dp))
                }
            }
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
                    Text(t(hindi,"Rankings show your registered name when Firebase is connected. No fake competitors.","Firebase जुड़ने पर पंजीकृत नाम से रैंकिंग दिखेगी। नकली प्रतियोगी नहीं।"),
                        color=Muted,fontSize=12.sp,lineHeight=18.sp)
                }
            }
        }
        if(ads.canShowNative()) item(key="native-leaderboard") { SponsoredNative(ads) }
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
                               student:SignupState.Ready?,onEditContact:()->Unit,onSignOut:()->Unit,
                               notificationsEnabled:Boolean,onNotifications:()->Unit,
                               onSetHindi:(Boolean)->Unit,onPrivacy:()->Unit, onDelete:()->Unit, ads:AdController) {
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }
    val context= LocalContext.current
    Column(modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState())
        .padding(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(CircleShape)
                .background(SoftLavender),
                contentAlignment=Alignment.Center) {
                Text(name.trim().take(1).uppercase().ifBlank { "O" },
                    color=Violet,fontWeight=FontWeight.Bold,fontSize=27.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f),
                verticalArrangement=Arrangement.spacedBy(3.dp)) {
                Text(t(hindi,"STUDENT PROFILE","छात्र प्रोफ़ाइल"),
                    fontSize=10.sp,fontWeight=FontWeight.Bold,
                    letterSpacing=1.sp,color=Violet)
                Text(name,color=SoftWhite,fontWeight=FontWeight.Bold,
                    fontSize=23.sp,maxLines=2)
                Text(t(hindi,"$attempts completed attempts",
                    "$attempts पूरे किए गए प्रयास"),
                    color=Muted,fontSize=12.sp)
            }
        }
        Panel {
            Text(t(hindi,"ACCOUNT DETAILS","खाते का विवरण"),
                color=Muted,fontWeight=FontWeight.Bold,fontSize=10.sp,
                letterSpacing=1.sp)
            Spacer(Modifier.height(9.dp))
            Text(name,color=SoftWhite,fontWeight=FontWeight.SemiBold,fontSize=16.sp)
            Spacer(Modifier.height(8.dp))
            Text(student?.email ?: t(hindi,"Sign in to see your email","ईमेल देखने के लिए लॉगिन करें"),
                color=Muted,fontSize=12.sp)
            Text(student?.phone ?: t(hindi,"No phone saved","मोबाइल नंबर नहीं सहेजा गया"),
                color=SoftWhite,fontSize=13.sp)
            Text(t(hindi,"Phone is self-reported, not OTP-verified.",
                "मोबाइल नंबर OTP से सत्यापित नहीं है।"),color=Muted,fontSize=10.sp)
            Text(t(hindi,"WhatsApp updates: ","WhatsApp अपडेट: ")+
                (if(student?.whatsappOptIn==true) t(hindi,"Opted in","सहमति दी")
                else t(hindi,"Not opted in","सहमति नहीं दी")),
                color=Muted,fontSize=11.sp)
            Spacer(Modifier.height(9.dp))
            OutlinedButton(onClick=onEditContact,enabled=student!=null) {
                Text(t(hindi,"Edit mobile / consent","मोबाइल / सहमति बदलें"),color=Violet)
            }
            TextButton(onClick={confirmSignOut=true},enabled=student!=null) {
                Text(t(hindi,"Sign out","साइन आउट"),color=Violet)
            }
        }
        Panel {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Rounded.Language,null,tint=Aqua)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(t(hindi,"🌐 Switch app language","🌐 ऐप की भाषा बदलें"),
                        color=SoftWhite,fontWeight=FontWeight.Black,fontSize=14.sp)
                    Text("हिंदी ⇄ English · "+t(hindi,"Questions and interface",
                        "प्रश्न और ऐप इंटरफ़ेस"),color=Muted,fontSize=11.sp)
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
                Icon(Icons.Rounded.DashboardCustomize,null,tint=Violet)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(t(hindi,"My activity","मेरी गतिविधि"),color=SoftWhite,
                        fontWeight=FontWeight.SemiBold)
                    Text("$attempts mock attempts saved on this device",color=Muted,fontSize=12.sp)
                }
            }
        }
        Panel {
            Text(t(hindi,"Ads and privacy","विज्ञापन और गोपनीयता"),
                color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=14.sp)
            Spacer(Modifier.height(8.dp))
            Text("Rewarded ads are optional: mocks 3–50 need 1 completed ad, and mocks 51–100 need 2 completed ads. Each credit and unlock is saved on this device. Consent choices can be changed here where applicable.",
                color=Muted,fontSize=12.sp,lineHeight=19.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick=onPrivacy) { Text("Privacy / ad choices",color=Violet) }
        }
        Panel {
            Text(t(hindi,"About OneShotGS","OneShotGS के बारे में"),
                color=SoftWhite,fontWeight=FontWeight.Black,fontSize=17.sp)
            Spacer(Modifier.height(10.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                .background(Color.White).padding(12.dp),
                horizontalAlignment=Alignment.CenterHorizontally) {
                Image(painter=painterResource(R.drawable.bssc_bilingual_logo),
                    contentDescription="Approved OneShotGS BSSC CGL 4 bilingual logo",
                    modifier=Modifier.fillMaxWidth().height(176.dp),
                    contentScale=ContentScale.Fit)
                Spacer(Modifier.height(3.dp))
                Text(t(hindi,"Part of OneShotGS Learning Apps",
                    "OneShotGS लर्निंग ऐप्स का हिस्सा"),
                    color=RoyalDeep,fontSize=13.sp,fontWeight=FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text(t(hindi,"BSSC CGL 4 Free 100 Mock is an independent exam-preparation app within the OneShotGS coaching and learning brand.",
                "BSSC CGL 4 Free 100 Mock, OneShotGS कोचिंग एवं लर्निंग ब्रांड का स्वतंत्र परीक्षा अभ्यास ऐप है।"),
                color=Muted,fontSize=12.sp,lineHeight=19.sp)
            Spacer(Modifier.height(13.dp))
            Row(verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Rounded.SupportAgent,null,tint=Violet,modifier=Modifier.size(24.dp))
                Spacer(Modifier.width(9.dp))
                Text(t(hindi,"Contact & support","संपर्क और सहायता"),
                    color=SoftWhite,fontWeight=FontWeight.Black,fontSize=14.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text("ruhionlabs@gmail.com",color=Violet,
                fontSize=14.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text(t(hindi,"⏱ Resolution target: within 30 minutes",
                "⏱ समाधान का लक्ष्य: 30 मिनट के भीतर"),
                color=Aqua,fontSize=12.sp,fontWeight=FontWeight.Bold)
            Text(t(hindi,
                "Best-effort support target, not a guarantee. Complex issues may take longer.",
                "यह हमारा प्रयास-आधारित लक्ष्य है, गारंटी नहीं। जटिल समस्याओं में अधिक समय लग सकता है।"),
                color=Muted,fontSize=10.sp,lineHeight=15.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick={
                val message=Intent(Intent.ACTION_SENDTO).apply {
                    data=Uri.parse("mailto:ruhionlabs@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT,"BSSC CGL 4 App Support")
                }
                try { context.startActivity(message) }
                catch (_:Exception) {
                    Toast.makeText(context,
                        t(hindi,"No email app found. Please use ruhionlabs@gmail.com.",
                            "ईमेल ऐप नहीं मिला। ruhionlabs@gmail.com पर लिखें।"),
                        Toast.LENGTH_LONG).show()
                }
            },modifier=Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Email,null,modifier=Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(t(hindi,"Email OneShotGS Support","OneShotGS को ईमेल करें"),
                    fontWeight=FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color=Color(0xFFE3DAEC))
            Spacer(Modifier.height(10.dp))
            Text(t(hindi,
                "Independent practice app. Not affiliated with or endorsed by the Bihar Staff Selection Commission or Government of Bihar. Confirm official exam details at BSSC.",
                "स्वतंत्र अभ्यास ऐप। बिहार कर्मचारी चयन आयोग या बिहार सरकार से संबद्ध या समर्थित नहीं। आधिकारिक परीक्षा विवरण BSSC से पुष्टि करें।"),
                color=Muted,fontSize=11.sp,lineHeight=17.sp)
            TextButton(onClick={
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://bssc.bihar.gov.in/NoticeBoard.htm")))
                } catch (_:Exception) {}
            }) { Text(t(hindi,"Official BSSC notice board ↗",
                "आधिकारिक BSSC सूचना पटल ↗"),color=Aqua) }
        }
        Panel {
            Text("🗑️ "+t(hindi,"Delete my data","मेरा डेटा मिटाएँ"),
                color=SoftWhite,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text("Delete saved attempts, unlocked mocks, private registration profile and your Firebase account. Requires connectivity.",
                color=Muted,fontSize=12.sp,lineHeight=19.sp)
            Spacer(Modifier.height(9.dp))
            OutlinedButton(onClick={confirmDelete=true}) { Text("Delete account & app data",color=Red) }
        }
        BiharLoveFooter(hindi)
        Text("BSSC CGL 4 Free 100 Mock · v" + BuildConfig.VERSION_NAME + " · " + BrandCopy.tagline(hindi),
            color=Muted,fontSize=11.sp)
    }
    if(confirmSignOut) AlertDialog(
        onDismissRequest={confirmSignOut=false},containerColor=CardInk,
        title={Text(t(hindi,"Sign out and clear device data?","साइन आउट कर डिवाइस डेटा मिटाएँ?"),color=SoftWhite)},
        text={Text(t(hindi,
            "Mock attempts and unlocks are saved on this device, not synced across accounts. Signing out clears local progress so the next student cannot see it.",
            "मॉक के परिणाम और अनलॉक इस डिवाइस पर सहेजे जाते हैं। साइन आउट पर स्थानीय प्रगति मिटेगी ताकि दूसरे छात्र को यह न दिखे।"),
            color=Muted)},
        confirmButton={TextButton(onClick={confirmSignOut=false;onSignOut()}) {
            Text(t(hindi,"Clear and sign out","डेटा हटाकर साइन आउट"),color=Red)
        }},
        dismissButton={TextButton(onClick={confirmSignOut=false}) {
            Text(t(hindi,"Cancel","रद्द करें"),color=Violet)
        }})
    if(confirmDelete) AlertDialog(onDismissRequest={confirmDelete=false},containerColor=CardInk,
        title={Text("Permanently delete your data?",color=SoftWhite)},
        text={Text("This removes saved results and mock unlocks. It cannot be undone.",color=Muted)},
        confirmButton={TextButton(onClick={confirmDelete=false;onDelete()}) {Text("Delete data",color=Red)}},
        dismissButton={TextButton(onClick={confirmDelete=false}) {Text("Cancel",color=Violet)}})
}
