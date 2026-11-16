package com.bssc.cgl4.free100mock

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Night = Color(0xFF21094B)
private val RoyalPurple = Color(0xFF57249A)
private val WarmGold = Color(0xFFFFD67A)
private val CardWhite = Color(0xFFFFFDFF)
private val InkPurple = Color(0xFF27143E)
private val SecondaryInk = Color(0xFF6D6082)
private val SubtleBorder = Color(0xFFE5DCF1)

@Composable
private fun RegistrationProgress(hindi:Boolean, phoneStep:Boolean) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.Center) {
        listOf(Triple("1",t(hindi,"YOUR NAME","आपका नाम"),!phoneStep),
            Triple("2",t(hindi,"MOBILE","मोबाइल"),phoneStep)).forEachIndexed { index,step ->
            if(index>0) Box(Modifier.width(32.dp).height(2.dp)
                .background(SubtleBorder,RoundedCornerShape(4.dp)))
            Row(verticalAlignment=Alignment.CenterVertically,
                modifier=Modifier.padding(horizontal=6.dp)) {
                Box(Modifier.size(24.dp).clip(CircleShape)
                    .background(if(step.third)RoyalPurple else Color(0xFFF0EAF7)),
                    contentAlignment=Alignment.Center) {
                    Text(step.first,fontSize=11.sp,fontWeight=FontWeight.Black,
                        color=if(step.third)Color.White else SecondaryInk)
                }
                Spacer(Modifier.width(5.dp))
                Text(step.second,fontSize=10.sp,fontWeight=FontWeight.Bold,
                    color=if(step.third)RoyalPurple else SecondaryInk)
            }
        }
    }
}

