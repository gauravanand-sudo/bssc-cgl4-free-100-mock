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
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp))
                .background(androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(RoyalDeep,Royal,Color(0xFF7C2EBB))))
                .padding(17.dp),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t(hindi,"🏆 PRACTICE LEADERBOARD","🏆 अभ्यास रैंकिंग"),
                        color=Gold,fontWeight=FontWeight.Black,fontSize=10.sp)
                    Text(BrandCopy.screen("ranking","title",hindi,
                        t(hindi,"Learn. Improve. Rise.","सीखें, सुधारें, आगे बढ़ें।")),
                        color=Color.White,fontWeight=FontWeight.Black,fontSize=22.sp,lineHeight=27.sp)
                    Text(t(hindi,"Compare with aspirants on the same mock.",
                        "उसी मॉक के अभ्यर्थियों से तुलना करें।"),
                        color=Color.White.copy(alpha=.91f),fontSize=11.sp)
                }
                Text("🥇",fontSize=40.sp)
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
                               student:SignupState.Ready?,onEditContact:()->Unit,onSignOut:()->Unit,
                               notificationsEnabled:Boolean,onNotifications:()->Unit,
                               onSetName:(String)->Unit,onSetHindi:(Boolean)->Unit,onPrivacy:()->Unit, onDelete:()->Unit, ads:AdController) {
    var draft by remember(name) {mutableStateOf(name)}
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }
    val context= LocalContext.current
    Column(modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState())
        .padding(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(androidx.compose.ui.graphics.Brush.horizontalGradient(
                listOf(RoyalDeep,Royal,Color(0xFF7432AD))))
            .padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
            Text("👨‍🎓",fontSize=36.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("✨ "+t(hindi,"YOUR SPACE","आपकी प्रोफ़ाइल"),
                    color=Gold,fontSize=10.sp,fontWeight=FontWeight.Black)
                Text(BrandCopy.screen("profile","title",hindi,
                    t(hindi,"My Preparation","मेरी तैयारी")),
                    color=Color.White,fontWeight=FontWeight.Black,fontSize=22.sp)
                Text("${attempts} "+t(hindi,"attempts saved","प्रयास सुरक्षित"),
                    color=Color.White.copy(alpha=.92f),fontSize=11.sp)
            }
        }
        Panel {
            Text(t(hindi,"👤 Registered student","👤 पंजीकृत छात्र"),
                color=SoftWhite,fontWeight=FontWeight.Black,fontSize=16.sp)
            Spacer(Modifier.height(8.dp))
            Text(student?.email ?: t(hindi,"Review mode (not registered)","समीक्षा मोड (पंजीकृत नहीं)"),
                color=Violet,fontSize=13.sp)
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
            Text("✍️ "+t(hindi,"DISPLAY NAME","नाम सेट करें"),color=Muted,fontSize=11.sp,fontWeight=FontWeight.Bold)
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
                Icon(Icons.Rounded.DashboardCustomize,null,tint=Amber)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Your activity",color=SoftWhite,fontWeight=FontWeight.Bold)
                    Text("$attempts mock attempts saved on this device",color=Muted,fontSize=12.sp)
                }
            }
        }
        Panel {
            Text("🛡️ "+t(hindi,"ADS & PRIVACY","विज्ञापन और गोपनीयता"),
                color=SoftWhite,fontWeight=FontWeight.Bold,fontSize=14.sp)
            Spacer(Modifier.height(8.dp))
            Text("Rewarded ads are optional: mocks 3–50 need 1 completed ad, and mocks 51–100 need 2 completed ads. Each credit and unlock is saved on this device. Consent choices can be changed here where applicable.",
                color=Muted,fontSize=12.sp,lineHeight=19.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick=onPrivacy) { Text("Privacy / ad choices",color=Violet) }
        }
        Panel {
            Text(t(hindi,"ℹ️ About the app","ℹ️ ऐप के बारे में"),
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
