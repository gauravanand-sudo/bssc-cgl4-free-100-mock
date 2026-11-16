package com.bssc.cgl4.free100mock

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Question(
    val id: String,
    val section: String,
    val topic: String,
    val questionEn: String,
    val questionHi: String,
    val optionsEn: List<String>,
    val optionsHi: List<String>,
    val correctIndex: Int,
    val explanationEn: String,
    val explanationHi: String,
)

data class ExamSession(
    val testId: Int,
    val startedAt: Long,
    val answers: List<Int> = List(150) { -1 },
    val marked: Set<Int> = emptySet(),
    val currentIndex: Int = 0,
)

data class Attempt(
    val testId: Int,
    val completedAt: Long,
    val elapsedSeconds: Int,
    val answers: List<Int>,
    val score: Int,
    val correct: Int,
    val wrong: Int,
    val skipped: Int,
)

data class TopicPerformance(val topic: String, val section: String, val correct: Int, val wrong: Int, val skipped: Int) {
    val attempted: Int get() = correct + wrong
    val accuracy: Float get() = if (attempted == 0) 0f else correct.toFloat() / attempted
}

object ExamConfig {
    const val QUESTION_COUNT = 150
    const val DURATION_SECONDS = 135 * 60
    const val MAX_SCORE = 600
    val subjects = listOf("General Studies", "Science & Maths", "Reasoning")
    private val customTitles = mutableMapOf<Int, String>()
    fun setTitles(tests: List<MockTest>) { tests.forEach { customTitles[it.id] = it.title } }
    fun title(testId: Int) = customTitles[testId] ?: "Mock Test %02d".format(Locale.ROOT, testId)
}

class QuestionRepository(private val context: Context) {
    private val cache = mutableMapOf<Int, List<Question>>()
    private val folder = java.io.File(context.filesDir, "cached_papers").apply { mkdirs() }
    private fun localFile(id: Int) = java.io.File(folder, "mock_%03d.json".format(Locale.ROOT,id))
    private fun parse(raw: String): List<Question> {
        val arr = JSONArray(raw)
        require(arr.length() == ExamConfig.QUESTION_COUNT) { "A mock must have exactly 150 questions" }
        return List(arr.length()) { i ->
            val q = arr.getJSONObject(i)
            fun array(key: String): List<String> = q.getJSONArray(key).let { a -> List(a.length()) { a.getString(it) } }
            val answers = array("optionsEn")
            require(answers.size == 4 && q.getInt("correctIndex") in 0..3)
            Question(q.getString("id"),q.getString("section"),q.getString("topic"),
                q.getString("questionEn"),q.getString("questionHi"), answers,
                array("optionsHi"),q.getInt("correctIndex"),
                q.getString("explanationEn"),q.getString("explanationHi"))
        }
    }
    fun getOrNull(testId: Int): List<Question>? {
        cache[testId]?.let { return it }
        val file = localFile(testId)
        val raw = when {
            file.exists() -> file.readText()
            testId in 1..4 -> context.assets.open("mock_%02d.json".format(Locale.ROOT, testId))
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            else -> return null
        }
        return try { parse(raw).also { cache[testId] = it } } catch (_: Exception) { null }
    }
    fun get(testId: Int) = requireNotNull(getOrNull(testId)) { "Paper not downloaded: $testId" }
    fun clearPaperCache() { cache.clear(); folder.listFiles()?.forEach { it.delete() }
        context.getSharedPreferences("paper_versions", Context.MODE_PRIVATE).edit().clear().apply() }
    /** Callback on main thread. Firestore cached paper documents can also work offline. */
    fun load(test: MockTest, onResult: (Boolean, String?) -> Unit) {
        val prefs=context.getSharedPreferences("paper_versions",Context.MODE_PRIVATE)
        val versionOk = prefs.getInt("version_${test.id}",-1) == test.version
        if (versionOk && getOrNull(test.id) != null) { onResult(true,null); return }
        CloudBackend.withIdentity(context,ready={ db ->
            db.collection("mock_papers").document("mock_%03d".format(Locale.ROOT,test.id))
                .get().addOnSuccessListener { doc ->
                    val raw=doc.getString("questionsJson")
                    try {
                        require(!raw.isNullOrBlank())
                        val paper=parse(raw)
                        val file=localFile(test.id)
                        file.writeText(raw)
                        cache[test.id]=paper
                        prefs.edit().putInt("version_${test.id}",test.version).apply()
                        onResult(true,null)
                    } catch (_: Exception) {
                        onResult(getOrNull(test.id)!=null,"Paper not published or invalid. Try another mock.")
                    }
                }.addOnFailureListener {
                    onResult(getOrNull(test.id)!=null,"Can't download this paper. Please reconnect and try again.")
                }
        },failed={ message -> onResult(getOrNull(test.id)!=null,message) })
    }
}