@Composable
fun StudentRegistrationGate(
    state: SignupState,
    hindi: Boolean,
    editing: Boolean,
    onLanguage: () -> Unit,
    onGoogle: (String?, (String?) -> Unit) -> Unit,
    onSave: (String, String, Boolean, (String?) -> Unit) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    // NOT keyed by Firebase auth state: survives Google sign-in and phone step.
    var studentName by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var whatsappOptIn by rememberSaveable { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state, editing) {
        if(state is SignupState.NeedsPhone) {
            if(editing) {
                studentName=state.displayName
                mobile=state.phone.removePrefix("+91")
                whatsappOptIn=state.whatsappOptIn
            } else if(mobile.isEmpty()) {
                mobile=state.phone.removePrefix("+91")
            }
        }
    }

    val phoneStep = state is SignupState.NeedsPhone
    val cleanName = StudentName.normalize(studentName)
    BoxWithConstraints(Modifier.fillMaxSize().imePadding()
        .background(Brush.verticalGradient(listOf(Night,RoyalPurple,Color(0xFF6D41AF))))) {
        // Scroll container measures content at natural height. The inner Box has
        // at least viewport height so short forms are vertically CENTERED;
        // larger forms scroll naturally on short phones and with the keyboard.
        val viewportHeight = maxHeight
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().heightIn(min=viewportHeight)
                .padding(horizontal=20.dp,vertical=16.dp),
                contentAlignment=Alignment.Center) {
            Column(
                modifier=Modifier.widthIn(max=430.dp).fillMaxWidth(),
                horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.spacedBy(12.dp)
            ) {
                // Logo center is independent from the language switch.
                // This is centered relative to the entire login card width.
                Box(Modifier.fillMaxWidth().height(33.dp)) {
                    Surface(onClick=onLanguage,
                        modifier=Modifier.align(Alignment.CenterEnd),
                        shape=RoundedCornerShape(11.dp),
                        color=Color.White.copy(alpha=.13f),
                        border=androidx.compose.foundation.BorderStroke(
                            1.dp,Color.White.copy(alpha=.26f))) {
                        Text(if(hindi)"EN" else "हिंदी",
                            modifier=Modifier.padding(horizontal=12.dp,vertical=8.dp),
                            color=Color.White,fontWeight=FontWeight.Black,fontSize=11.sp)
                    }
                }
                Image(painterResource(R.drawable.bssc_bilingual_logo),
                    contentDescription="Approved OneShotGS BSSC CGL 4 logo",
                    modifier=Modifier.size(105.dp).align(Alignment.CenterHorizontally),
                    contentScale=ContentScale.Fit)
                Text("OneShotGS",color=Color.White,fontSize=21.sp,
                    fontWeight=FontWeight.Black,textAlign=TextAlign.Center,
                    modifier=Modifier.fillMaxWidth())
                Text("BSSC CGL 4 • FREE MOCK",color=WarmGold,fontSize=10.sp,
                    fontWeight=FontWeight.Black,textAlign=TextAlign.Center,
                    modifier=Modifier.fillMaxWidth())
                Column(Modifier.fillMaxWidth(),
                    horizontalAlignment=Alignment.CenterHorizontally,
                    verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    Text(t(hindi,"YOUR JOURNEY STARTS HERE","आपकी तैयारी की शुरुआत"),
                        color=WarmGold,fontWeight=FontWeight.Black,
                        letterSpacing=1.0.sp,fontSize=10.sp)
                    Text(t(hindi,"Prepare with purpose.\nRise with confidence.",
                        "संकल्प के साथ तैयारी।\nविश्वास के साथ सफलता।"),
                        color=Color.White,fontSize=24.sp,lineHeight=30.sp,
                        fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
                    Text(t(hindi,"One mock at a time. One step closer to your goal.",
                        "हर मॉक के साथ लक्ष्य के एक कदम और करीब।"),
                        color=Color.White.copy(alpha=.83f),fontSize=11.sp,
                        lineHeight=16.sp,textAlign=TextAlign.Center)
                }
                Surface(
                    modifier=Modifier.fillMaxWidth(),
                    color=CardWhite,shape=RoundedCornerShape(24.dp),
                    shadowElevation=12.dp
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement=Arrangement.spacedBy(11.dp)) {
                        RegistrationProgress(hindi,phoneStep)
                        when(state) {
                            SignupState.Loading -> {
                                Row(Modifier.fillMaxWidth().padding(vertical=25.dp),
                                    horizontalArrangement=Arrangement.Center,
                                    verticalAlignment=Alignment.CenterVertically) {
                                    CircularProgressIndicator(Modifier.size(22.dp),
                                        color=RoyalPurple,strokeWidth=2.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Text(t(hindi,"Preparing your account…","खाता तैयार किया जा रहा है…"),
                                        color=InkPurple,fontSize=13.sp)
                                }
                            }
                            SignupState.NeedsGoogle -> {
                                Text(t(hindi,"Hello, future officer!","स्वागत है, भावी अधिकारी!"),
                                    color=InkPurple,fontSize=19.sp,
                                    fontWeight=FontWeight.Black)
                                Text(t(hindi,
                                    "First, tell us what you'd like to be called.",
                                    "सबसे पहले अपना नाम दर्ज करें।"),
                                    color=SecondaryInk,fontSize=12.sp)
                                OutlinedTextField(
                                    value=studentName,
                                    onValueChange={studentName=it.replace("\n"," ").take(28)},
                                    modifier=Modifier.fillMaxWidth(),
                                    singleLine=true,
                                    label={Text(t(hindi,"Your name","आपका नाम"))},
                                    placeholder={Text(t(hindi,"Enter your full name","अपना पूरा नाम लिखें"))},
                                    leadingIcon={Icon(Icons.Rounded.PersonOutline,null,tint=RoyalPurple)},
                                    keyboardOptions=KeyboardOptions(
                                        capitalization=KeyboardCapitalization.Words),
                                    shape=RoundedCornerShape(14.dp),
                                    colors=OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor=RoyalPurple,
                                        unfocusedBorderColor=SubtleBorder)
                                )
                                Text(t(hindi,
                                    "We'll use this name on your Home screen, profile and public mock rankings.",
                                    "यही नाम होम स्क्रीन, प्रोफ़ाइल और सार्वजनिक मॉक रैंकिंग पर दिखेगा।"),
                                    color=SecondaryInk,fontSize=10.sp,lineHeight=15.sp)
                                Button(
                                    onClick={
                                        error=null;working=true
                                        onGoogle(cleanName!!) { result -> working=false;error=result }
                                    },enabled=!working && cleanName!=null,
                                    modifier=Modifier.fillMaxWidth().height(52.dp),
                                    shape=RoundedCornerShape(14.dp),
                                    colors=ButtonDefaults.buttonColors(containerColor=RoyalPurple)
                                ) {
                                    if(working) CircularProgressIndicator(
                                        modifier=Modifier.size(18.dp),color=Color.White,
                                        strokeWidth=2.dp)
                                    else {
                                        Box(Modifier.size(25.dp).clip(CircleShape)
                                            .background(Color.White),
                                            contentAlignment=Alignment.Center) {
                                            Text("G",fontSize=16.sp,fontWeight=FontWeight.Black,
                                                color=Color(0xFF4285F4))
                                        }
                                        Spacer(Modifier.width(9.dp))
                                        Text(t(hindi,"Continue with Google","Google से जारी रखें"),
                                            fontWeight=FontWeight.Black,fontSize=13.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Icon(Icons.Rounded.ArrowForward,null,
                                            modifier=Modifier.size(18.dp))
                                    }
                                }
                                Text(t(hindi,"Free account • No SMS OTP charges",
                                    "मुफ़्त खाता • कोई SMS OTP शुल्क नहीं"),
                                    color=SecondaryInk,fontSize=10.sp,
                                    modifier=Modifier.align(Alignment.CenterHorizontally))
                                HorizontalDivider(color=SubtleBorder)
                                TextButton(
                                    enabled=!working,
                                    modifier=Modifier.fillMaxWidth(),
                                    onClick={
                                        error=null;working=true
                                        onGoogle(null) { result -> working=false;error=result }
                                    }) {
                                    Text(t(hindi,
                                        "Already registered? Sign in with Google",
                                        "पहले से खाता है? Google से लॉगिन करें"),
                                        color=RoyalPurple,fontSize=12.sp,
                                        fontWeight=FontWeight.Bold)
                                }
                            }
                            is SignupState.NeedsPhone -> {
                                Text(t(hindi,"Almost ready!","बस एक कदम और!"),
                                    color=InkPurple,fontSize=20.sp,
                                    fontWeight=FontWeight.Black)
                                Text(t(hindi,"Welcome, ","स्वागत है, ")+
                                        (cleanName ?: state.displayName),
                                    color=RoyalPurple,fontWeight=FontWeight.Bold,
                                    fontSize=14.sp)
                                Text(state.email,color=SecondaryInk,fontSize=11.sp,
                                    maxLines=1,overflow=TextOverflow.Ellipsis)
                                // Recover gracefully if sign-in is resumed after app restart.
                                if(!editing && cleanName==null) {
                                    OutlinedTextField(
                                        value=studentName,
                                        onValueChange={studentName=it.replace("\n"," ").take(28)},
                                        modifier=Modifier.fillMaxWidth(),
                                        label={Text(t(hindi,"Your name","आपका नाम"))},
                                        leadingIcon={Icon(Icons.Rounded.PersonOutline,null,
                                            tint=RoyalPurple)},
                                        singleLine=true,shape=RoundedCornerShape(14.dp))
                                }
                                OutlinedTextField(
                                    value=mobile,
                                    onValueChange={mobile=it.filter(Char::isDigit).take(10)},
                                    modifier=Modifier.fillMaxWidth(),
                                    label={Text(t(hindi,"Mobile number","मोबाइल नंबर"))},
                                    prefix={Text("+91  ",color=RoyalPurple,
                                        fontWeight=FontWeight.Black)},
                                    leadingIcon={Icon(Icons.Rounded.PhoneAndroid,null,
                                        tint=RoyalPurple)},
                                    supportingText={Text(t(hindi,
                                        "10 digits • Not OTP-verified",
                                        "10 अंक • OTP से सत्यापित नहीं"),
                                        fontSize=10.sp)},
                                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone),
                                    singleLine=true,shape=RoundedCornerShape(14.dp)
                                )
                                Row(Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Color(0xFFF3EDF9))
                                    .clickable { whatsappOptIn=!whatsappOptIn }
                                    .padding(end=8.dp,top=3.dp,bottom=3.dp),
                                    verticalAlignment=Alignment.CenterVertically) {
                                    Checkbox(checked=whatsappOptIn,
                                        onCheckedChange={whatsappOptIn=it},
                                        colors=CheckboxDefaults.colors(checkedColor=RoyalPurple))
                                    Text(t(hindi,
                                        "Optional: Send exam updates to my WhatsApp. I may opt out later.",
                                        "वैकल्पिक: मुझे WhatsApp पर परीक्षा अपडेट भेजें। सहमति बाद में बदल सकते हैं।"),
                                        modifier=Modifier.weight(1f),
                                        color=InkPurple,fontSize=11.sp,lineHeight=15.sp)
                                }
                                Button(
                                    onClick={
                                        error=null;working=true
                                        onSave(if(editing)state.displayName else studentName,
                                            mobile,whatsappOptIn) { result ->
                                                working=false;error=result
                                            }
                                    },
                                    enabled=!working &&
                                        IndianMobile.normalize(mobile)!=null &&
                                        StudentName.normalize(
                                            if(editing)state.displayName else studentName)!=null,
                                    modifier=Modifier.fillMaxWidth().height(52.dp),
                                    shape=RoundedCornerShape(14.dp),
                                    colors=ButtonDefaults.buttonColors(containerColor=RoyalPurple)
                                ) {
                                    if(working) CircularProgressIndicator(
                                        Modifier.size(18.dp),color=Color.White,strokeWidth=2.dp)
                                    else {
                                        Text(t(hindi,"Start my preparation","मेरी तैयारी शुरू करें"),
                                            fontWeight=FontWeight.Black,fontSize=13.sp)
                                        Spacer(Modifier.width(7.dp))
                                        Icon(Icons.Rounded.ArrowForward,null,
                                            modifier=Modifier.size(17.dp))
                                    }
                                }
                                if(editing) TextButton(onClick=onCancel,
                                    modifier=Modifier.align(Alignment.CenterHorizontally)) {
                                    Text(t(hindi,"Cancel","रद्द करें"),color=RoyalPurple)
                                }
                            }
                            is SignupState.Unavailable -> {
                                Text(t(hindi,"Connection needed","कनेक्शन आवश्यक है"),
                                    color=InkPurple,fontSize=18.sp,
                                    fontWeight=FontWeight.Black)
                                Text(state.reason,color=SecondaryInk,fontSize=12.sp)
                                Button(onClick=onRetry,
                                    modifier=Modifier.fillMaxWidth()) {
                                    Text(t(hindi,"Try again","दोबारा कोशिश करें"))
                                }
                            }
                            is SignupState.Ready -> {
                                Text(t(hindi,"All set!","तैयारी शुरू!"),
                                    color=InkPurple,fontWeight=FontWeight.Black)
                            }
                        }
                        error?.let { message ->
                            Surface(color=Color(0xFFFFEAEC),
                                shape=RoundedCornerShape(11.dp)) {
                                Text(message,color=Color(0xFF9A2835),fontSize=11.sp,
                                    modifier=Modifier.padding(11.dp))
                            }
                        }
                    }
                }
                Text(t(hindi,"Today's effort. Tomorrow's success.",
                    "आज की मेहनत, कल की सफलता।"),
                    color=WarmGold,fontWeight=FontWeight.Bold,fontSize=12.sp,
                    textAlign=TextAlign.Center)
                Text(t(hindi,
                    "Your details are stored privately. Mobile numbers are not verified. WhatsApp updates require separate consent.",
                    "आपका डेटा निजी रहेगा। मोबाइल OTP सत्यापित नहीं है। WhatsApp अपडेट के लिए अलग सहमति चाहिए।"),
                    color=Color.White.copy(alpha=.73f),fontSize=10.sp,
                    lineHeight=15.sp,textAlign=TextAlign.Center)
            }
            }
        }
    }
}
