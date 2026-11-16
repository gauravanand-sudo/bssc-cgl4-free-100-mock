package com.bssc.cgl4.free100mock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val LoginNight = Color(0xFF210541)
private val LoginRoyal = Color(0xFF4C188D)
private val LoginBright = Color(0xFF7D48DC)
private val LoginGold = Color(0xFFFFD976)
private val LoginPaper = Color(0xFFFFFCFF)
private val LoginSubtle = Color(0xFF71637E)
private val LoginInk = Color(0xFF291342)

@Composable
private fun LoginBenefit(icon: ImageVector, label: String) {
    Row(
        Modifier.clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = LoginGold, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LoginStep(number: String, label: String, selected: Boolean) {
    Row(verticalAlignment=Alignment.CenterVertically) {
        Box(Modifier.size(27.dp).clip(CircleShape)
            .background(if (selected) LoginRoyal else Color(0xFFE9E2F5)),
            contentAlignment=Alignment.Center) {
            Text(number, fontSize=11.sp,fontWeight=FontWeight.Black,
                color=if (selected) Color.White else LoginSubtle)
        }
        Spacer(Modifier.width(7.dp))
        Text(label, color=if(selected) LoginRoyal else LoginSubtle,
            fontSize=11.sp,fontWeight=if(selected) FontWeight.Black else FontWeight.SemiBold)
    }
}

@Composable
private fun LoginPrivacyLine(hindi: Boolean) {
    Row(verticalAlignment=Alignment.Top, horizontalArrangement=Arrangement.spacedBy(7.dp)) {
        Icon(Icons.Rounded.Lock,contentDescription=null,
            tint=LoginSubtle, modifier=Modifier.size(14.dp))
        Text(t(hindi,
            "Google verifies your email. Mobile numbers are self-entered and not OTP-verified.",
            "Google आपकी ईमेल सत्यापित करता है। मोबाइल नंबर स्वयं भरा जाता है, OTP से सत्यापित नहीं।"),
            color=LoginSubtle,fontSize=10.sp,lineHeight=15.sp)
    }
}

/**
 * Motivational OneShotGS onboarding. Real Firebase registration callbacks remain
 * unchanged; language switch, validation and separate WhatsApp consent are preserved.
 */
@Composable
fun StudentRegistrationGate(
    state: SignupState,
    hindi: Boolean,
    editing: Boolean,
    onLanguage: () -> Unit,
    onGoogle: ((String?) -> Unit) -> Unit,
    onSave: (String, Boolean, (String?) -> Unit) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onPreview: () -> Unit
) {
    var phone by remember(state) {
        mutableStateOf((state as? SignupState.NeedsPhone)?.phone?.removePrefix("+91").orEmpty())
    }
    var optIn by remember(state) {
        mutableStateOf((state as? SignupState.NeedsPhone)?.whatsappOptIn ?: false)
    }
    var busy by remember { mutableStateOf(false) }
    var localError by remember(state) { mutableStateOf<String?>(null) }
    val quotesEn = listOf(
        "One focused mock today. One stronger attempt tomorrow.",
        "Your progress begins with showing up.",
        "Practice with purpose. Grow with every attempt."
    )
    val quotesHi = listOf(
        "आज का एक मॉक, कल की बेहतर तैयारी।",
        "हर दिन की मेहनत आपकी ताकत है।",
        "हर प्रयास से सीखें, हर दिन आगे बढ़ें।"
    )
    var quoteIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while(true) {
            delay(4900L)
            quoteIndex = (quoteIndex + 1) % quotesEn.size
        }
    }
    val isPhoneStep = state is SignupState.NeedsPhone
    val isStepOne = state is SignupState.NeedsGoogle
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
        listOf(LoginNight, LoginRoyal, Color(0xFF6535B0))))) {
        // Ambient glow gives the welcome screen depth without a separate illustration.
        Box(Modifier.align(Alignment.TopEnd).offset(x = 84.dp, y = (-98).dp)
            .size(245.dp).clip(CircleShape).background(Brush.radialGradient(
                listOf(Color(0x557F62EB),Color(0x005C2DAD)))))
        Box(Modifier.align(Alignment.BottomStart).offset(x = (-92).dp, y = 85.dp)
            .size(220.dp).clip(CircleShape).background(Brush.radialGradient(
                listOf(Color(0x4AFFD976),Color.Transparent))))
        Column(
            Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal=20.dp, vertical=17.dp),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                Row(Modifier.weight(1f),verticalAlignment=Alignment.CenterVertically) {
                    Box(
                        Modifier.size(76.dp).clip(RoundedCornerShape(19.dp))
                            .background(Color.White).padding(3.dp),
                        contentAlignment=Alignment.Center
                    ) {
                        Image(painterResource(R.drawable.bssc_bilingual_logo),
                            contentDescription="Approved OneShotGS BSSC CGL 4 badge",
                            modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Fit)
                    }
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text("OneShotGS",fontSize=20.sp,fontWeight=FontWeight.Black,
                            color=Color.White, letterSpacing=0.1.sp)
                        Text("BSSC CGL 4  •  FREE MOCK",
                            color=LoginGold,fontSize=10.sp,fontWeight=FontWeight.ExtraBold)
                        Text(t(hindi,"Your exam. Your journey.","आपकी परीक्षा, आपका सफर।"),
                            color=Color.White.copy(alpha=.8f),fontSize=10.sp)
                    }
                }
                Surface(
                    onClick=onLanguage,color=Color.White.copy(alpha=.14f),
                    shape=RoundedCornerShape(14.dp),
                    border=androidx.compose.foundation.BorderStroke(
                        1.dp,Color.White.copy(alpha=.24f))
                ) {
                    Text(if(hindi) "EN" else "हिंदी",color=Color.White,
                        modifier=Modifier.padding(horizontal=11.dp,vertical=10.dp),
                        fontSize=12.sp,fontWeight=FontWeight.Black)
                }
            }

            Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Text("✦  "+t(hindi,"THE OFFICER JOURNEY STARTS HERE",
                    "आपकी तैयारी की नई शुरुआत"),
                    color=LoginGold,fontWeight=FontWeight.Black,fontSize=10.sp,
                    letterSpacing=1.sp)
                Text(t(hindi,"Dream bigger.\nPrepare smarter.",
                    "सपने बड़े रखो।\nतैयारी दमदार करो।"),
                    color=Color.White,fontSize=29.sp,lineHeight=35.sp,
                    fontWeight=FontWeight.Black)
                Text(t(hindi,
                    "Your next chapter begins with one confident step.",
                    "सफलता की ओर हर दिन एक मजबूत कदम बढ़ाइए।"),
                    color=Color.White.copy(alpha=.84f),fontSize=12.sp,lineHeight=17.sp)
            }

            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                LoginBenefit(Icons.Rounded.MenuBook,t(hindi,"150 Q / mock","150 प्रश्न / मॉक"))
                LoginBenefit(Icons.Rounded.Language,t(hindi,"Hindi + English","हिंदी + English"))
            }

            Surface(
                modifier=Modifier.fillMaxWidth().shadow(18.dp,RoundedCornerShape(27.dp)),
                shape=RoundedCornerShape(27.dp),color=LoginPaper
            ) {
                Column(Modifier.padding(horizontal=18.dp,vertical=20.dp),
                    verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,
                        horizontalArrangement=Arrangement.SpaceBetween) {
                        LoginStep("1",t(hindi,"Google","Google"),!isPhoneStep)
                        Box(Modifier.weight(1f).padding(horizontal=8.dp).height(2.dp)
                            .background(Color(0xFFE8DDF4),CircleShape))
                        LoginStep("2",t(hindi,"Mobile","मोबाइल"),isPhoneStep)
                    }
                    when(state) {
                        SignupState.Loading -> {
                            Row(Modifier.fillMaxWidth().padding(vertical=24.dp),
                                horizontalArrangement=Arrangement.Center,
                                verticalAlignment=Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier=Modifier.size(23.dp),
                                    color=LoginRoyal,strokeWidth=2.dp)
                                Spacer(Modifier.width(10.dp))
                                Text(t(hindi,"Preparing your account…","आपका खाता तैयार हो रहा है…"),
                                    color=LoginInk,fontWeight=FontWeight.Bold,fontSize=13.sp)
                            }
                        }
                        SignupState.NeedsGoogle -> {
                            Text(t(hindi,"Welcome, future officer!","स्वागत है, भावी अधिकारी!"),
                                color=LoginInk,fontWeight=FontWeight.Black,fontSize=20.sp)
                            Text(t(hindi,
                                "Sign in securely with Google to save your student profile.",
                                "अपनी छात्र प्रोफ़ाइल के लिए Google से सुरक्षित लॉग इन करें।"),
                                color=LoginSubtle,fontSize=12.sp,lineHeight=17.sp)
                            Spacer(Modifier.height(2.dp))
                            Button(
                                onClick={
                                    localError=null;busy=true
                                    onGoogle { error -> busy=false;localError=error }
                                }, enabled=!busy,
                                modifier=Modifier.fillMaxWidth().height(54.dp),
                                shape=RoundedCornerShape(17.dp),
                                colors=ButtonDefaults.buttonColors(
                                    containerColor=LoginRoyal,contentColor=Color.White),
                                contentPadding=PaddingValues(horizontal=17.dp)
                            ) {
                                if(busy) CircularProgressIndicator(
                                    modifier=Modifier.size(19.dp),color=Color.White,strokeWidth=2.dp)
                                else {
                                    Box(Modifier.size(27.dp).clip(CircleShape)
                                        .background(Color.White),
                                        contentAlignment=Alignment.Center) {
                                        Text("G",fontWeight=FontWeight.Black,fontSize=17.sp,
                                            color=Color(0xFF4285F4))
                                    }
                                    Spacer(Modifier.width(11.dp))
                                    Text(t(hindi,"Continue with Google","Google से जारी रखें"),
                                        fontSize=14.sp,fontWeight=FontWeight.Black,
                                        modifier=Modifier.weight(1f))
                                    Icon(Icons.Rounded.ArrowForward,contentDescription=null,
                                        modifier=Modifier.size(18.dp))
                                }
                            }
                            LoginPrivacyLine(hindi)
                        }
                        is SignupState.NeedsPhone -> {
                            Row(verticalAlignment=Alignment.CenterVertically) {
                                Icon(Icons.Rounded.VerifiedUser,contentDescription=null,
                                    tint=Color(0xFF178463), modifier=Modifier.size(21.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(t(hindi,"You're almost there!","बस एक कदम और!"),
                                        color=LoginInk,fontWeight=FontWeight.Black,fontSize=20.sp)
                                    Text(state.email,color=LoginSubtle,fontSize=11.sp,
                                        maxLines=1,overflow=TextOverflow.Ellipsis)
                                }
                            }
                            Text(t(hindi,
                                "Add your number to finish setting up your student account.",
                                "छात्र खाता पूरा करने के लिए अपना मोबाइल नंबर जोड़ें।"),
                                color=LoginSubtle,fontSize=12.sp)
                            OutlinedTextField(
                                value=phone,
                                onValueChange={phone=it.filter { char -> char.isDigit() }.take(10)},
                                modifier=Modifier.fillMaxWidth(),
                                singleLine=true,
                                prefix={Text("+91  ",fontWeight=FontWeight.Black,
                                    color=LoginRoyal)},
                                label={Text(t(hindi,"Mobile number","मोबाइल नंबर"))},
                                leadingIcon={Icon(Icons.Rounded.PhoneAndroid,null,
                                    tint=LoginRoyal)},
                                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone),
                                supportingText={Text(t(hindi,
                                    "10 digits · no SMS or WhatsApp OTP",
                                    "10 अंक · SMS या WhatsApp OTP नहीं"),
                                    fontSize=10.sp)},
                                shape=RoundedCornerShape(15.dp),
                                colors=OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor=LoginRoyal,
                                    unfocusedBorderColor=Color(0xFFCEBEE4),
                                    focusedLabelColor=LoginRoyal
                                )
                            )
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                                    .background(Color(0xFFF2EDFA))
                                    .clickable { optIn=!optIn }
                                    .padding(end=12.dp,top=5.dp,bottom=5.dp),
                                verticalAlignment=Alignment.CenterVertically) {
                                Checkbox(checked=optIn,onCheckedChange={optIn=it},
                                    colors=CheckboxDefaults.colors(checkedColor=LoginRoyal))
                                Text(t(hindi,
                                    "Optional: Send me OneShotGS exam updates on WhatsApp. I can opt out anytime.",
                                    "वैकल्पिक: मुझे WhatsApp पर OneShotGS परीक्षा अपडेट भेजें। सहमति कभी भी बदल सकता/सकती हूँ।"),
                                    modifier=Modifier.weight(1f),
                                    color=LoginInk,fontSize=11.sp,lineHeight=16.sp)
                            }
                            Button(
                                onClick={
                                    localError=null;busy=true
                                    onSave(phone,optIn){error->busy=false;localError=error}
                                },enabled=!busy && IndianMobile.normalize(phone)!=null,
                                modifier=Modifier.fillMaxWidth().height(54.dp),
                                shape=RoundedCornerShape(17.dp),
                                colors=ButtonDefaults.buttonColors(containerColor=LoginRoyal)
                            ) {
                                if(busy) CircularProgressIndicator(
                                    modifier=Modifier.size(19.dp),color=Color.White,strokeWidth=2.dp)
                                else {
                                    Text(t(hindi,"Start my preparation","मेरी तैयारी शुरू करें"),
                                        fontSize=14.sp,fontWeight=FontWeight.Black)
                                    Spacer(Modifier.width(9.dp))
                                    Icon(Icons.Rounded.ArrowForward,null,
                                        modifier=Modifier.size(18.dp))
                                }
                            }
                            if(editing) TextButton(onClick=onCancel,
                                modifier=Modifier.align(Alignment.CenterHorizontally)) {
                                Text(t(hindi,"Cancel changes","बदलाव रद्द करें"),color=LoginRoyal)
                            }
                            LoginPrivacyLine(hindi)
                        }
                        is SignupState.Unavailable -> {
                            Icon(Icons.Rounded.WifiOff,contentDescription=null,
                                tint=LoginRoyal,modifier=Modifier.size(28.dp))
                            Text(t(hindi,"We couldn't connect just yet.","अभी कनेक्ट नहीं हो सका।"),
                                color=LoginInk,fontWeight=FontWeight.Black,fontSize=17.sp)
                            Text(state.reason,color=LoginSubtle,fontSize=12.sp,lineHeight=17.sp)
                            Button(onClick=onRetry,modifier=Modifier.fillMaxWidth(),
                                colors=ButtonDefaults.buttonColors(containerColor=LoginRoyal)) {
                                Icon(Icons.Rounded.Refresh,null)
                                Spacer(Modifier.width(7.dp))
                                Text(t(hindi,"Try again","दोबारा कोशिश करें"))
                            }
                        }
                        is SignupState.Ready -> {
                            Text(t(hindi,"You're ready to go!","आप तैयार हैं!"),
                                color=LoginInk,fontWeight=FontWeight.Black,fontSize=20.sp)
                        }
                    }
                    AnimatedVisibility(visible=localError!=null) {
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEBEE)).padding(12.dp),
                            verticalAlignment=Alignment.Top,
                            horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                            Icon(Icons.Rounded.Info,contentDescription=null,
                                tint=Color(0xFFB42338),modifier=Modifier.size(18.dp))
                            Text(localError.orEmpty(),color=Color(0xFF9C2232),
                                fontSize=11.sp,lineHeight=16.sp)
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha=.11f))
                    .padding(horizontal=13.dp,vertical=12.dp),
                verticalAlignment=Alignment.CenterVertically) {
                Text("✦",fontSize=19.sp,color=LoginGold)
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(t(hindi,"TODAY'S MINDSET","आज का संकल्प"),
                        color=LoginGold,fontWeight=FontWeight.Black,fontSize=10.sp)
                    AnimatedContent(targetState=quoteIndex,label="login motivation") { index ->
                        Text(if(hindi) quotesHi[index] else quotesEn[index],
                            color=Color.White,fontWeight=FontWeight.SemiBold,
                            fontSize=11.sp,lineHeight=17.sp)
                    }
                }
            }

            if(BuildConfig.DEBUG && !editing) {
                TextButton(onClick=onPreview) {
                    Text(t(hindi,"Preview app without registration (test only)",
                        "बिना पंजीकरण ऐप देखें (सिर्फ परीक्षण)"),
                        color=Color.White.copy(alpha=.82f),fontSize=11.sp)
                }
            }
            Text(t(hindi,
                "Your registration is private. WhatsApp messages require your separate consent. Manage or delete your data anytime in More.",
                "आपका पंजीकरण निजी है। WhatsApp संदेशों के लिए अलग सहमति चाहिए। अन्य टैब से डेटा बदलें या मिटाएँ।"),
                color=Color.White.copy(alpha=.72f),
                fontSize=10.sp,lineHeight=15.sp,textAlign=TextAlign.Center)
            Spacer(Modifier.height(4.dp))
        }
    }
}