object Grading {
    fun grade(testId: Int, startedAt: Long, answers: List<Int>, questions: List<Question>, now: Long = System.currentTimeMillis()): Attempt {
        require(questions.size == ExamConfig.QUESTION_COUNT && answers.size == questions.size)
        val correct = questions.indices.count { answers[it] == questions[it].correctIndex }
        val wrong = questions.indices.count { answers[it] != -1 && answers[it] != questions[it].correctIndex }
        val skipped = questions.size - correct - wrong
        return Attempt(testId, now, ((now - startedAt) / 1000).toInt().coerceIn(0, ExamConfig.DURATION_SECONDS), answers,
            correct * 4 - wrong, correct, wrong, skipped)
    }
    fun breakdown(attempts: List<Attempt>, repository: QuestionRepository): List<TopicPerformance> {
        data class C(var correct: Int = 0, var wrong: Int = 0, var skipped: Int = 0, val section: String)
        val stats = linkedMapOf<String, C>()
        attempts.forEach { a ->
            repository.getOrNull(a.testId)?.forEachIndexed { i, q ->
                val key = q.section + " | " + q.topic
                val c = stats.getOrPut(key) { C(section = q.section) }
                when (a.answers.getOrElse(i) { -1 }) {
                    -1 -> c.skipped++
                    q.correctIndex -> c.correct++
                    else -> c.wrong++
                }
            }
        }
        return stats.map { (key, c) -> TopicPerformance(key.substringAfter(" | "), c.section, c.correct, c.wrong, c.skipped) }
            .sortedWith(compareBy<TopicPerformance> { it.accuracy }.thenByDescending { it.attempted })
    }
    fun subjectBreakdown(attempts: List<Attempt>, repo: QuestionRepository): List<TopicPerformance> =
        ExamConfig.subjects.map { name ->
            val list = breakdown(attempts, repo).filter { it.section == name }
            TopicPerformance(name, name, list.sumOf { it.correct }, list.sumOf { it.wrong }, list.sumOf { it.skipped })
        }
}

class AppStore(context: Context) {
    private val prefs = context.getSharedPreferences("ranksetu_v1", Context.MODE_PRIVATE)
    fun name() = prefs.getString("name", "Aspirant") ?: "Aspirant"
    fun setName(name: String) = prefs.edit().putString("name", name.take(28).trim().ifBlank { "Aspirant" }).apply()
    fun hindi() = prefs.getBoolean("hindi", false)
    fun setHindi(value: Boolean) = prefs.edit().putBoolean("hindi", value).apply()
    fun unlocked() = prefs.getStringSet("unlocked", emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()

    /** Earned ads already watched for each mock, persisted across app restarts.
     *  Do not grant these credits for a dismissed/failed ad: call ONLY from
     *  the Google onUserEarnedReward callback.
     */
    fun rewardCredits(): Map<Int, Int> = (3..100).mapNotNull { id ->
        val credit = prefs.getInt("ad_credit_$id", 0)
            .coerceIn(0, MockAccess.requiredAds(id))
        if (credit > 0 && id !in unlocked()) id to credit else null
    }.toMap()

    @Synchronized fun recordEarnedReward(testId: Int): Int {
        val needed = MockAccess.requiredAds(testId)
        if (needed == 0) return 0
        // Existing unlocks, including those earned in previous versions, remain valid.
        val priorUnlocks = unlocked()
        if (testId in priorUnlocks) return needed
        val credit = prefs.getInt("ad_credit_$testId", 0)
        val next = MockAccess.nextCredit(testId, credit)
        val editor = prefs.edit().putInt("ad_credit_$testId", next)
        if (next >= needed) {
            editor.putStringSet("unlocked", (priorUnlocks + testId).map(Int::toString).toSet())
        }
        editor.apply()
        return next
    }
    fun session(): ExamSession? { return try {
        val raw = prefs.getString("session", null) ?: return null
        val j = JSONObject(raw)
        ExamSession(j.getInt("id"), j.getLong("start"),
            j.getJSONArray("answers").let { a -> List(a.length()) { a.getInt(it) } },
            j.getJSONArray("marked").let { a -> (0 until a.length()).map { a.getInt(it) }.toSet() },
            j.optInt("current", 0))
    } catch (_: Exception) { null } }
    fun saveSession(s: ExamSession?) {
        if (s == null) { prefs.edit().remove("session").apply(); return }
        val j = JSONObject().put("id", s.testId).put("start", s.startedAt)
            .put("answers", JSONArray(s.answers)).put("marked", JSONArray(s.marked.toList())).put("current", s.currentIndex)
        prefs.edit().putString("session", j.toString()).apply()
    }
    fun attempts(): List<Attempt> = try {
        val arr = JSONArray(prefs.getString("attempts", "[]"))
        (0 until arr.length()).map { i ->
            val j = arr.getJSONObject(i)
            Attempt(j.getInt("id"), j.getLong("when"), j.getInt("duration"),
                j.getJSONArray("answers").let { a -> List(a.length()) { a.getInt(it) } },
                j.getInt("score"), j.getInt("correct"), j.getInt("wrong"), j.getInt("skipped"))
        }.sortedByDescending { it.completedAt }
    } catch (_: Exception) { emptyList() }
    fun clearAll() { prefs.edit().clear().apply() }
    fun addAttempt(a: Attempt) {
        val arr = JSONArray()
        (listOf(a) + attempts()).take(100).forEach { x ->
            arr.put(JSONObject().put("id", x.testId).put("when", x.completedAt).put("duration", x.elapsedSeconds)
                .put("answers", JSONArray(x.answers)).put("score", x.score).put("correct", x.correct)
                .put("wrong", x.wrong).put("skipped", x.skipped))
        }
        prefs.edit().putString("attempts", arr.toString()).apply()
        saveSession(null)
    }
}

fun formatDate(time: Long): String = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(time))
fun formatDuration(seconds: Int): String = "%02d:%02d:%02d".format(Locale.ROOT, seconds / 3600, seconds / 60 % 60, seconds % 60)
