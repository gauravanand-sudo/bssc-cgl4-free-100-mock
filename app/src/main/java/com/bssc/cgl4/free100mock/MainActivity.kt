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
        val notificationPrefs = PushPreferences(this)
        ExamPushService.ensureChannel(this)
        setContent {
            RankTheme {
                var tab by remember { mutableStateOf(Tab.HOME) }
                var route by remember { mutableStateOf<Route>(Route.Tabs) }
                var attempts by remember { mutableStateOf(store.attempts()) }
                var activeSession by remember { mutableStateOf(store.session()) }
                var unlocked by remember { mutableStateOf(store.unlocked()) }
                var hindi by remember { mutableStateOf(store.hindi()) }
                var name by remember { mutableStateOf(store.name()) }
                var tests by remember { mutableStateOf(catalog.cached()) }
                var catalogStatus by remember { mutableStateOf<String?>(null) }
                var loadingCatalog by remember { mutableStateOf(false) }
                var busyPaper by remember { mutableStateOf(false) }
                var paperError by remember { mutableStateOf<String?>(null) }
                var quickIntroId by remember { mutableStateOf<Int?>(null) }
                var adReady by remember { mutableStateOf(false) }
                var quickReady by remember { mutableStateOf(false) }
                var newlyFinished by remember { mutableStateOf(false) }
                var confirmExitTest by remember { mutableStateOf(false) }
                var remoteBanners by remember { mutableStateOf<List<PromoBanner>>(emptyList()) }
                var examEvents by remember { mutableStateOf<List<ExamEvent>>(emptyList()) }
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
                    engagement.banners { remoteBanners=it }
                    engagement.events { examEvents=it }
                    catalog.refresh { result, status ->
                        tests = result
                        ExamConfig.setTitles(result)
                        catalogStatus = status
                        loadingCatalog = false
                    }
                }
                LaunchedEffect(Unit) {
                    ExamConfig.setTitles(tests)
                    ads.initialize(notifyAd)
                    refreshCatalogue()
                    delay(1300L)
                    splash=false
                }
                SideEffect { safeHome = route == Route.Tabs && tab == Tab.HOME }
                fun startPaper(id: Int) {
                    val test = tests.firstOrNull { it.id == id }
                        ?: MockTest(id,ExamConfig.title(id),"Saved mock","Balanced",false)
                    // Direct resume must obey the same access gates as the catalogue.
                    if (!MockAccess.isAccessible(id, unlocked)) {
                        paperError = if (MockAccess.isPremium(id))
                            t(hindi,"Premium pack (₹25) is not available yet.","₹25 का प्रीमियम पैक अभी उपलब्ध नहीं है।")
                        else t(hindi,"Unlock this mock with a rewarded ad first.","पहले विज्ञापन देखकर इस मॉक को अनलॉक करें।")
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
                        if (available) {
                            val session = existing ?: ExamSession(id,System.currentTimeMillis())
                            activeSession = session
                            store.saveSession(session)
                            route = Route.Exam(id)
                        } else paperError = error ?: "This paper could not be loaded."
                    }
                }
                val startTest: (Int) -> Unit = { id ->
                    val test = tests.firstOrNull { it.id == id }
                    if (test == null) paperError = "This mock isn't in the current catalogue."
                    else if (MockAccess.isAccessible(id, unlocked)) startPaper(id)
                    else if (MockAccess.isPremium(id))
                        Toast.makeText(this@MainActivity,
                            t(hindi,"₹25 Premium Pack coming soon","₹25 प्रीमियम पैक जल्द आएगा"),Toast.LENGTH_LONG).show()
                    else Toast.makeText(this@MainActivity,
                        t(hindi,"Watch a rewarded ad to unlock","विज्ञापन देखकर अनलॉक करें"),Toast.LENGTH_SHORT).show()
                }
                val earnedUnlock: (Int) -> Unit = { id ->
                    if (MockAccess.isRewarded(id)) {
                        store.unlock(id); unlocked = store.unlocked()
                        Toast.makeText(this@MainActivity,"Mock %03d unlocked!".format(id),Toast.LENGTH_LONG).show()
                    }
                }
                val unavailable = {
                    Toast.makeText(this,"No ad available. Please try again shortly.",Toast.LENGTH_LONG).show()
                }
                val unlock: (Int) -> Unit = { id ->
                    if (MockAccess.isRewarded(id) && id !in unlocked) {
                        ads.showRewarded(onEarned={earnedUnlock(id)},onUnavailable=unavailable,onChange=notifyAd)
                    }
                }
                val quickUnlock: (Int) -> Unit = { id ->
                    if (MockAccess.isRewarded(id) && id !in unlocked) quickIntroId = id
                }
                // The system Back button must not leave an ongoing exam without confirmation.
                BackHandler(enabled = route is Route.Exam) { confirmExitTest = true }
                BackHandler(enabled = route is Route.Result) { route = Route.Tabs; tab = Tab.HOME }
                // Include cutouts, system status and gesture navigation insets on Android 15+.
                Box(Modifier.fillMaxSize().background(RoyalDeep).safeDrawingPadding()) {
                    Column(Modifier.fillMaxSize().background(Ink)) {
                        GlobalExamHeader(hindi,onLanguage={hindi=!hindi;store.setHindi(hindi)},
                            notificationsEnabled=notificationsEnabled,
                            showNotifications=route == Route.Tabs,
                            onNotifications=toggleNotifications)
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
                            onAnalytics={route=Route.Tabs;tab=Tab.ANALYTICS;newlyFinished=false})
                    }
                    Route.Tabs -> Scaffold(containerColor=Ink,
                        bottomBar={
                            Row(Modifier.fillMaxWidth().background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(RoyalDeep,Royal,Color(0xFF521079))))
                                .padding(horizontal=9.dp,vertical=9.dp),
                                horizontalArrangement=Arrangement.spacedBy(7.dp),
                                verticalAlignment=Alignment.CenterVertically) {
                                listOf(
                                    Triple(Tab.HOME,t(hindi,"Home","होम"),Icons.Rounded.Home),
                                    Triple(Tab.TESTS,t(hindi,"Tests","टेस्ट"),Icons.Rounded.Assignment),
                                    Triple(Tab.ANALYTICS,t(hindi,"Analysis","विश्लेषण"),Icons.Rounded.Insights),
                                    Triple(Tab.RANKS,t(hindi,"Rank","रैंक"),Icons.Rounded.EmojiEvents),
                                    Triple(Tab.PROFILE,t(hindi,"More","अन्य"),Icons.Rounded.MoreHoriz)
                                ).forEach { (item,label,icon) ->
                                    val selected=tab==item
                                    val tileColor by animateColorAsState(if(selected) Gold else Color.White.copy(alpha=.11f),
                                        animationSpec=tween(230),label="tab tile")
                                    val outlineColor by animateColorAsState(if(selected) Gold else Color.White.copy(alpha=.24f),
                                        animationSpec=tween(230),label="tab outline")
                                    val tileScale by animateFloatAsState(if(selected) 1f else .97f,
                                        animationSpec=tween(230),label="tab tap")
                                    Column(Modifier.weight(1f).height(65.dp)
                                        .graphicsLayer { scaleX=tileScale; scaleY=tileScale }
                                        .clip(RoundedCornerShape(15.dp))
                                        .background(tileColor)
                                        .border(1.dp,outlineColor,RoundedCornerShape(15.dp))
                                        .clickable(role=Role.Tab,onClick={tab=item})
                                        .padding(vertical=7.dp,horizontal=2.dp),
                                        horizontalAlignment=Alignment.CenterHorizontally,
                                        verticalArrangement=Arrangement.Center) {
                                        Icon(icon,contentDescription=label,tint=if(selected) RoyalDeep else Color.White,
                                            modifier=Modifier.size(22.dp))
                                        Spacer(Modifier.height(3.dp))
                                        Text(label,color=if(selected) RoyalDeep else Color.White,
                                            fontWeight=if(selected) FontWeight.Black else FontWeight.SemiBold,
                                            fontSize=10.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
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
                                Tab.HOME -> DashboardScreen(name,hindi,attempts,activeSession,tests,
                                    catalogStatus,loadingCatalog,unlocked,adReady,quickReady,
                                    ads.canShowBanner(),ads,remoteBanners,examEvents,Modifier,startTest,unlock,quickUnlock,
                                    onResume={ activeSession?.let { startPaper(it.testId) } },
                                    onViewResult={newlyFinished=false;route=Route.Result(it)},
                                    onToggleLanguage={hindi=!hindi;store.setHindi(hindi)},
                                    onRefresh={ refreshCatalogue() },
                                    onOpenTests={tab=Tab.TESTS},onOpenAnalytics={tab=Tab.ANALYTICS},
                                    onOpenRanks={tab=Tab.RANKS},
                                    onBannerAction={ destination ->
                                        when(destination) {
                                            "insights" -> tab=Tab.ANALYTICS
                                            "ranking" -> tab=Tab.RANKS
                                            "home", "mock_list" -> tab=Tab.HOME
                                        }
                                    })
                                Tab.TESTS -> TestCatalogueScreen(tests,attempts,unlocked,adReady,quickReady,
                                    loadingCatalog,catalogStatus,hindi,ads.canShowBanner(),ads,
                                    onStart=startTest,onUnlock=unlock,onQuickUnlock=quickUnlock,
                                    onRefresh={refreshCatalogue()})
                                Tab.ANALYTICS -> AnalyticsScreen(attempts,questions,Modifier,hindi,
                                    onViewResult={newlyFinished=false;route=Route.Result(it)})
                                Tab.RANKS -> LeaderboardScreen(board,tests,Modifier,hindi)
                                Tab.PROFILE -> ProfileScreen(name,hindi,attempts.size,Modifier,
                                    notificationsEnabled=notificationsEnabled,onNotifications=toggleNotifications,
                                    onSetName={name=it;store.setName(it)},
                                    onSetHindi={hindi=it;store.setHindi(it)},
                                    onPrivacy={ads.showPrivacyOptions({})},
                                    onDelete={board.deleteMyData { ok ->
                                        if(ok) {
                                            store.clearAll();questions.clearPaperCache();catalog.clearCache()
                                            tests=catalog.cached();ExamConfig.setTitles(tests)
                                            attempts=emptyList();activeSession=null
                                            unlocked=emptySet();name="Aspirant";hindi=false;tab=Tab.HOME
                                            Toast.makeText(this@MainActivity,"Saved and online data deleted",Toast.LENGTH_LONG).show()
                                        } else Toast.makeText(this@MainActivity,"Cloud deletion failed. Connect and retry.",Toast.LENGTH_LONG).show()
                                    }})
                            }
                        }
                    }
                }
                        }
                        if(route !is Route.Exam) BiharLoveFooter(hindi)
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
                if(busyPaper) AlertDialog(onDismissRequest={},title={Text(t(hindi,"Preparing your next step","आपका अगला कदम तैयार हो रहा है"))},
                    text={MotivationalLoading(hindi,
                        t(hindi,"Downloading and checking your paper…","आपका प्रश्नपत्र तैयार किया जा रहा है…"))},confirmButton={})
                paperError?.let { message ->
                    AlertDialog(onDismissRequest={paperError=null},title={Text(t(hindi,"Mock unavailable","मॉक उपलब्ध नहीं है"))},
                        text={Text(message)},confirmButton={TextButton(onClick={paperError=null}) {Text("OK")} })
                }
                quickIntroId?.takeIf { MockAccess.isRewarded(it) }?.let { id ->
                    AlertDialog(onDismissRequest={quickIntroId=null},
                        title={Text("Unlock Mock %03d".format(id))},
                        text={Text("Watch a rewarded quick ad to unlock this mock permanently. You can skip and stay on this screen without losing anything.")},
                        dismissButton={TextButton(onClick={quickIntroId=null}) {Text("Skip ad")}},
                        confirmButton={TextButton(onClick={
                            quickIntroId=null
                            ads.showQuickReward(onEarned={earnedUnlock(id)},
                                onUnavailable=unavailable,onChange=notifyAd)
                        }) {Text("Watch & unlock")} })
                }
            }
        }
    }
}
