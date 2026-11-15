package com.bssc.cgl4.free100mock

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONArray
import org.json.JSONObject

/** No registration page: Firebase creates a silent anonymous identity for protected reads/rankings. */
object CloudBackend {
    private var instance: FirebaseApp? = null
    fun configured() = BuildConfig.FIREBASE_API_KEY.isNotBlank() && BuildConfig.FIREBASE_APP_ID.isNotBlank() &&
        BuildConfig.FIREBASE_PROJECT_ID.isNotBlank()
    @Synchronized fun app(context: Context): FirebaseApp? {
        if (!configured()) return null
        if (instance != null) return instance
        return try {
            instance = FirebaseApp.getApps(context).firstOrNull { it.name == "[DEFAULT]" }
                ?: FirebaseApp.initializeApp(context.applicationContext,
                    FirebaseOptions.Builder().setApplicationId(BuildConfig.FIREBASE_APP_ID)
                        .setApiKey(BuildConfig.FIREBASE_API_KEY)
                        .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                        .setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID).build())
            instance
        } catch (_: Exception) { null }
    }
    fun withIdentity(context: Context, ready: (FirebaseFirestore) -> Unit, failed: (String) -> Unit) {
        val app = app(context) ?: run { failed("Connect Firebase to see new online mocks."); return }
        val auth = FirebaseAuth.getInstance(app)
        val db = FirebaseFirestore.getInstance(app)
        if (auth.currentUser != null) { ready(db); return }
        auth.signInAnonymously().addOnSuccessListener { ready(db) }
            .addOnFailureListener { failed("Could not connect to the test library. Check your internet connection.") }
    }
}

data class MockTest(
    val id: Int, val title: String, val subtitle: String, val difficulty: String,
    val isFree: Boolean, val version: Int = 1, val questionCount: Int = 150,
    val durationMinutes: Int = 135
)

/** Full list is data-driven; the four bundled mocks are an offline starter, not fabricated extra papers. */
class CatalogRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("mock_catalog_cache_v1", Context.MODE_PRIVATE)
    private val bundled = listOf(
        MockTest(1, "The Foundation", "Build exam-day confidence", "Balanced", true),
        MockTest(2, "Bihar Special", "State-focused practice", "Intermediate", true),
        MockTest(3, "Speed & Accuracy", "Sharpen your solving speed", "Advanced", false),
        MockTest(4, "The Grand Mock", "Realistic full-length challenge", "Advanced", false)
    )
    fun cached(): List<MockTest> = try {
        val arr = JSONArray(prefs.getString("catalog", ""))
        (0 until arr.length()).map { i -> val o = arr.getJSONObject(i)
            MockTest(o.getInt("id"), o.getString("title"), o.optString("subtitle"),
                o.optString("difficulty", "Balanced"), o.optBoolean("isFree", false),
                o.optInt("version", 1), o.optInt("questionCount", 150), o.optInt("durationMinutes", 135))
        }.filter { it.id in 1..100 && it.questionCount == 150 && it.durationMinutes == 135 }.ifEmpty { bundled }
    } catch (_: Exception) { bundled }

    fun clearCache() { prefs.edit().clear().apply() }
    fun refresh(onLoaded: (List<MockTest>, String?) -> Unit) {
        CloudBackend.withIdentity(context, ready = { db ->
            db.collection("mock_catalog").whereEqualTo("published", true)
                .orderBy("sortOrder", Query.Direction.ASCENDING)
                .limit(100).get().addOnSuccessListener { result ->
                    val tests = result.documents.mapNotNull { d -> try {
                        val id = (d.getLong("testId") ?: return@mapNotNull null).toInt()
                        val count = (d.getLong("questionCount") ?: 150L).toInt()
                        val minutes = (d.getLong("durationMinutes") ?: 135L).toInt()
                        if (id !in 1..100 || count != 150 || minutes != 135) return@mapNotNull null
                        MockTest(id, d.getString("title") ?: "Mock $id",
                            d.getString("subtitle") ?: "Full-length prelims test",
                            d.getString("difficulty") ?: "Balanced", d.getBoolean("isFree") == true,
                            (d.getLong("version") ?: 1L).toInt(), count, minutes)
                    } catch (_: Exception) { null } }.distinctBy { it.id }
                    if (tests.isNotEmpty()) {
                        val array = JSONArray()
                        tests.forEach { t -> array.put(JSONObject()
                            .put("id", t.id).put("title", t.title).put("subtitle", t.subtitle)
                            .put("difficulty", t.difficulty).put("isFree", t.isFree)
                            .put("version", t.version).put("questionCount", t.questionCount)
                            .put("durationMinutes", t.durationMinutes)) }
                        prefs.edit().putString("catalog", array.toString()).apply()
                        onLoaded(tests, null)
                    } else onLoaded(cached(), "No published online mocks yet. Showing offline starter tests.")
                }.addOnFailureListener { onLoaded(cached(), "Offline mode: showing saved tests.") }
        }, failed = { onLoaded(cached(), it) })
    }
}
