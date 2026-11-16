package com.bssc.cgl4.free100mock

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class Tab { HOME, TESTS, ANALYTICS, RANKS, PROFILE }
private sealed interface Route {
    data object Tabs : Route
    data class Exam(val testId: Int) : Route
    data class Result(val attempt: Attempt) : Route
}

class MainActivity : ComponentActivity() {
    private lateinit var adController: AdController
    private var safeHome = false
    private var stoppedAt = 0L
    override fun onStop() { super.onStop(); stoppedAt = System.currentTimeMillis() }
    override fun onStart() {
        super.onStart()
        if (stoppedAt > 0L && safeHome) adController.onReturnToHome(System.currentTimeMillis() - stoppedAt)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(38,6,68)
        window.navigationBarColor = android.graphics.Color.rgb(38,6,68)
        // Purple system bars with light icons; content itself respects safe insets.
        window.decorView.systemUiVisibility = 0
        BrandCopy.initialize(this)
        val store = AppStore(this)
        val questions = QuestionRepository(this)
        val catalog = CatalogRepository(this)
        adController = AdController(this)
        val ads = adController
        val board = LeaderboardRepository(this)
        val engagement = EngagementRepository(this)
        val homeContent = HomeCardsRepository(this)
        val notificationPrefs = PushPreferences(this)
        ExamPushService.ensureChannel(this)
        setContent {
            RankTheme {
                var tab by remember { mutableStateOf(Tab.HOME) }
                var route by remember { mutableStateOf<Route>(Route.Tabs) }
                var attempts by remember { mutableStateOf(store.attempts()) }
                var activeSession by remember { mutableStateOf(store.session()) }
                var unlocked by remember { mutableStateOf(store.unlocked()) }
                var rewardCredits by remember { mutableStateOf(store.rewardCredits()) }
                var hindi by remember { mutableStateOf(store.hindi()) }
                var name by remember { mutableStateOf(store.name()) }
                var tests by remember { mutableStateOf(catalog.cached()) }
                var catalogStatus by remember { mutableStateOf<String?>(null) }
                var loadingCatalog by remember { mutableStateOf(false) }
                var busyPaper by remember { mutableStateOf(false) }
                var paperError by remember { mutableStateOf<String?>(null) }
                var quickIntroId by remember { mutableStateOf<Int?>(null) }
                var preflightId by remember { mutableStateOf<Int?>(null) }
                var unlockEvent by remember { mutableStateOf<Triple<Int,Int,Int>?>(null) }
                LaunchedEffect(unlockEvent) {
                    if(unlockEvent!=null) {
                        delay(3200L)
                        unlockEvent=null
                    }
                }
                var adReady by remember { mutableStateOf(false) }
                var quickReady by remember { mutableStateOf(false) }
                var newlyFinished by remember { mutableStateOf(false) }
                var confirmExitTest by remember { mutableStateOf(false) }
                var remoteBanner by remember { mutableStateOf<PromoBanner?>(null) }
                var examEvents by remember { mutableStateOf<List<ExamEvent>>(emptyList()) }
                var homeCards by remember { mutableStateOf<List<HomeCard>>(emptyList()) }
                var notificationsEnabled by remember { mutableStateOf(notificationPrefs.enabled()) }
                var splash by remember { mutableStateOf(true) }
                val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    if (granted) notificationPrefs.change(true) { ok ->
                        notificationsEnabled=ok
                        if(!ok) Toast.makeText(this@MainActivity,"Connect Firebase to activate notifications",Toast.LENGTH_LONG).show()
                    }
                    else Toast.makeText(this@MainActivity,"Notifications remain off",Toast.LENGTH_SHORT).show()
                }
                val toggleNotifications: () -> Unit = {
                    if (notificationsEnabled) notificationPrefs.change(false) { notificationsEnabled=false }
                    else if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                            this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else notificationPrefs.change(true) { ok ->
                        notificationsEnabled=ok
                        if(!ok) Toast.makeText(this@MainActivity,"Set up Firebase first",Toast.LENGTH_LONG).show()
                    }
                }
                val notifyAd: () -> Unit = { adReady = ads.ready; quickReady = ads.quickRewardReady }
                fun refreshCatalogue() {
                    loadingCatalog = true
                    catalog.refresh { result, status ->
                        tests = result
                        ExamConfig.setTitles(result)
                        catalogStatus = status
                        loadingCatalog = false
                    }
                }
                // Firebase controls the single banner, countdown and modular Home cards.
                // All listeners are removed with the UI lifecycle.
                DisposableEffect(Unit) {
                    val stopBanner=engagement.observeBanner {remoteBanner=it}
                    val stopEvents=engagement.observeEvents {examEvents=it}
                    val stopCards=homeContent.observe {homeCards=it}
                    onDispose {stopBanner();stopEvents();stopCards()}
                }
                LaunchedEffect(Unit) {
                    ExamConfig.setTitles(tests)
                    ads.initialize(notifyAd)
                    refreshCatalogue()
                    delay(800L)
                    splash=false
                }
                SideEffect { safeHome = route == Route.Tabs && tab == Tab.HOME }
                fun startPaper(id: Int) {
                    val test = tests.firstOrNull { it.id == id }
                    if (test==null) {
                        paperError=t(hindi,"This mock is not in the catalogue.",
                            "यह मॉक अभी सूची में नहीं है।")
                        return
                    }
                    // Firebase Upcoming overrides all older saved ad unlocks.
                    if (test.isUpcoming) {
                        paperError=t(hindi,"⏳ This mock is coming soon. Check again later.",
                            "⏳ यह मॉक जल्द आएगा। बाद में जाँचें।")
                        return
                    }
                    if (!MockAvailability.canStart(test, unlocked)) {
                        paperError = t(hindi,"Watch a rewarded ad to unlock this mock.","विज्ञापन देखकर मॉक खोलें।")
                        return
                    }
                    val existing = activeSession
                    if (existing != null && existing.testId != id) {
                        Toast.makeText(this@MainActivity,"Resume or finish your active mock first",Toast.LENGTH_LONG).show()
                        return
                    }
                    busyPaper = true
                    paperError = null
                    questions.load(test) { available, error ->
                        busyPaper = false
                        if (available && MockAvailability.canStart(test, unlocked)) {
                            val session = existing ?: ExamSession(id,System.currentTimeMillis())
                            activeSession = session
                            store.saveSession(session)
                            route = Route.Exam(id)
                        } else paperError = if (test.isUpcoming)
                            t(hindi,"This mock is coming soon.","यह मॉक जल्द आएगा।")
                            else if (!MockAvailability.canStart(test, unlocked))
                                t(hindi,"Watch a rewarded ad to unlock this mock.","विज्ञापन देखकर मॉक खोलें।")
                            else error ?: "This paper could not be loaded."
                    }
                }
                val startTest: (Int) -> Unit = { id ->
                    val test = tests.firstOrNull { it.id == id }
                    if (test == null) paperError = "This mock isn't in the current catalogue."
                    else if (test.isUpcoming) {
                        Toast.makeText(this@MainActivity,
                            t(hindi,"⏳ Mock coming soon","⏳ मॉक जल्द उपलब्ध होगा"),
                            Toast.LENGTH_LONG).show()
                    }
                    else if (MockAvailability.canStart(test, unlocked)) {
                        if(activeSession!=null && activeSession?.testId != id) {
                            Toast.makeText(this@MainActivity,
                                t(hindi,"Finish or resume your current exam first",
                                    "पहले मौजूदा परीक्षा पूरी करें या जारी रखें"),
                                Toast.LENGTH_LONG).show()
                        } else if(activeSession?.testId == id) startPaper(id)
                        else preflightId=id
                    }
                    else Toast.makeText(this@MainActivity,
                        t(hindi,"Watch a rewarded ad to unlock","विज्ञापन देखकर अनलॉक करें"),Toast.LENGTH_SHORT).show()
                }
                val earnedUnlock: (Int) -> Unit = { id ->
                    if (tests.any {it.id==id && MockAvailability.canWatchReward(it,unlocked)}) {
                        val earned = store.recordEarnedReward(id)
                        rewardCredits = store.rewardCredits()
                        unlocked = store.unlocked()
                        val required = MockAccess.requiredAds(id)
                        unlockEvent=Triple(id,earned,required)
                    }
                }
                val unavailable = {
                    Toast.makeText(this,"No ad available. Please try again shortly.",Toast.LENGTH_LONG).show()
                }
                val unlock: (Int) -> Unit = { id ->
                    if (tests.any {it.id==id && MockAvailability.canWatchReward(it,unlocked)}) {
                        ads.showRewarded(onEarned={earnedUnlock(id)},onUnavailable=unavailable,onChange=notifyAd)
                    }
                }
                val quickUnlock: (Int) -> Unit = { id ->
                    if (tests.any {it.id==id && MockAvailability.canWatchReward(it,unlocked)})
                        quickIntroId = id
                }
                // The system Back button must not leave an ongoing exam without confirmation.
                BackHandler(enabled = route is Route.Exam) { confirmExitTest = true }
                BackHandler(enabled = route is Route.Result) { route = Route.Tabs; tab = Tab.HOME }
                // Include cutouts, system status and gesture navigation insets on Android 15+.
                Box(Modifier.fillMaxSize().background(RoyalDeep).safeDrawingPadding()) {
                    Column(Modifier.fillMaxSize().background(Ink)) {
                        if(route is Route.Tabs) {
                            GlobalExamHeader(hindi,onLanguage={hindi=!hindi;store.setHindi(hindi)},
                                notificationsEnabled=notificationsEnabled,
                                showNotifications=true,
                                onNotifications=toggleNotifications)
                        }
                        Box(Modifier.weight(1f)) {
                when (val page = route) {
                    is Route.Exam -> {
                        val session = activeSession ?: store.session()
                        val paper = questions.getOrNull(page.testId)
                        if (session == null || session.testId != page.testId || paper == null) {
                            route = Route.Tabs
                        } else ExamScreen(initial=session,questions=paper,hindi=hindi,
                            onSave={ updated -> activeSession=updated; store.saveSession(updated) },
                            onExit={confirmExitTest=true},
                            onFinish={ latest ->
                                confirmExitTest=false
                                val a=Grading.grade(latest.testId,latest.startedAt,latest.answers,paper)
                                store.addAttempt(a)
                                attempts=store.attempts()
                                activeSession=null
                                board.submit(a,name)
                                newlyFinished=true
                                route=Route.Result(a)
                            })
                    }
                    is Route.Result -> {
                        val paper=questions.getOrNull(page.attempt.testId)
                        if(paper == null) {
                            Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
                                PrimaryAction(t(hindi,"Return home","होम पर जाएँ"),onClick={route=Route.Tabs;tab=Tab.HOME})
                            }
                        } else ResultScreen(page.attempt,paper,hindi,
                            onDone={
                                val goHome = { route=Route.Tabs;tab=Tab.HOME; newlyFinished=false }
                                if(newlyFinished) ads.afterResult(goHome) else goHome()
                            },
                            onAnalytics={
                                val goAnalytics = {
                                    route=Route.Tabs;tab=Tab.ANALYTICS;newlyFinished=false
                                }
                                if(newlyFinished) ads.afterResult(goAnalytics) else goAnalytics()
                            },ads=ads,
                                previousScore=attempts.filter {
                                    it.testId==page.attempt.testId &&
                                        it.completedAt < page.attempt.completedAt
                                }.maxByOrNull {it.completedAt}?.score)
                    }
                    Route.Tabs -> Scaffold(containerColor=Ink,
                        bottomBar={
                            // Reclaimed screen space: compact floating navigation.
                            Row(Modifier.fillMaxWidth().height(56.dp)
                                .background(Ink).padding(horizontal=9.dp,vertical=3.dp)
                                .shadow(5.dp,RoundedCornerShape(19.dp))
                                .clip(RoundedCornerShape(19.dp))
                                .background(androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(RoyalDeep,Royal,Color(0xFF611998))))
                                .padding(horizontal=6.dp,vertical=2.dp),
                                horizontalArrangement=Arrangement.spacedBy(4.dp),
                                verticalAlignment=Alignment.CenterVertically) {
                                listOf(
                                    Triple(Tab.HOME,t(hindi,"Home","होम"),Icons.Rounded.Home),
                                    Triple(Tab.TESTS,t(hindi,"Tests","टेस्ट"),Icons.Rounded.Assignment),
                                    Triple(Tab.ANALYTICS,t(hindi,"Analysis","विश्लेषण"),Icons.Rounded.Insights),
                                    Triple(Tab.RANKS,t(hindi,"Rank","रैंक"),Icons.Rounded.EmojiEvents),
                                    Triple(Tab.PROFILE,t(hindi,"More","अन्य"),Icons.Rounded.MoreHoriz)
                                ).forEach { (item,label,icon) ->
                                    val selected=tab==item
                                    val tileColor by animateColorAsState(
                                        if(selected) Gold else Color.Transparent,
                                        animationSpec=tween(190),label="compact nav tile")
                                    Column(Modifier.weight(1f).fillMaxHeight()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(tileColor)
                                        .clickable(role=Role.Tab,onClick={tab=item})
                                        .padding(vertical=3.dp,horizontal=1.dp),
                                        horizontalAlignment=Alignment.CenterHorizontally,
                                        verticalArrangement=Arrangement.Center) {
                                        Icon(icon,contentDescription=label,
                                            tint=if(selected) RoyalDeep else Color.White,
                                            modifier=Modifier.size(20.dp))
                                        if(selected) {
                                            Spacer(Modifier.height(2.dp))
                                            Text(label,color=RoyalDeep,fontWeight=FontWeight.Black,
                                                fontSize=10.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }) { padding ->
                        AnimatedContent(targetState=tab,modifier=Modifier.padding(padding),
                            transitionSpec={
                                (fadeIn(tween(220)) + slideIntoContainer(
                                    if(targetState.ordinal >= initialState.ordinal)
                                        AnimatedContentTransitionScope.SlideDirection.Left
                                    else AnimatedContentTransitionScope.SlideDirection.Right,
                                    tween(250)))
                                    .togetherWith(fadeOut(tween(170)))
                            },label="nav-flow") { currentTab ->
                            when(currentTab) {
                                Tab.HOME -> DashboardScreen(hindi,attempts,activeSession,tests,
                                    catalogStatus,loadingCatalog,unlocked,rewardCredits,adReady,quickReady,
                                    remoteBanner,examEvents,homeCards,Modifier,startTest,unlock,quickUnlock,
                                    onResume={ activeSession?.let { startPaper(it.testId) } },
                                    onViewResult={newlyFinished=false;route=Route.Result(it)},
                                    onToggleLanguage={hindi=!hindi;store.setHindi(hindi)},
                                    onOpenTests={tab=Tab.TESTS},onOpenAnalytics={tab=Tab.ANALYTICS},
                                    onOpenRanks={tab=Tab.RANKS},
                                    onBannerAction={ destination ->
                                        when(destination) {
                                            "insights" -> tab=Tab.ANALYTICS
                                            "ranking" -> tab=Tab.RANKS
                                            "home" -> tab=Tab.HOME
                                            "mock_list" -> tab=Tab.TESTS
                                            "profile" -> tab=Tab.PROFILE
                                        }
                                    })
                                Tab.TESTS -> TestCatalogueScreen(tests,attempts,unlocked,rewardCredits,adReady,quickReady,
                                    loadingCatalog,catalogStatus,hindi,ads.canShowBanner(),ads,
                                    onStart=startTest,onUnlock=unlock,onQuickUnlock=quickUnlock,
                                    onRefresh={refreshCatalogue()})
                                Tab.ANALYTICS -> AnalyticsScreen(attempts,questions,Modifier,hindi,
                                    onViewResult={newlyFinished=false;route=Route.Result(it)},ads=ads)
                                Tab.RANKS -> LeaderboardScreen(board,tests,Modifier,hindi,ads=ads)
                                Tab.PROFILE -> ProfileScreen(name,hindi,attempts.size,Modifier,
                                    notificationsEnabled=notificationsEnabled,onNotifications=toggleNotifications,
                                    onSetName={name=it;store.setName(it)},
                                    onSetHindi={hindi=it;store.setHindi(it)},
                                    onPrivacy={ads.showPrivacyOptions({})},ads=ads,
                                    onDelete={board.deleteMyData { ok ->
                                        if(ok) {
                                            store.clearAll();questions.clearPaperCache();catalog.clearCache()
                                            tests=catalog.cached();ExamConfig.setTitles(tests)
                                            attempts=emptyList();activeSession=null
                                            unlocked=emptySet();rewardCredits=emptyMap();unlockEvent=null;name="Aspirant";hindi=false;tab=Tab.HOME
                                            Toast.makeText(this@MainActivity,"Saved and online data deleted",Toast.LENGTH_LONG).show()
                                        } else Toast.makeText(this@MainActivity,"Cloud deletion failed. Connect and retry.",Toast.LENGTH_LONG).show()
                                    }})
                            }
                        }
                    }
                }
                        }
                    }
                    AnimatedVisibility(visible=unlockEvent!=null,
                        enter=slideInVertically(initialOffsetY={-it})+fadeIn(),
                        exit=slideOutVertically(targetOffsetY={-it})+fadeOut(),
                        modifier=Modifier.align(Alignment.TopCenter)
                            .padding(horizontal=14.dp,vertical=8.dp)) {
                        unlockEvent?.let { event ->
                            UnlockProgressCard(event.first,event.second,event.third,hindi)
                        }
                    }
                    if(splash) ReferenceSplash(hindi)
                }
                if(confirmExitTest && route is Route.Exam) AlertDialog(
                    onDismissRequest={confirmExitTest=false},
                    containerColor=CardInk,
                    icon={Icon(Icons.Rounded.WarningAmber,contentDescription=null,tint=Amber)},
                    title={Text(t(hindi,"Test in progress","टेस्ट अभी चल रहा है"),
                        color=SoftWhite,fontWeight=FontWeight.ExtraBold)},
                    text={Text(t(hindi,
                        "Are you sure you want to exit? Your responses are saved, but the exam timer will continue. You can resume from Home before time runs out.",
                        "क्या आप टेस्ट छोड़ना चाहते हैं? आपके उत्तर सहेजे जाएँगे, लेकिन परीक्षा का टाइमर चलता रहेगा। समय रहते होम से टेस्ट जारी कर सकते हैं।"),
                        color=Muted)},
                    confirmButton={TextButton(onClick={
                        confirmExitTest=false
                        route=Route.Tabs
                        tab=Tab.HOME
                    }) {Text(t(hindi,"Exit & save","बाहर जाएँ और सहेजें"),color=Red,fontWeight=FontWeight.Bold)}},
                    dismissButton={TextButton(onClick={confirmExitTest=false}) {
                        Text(t(hindi,"Keep testing","टेस्ट जारी रखें"),color=Violet,fontWeight=FontWeight.Bold)
                    }})
                preflightId?.let { id ->
                    MockPreflightDialog(id,hindi,
                        onStart={preflightId=null;startPaper(id)},
                        onDismiss={preflightId=null})
                }
                if(busyPaper) AlertDialog(onDismissRequest={},title={Text(t(hindi,"Preparing your next step","आपका अगला कदम तैयार हो रहा है"))},
                    text={MotivationalLoading(hindi,
                        t(hindi,"Downloading and checking your paper…","आपका प्रश्नपत्र तैयार किया जा रहा है…"))},confirmButton={})
                paperError?.let { message ->
                    AlertDialog(onDismissRequest={paperError=null},title={Text(t(hindi,"Mock unavailable","मॉक उपलब्ध नहीं है"))},
                        text={Text(message)},confirmButton={TextButton(onClick={paperError=null}) {Text("OK")} })
                }
                quickIntroId?.takeIf { id ->
                    tests.any {it.id==id && MockAvailability.canWatchReward(it,unlocked)}
                }?.let { id ->
                    val needed = MockAccess.requiredAds(id)
                    val completed = rewardCredits[id] ?: 0
                    AlertDialog(onDismissRequest={quickIntroId=null},
                        title={Text(t(hindi,"Unlock Mock %03d".format(id),
                            "मॉक %03d अनलॉक करें".format(id)))},
                        text={Text(t(hindi,
                            "This mock requires $needed completed rewarded ad(s). Watch ad ${completed+1} of $needed now to earn one credit. Each completed ad saves your progress on this device. Skip without losing previously earned credits.",
                            "इस मॉक के लिए $needed विज्ञापन पूरे करने होंगे। एक क्रेडिट पाने के लिए ${completed+1}/$needed विज्ञापन देखें। क्रेडिट इसी डिवाइस पर सहेजे जाएंगे। छोड़ने पर पहले के क्रेडिट नहीं मिटेंगे।"))},
                        dismissButton={TextButton(onClick={quickIntroId=null}) {Text(t(hindi,"Skip ad","छोड़ें"))}},
                        confirmButton={TextButton(onClick={
                            quickIntroId=null
                            ads.showQuickReward(onEarned={earnedUnlock(id)},
                                onUnavailable=unavailable,onChange=notifyAd)
                        }) {Text(t(hindi,"Watch ad ${completed+1}/$needed","विज्ञापन ${completed+1}/$needed देखें"))} })
                }
            }
        }
    }
}
