package com.bssc.cgl4.free100mock

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/** Optional online leaderboard. No made-up ranks or seeded competitors. */
data class LeaderRow(val uid: String, val name: String, val score: Int, val correct: Int, val submittedAt: Long)

class LeaderboardRepository(context: Context) {
    private var auth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    init {
        val app = CloudBackend.app(context)
        if (app != null) {
            auth = FirebaseAuth.getInstance(app)
            db = FirebaseFirestore.getInstance(app)
            if (auth?.currentUser == null) auth?.signInAnonymously()
        }
    }
    fun isConfigured() = db != null
    fun currentUid() = auth?.currentUser?.uid
    private fun authenticated(onReady: (String) -> Unit) {
        val auth = auth ?: return
        auth.currentUser?.uid?.let { onReady(it); return }
        auth.signInAnonymously().addOnSuccessListener { result ->
            result.user?.uid?.let(onReady)
        }
    }
    fun submit(a: Attempt, displayName: String) {
        val db = db ?: return
        authenticated { uid ->
            val doc = db.collection("leaderboard_mock_${a.testId}").document(uid)
            doc.get().addOnSuccessListener { existing ->
                val previous = existing.getLong("score")?.toInt() ?: Int.MIN_VALUE
                // Keep the user's best score per mock.
                if (a.score > previous) {
                    doc.set(mapOf("uid" to uid, "name" to displayName.take(28),
                        "score" to a.score, "correct" to a.correct,
                        "submittedAt" to a.completedAt))
                }
            }
        }
    }
    fun load(testId: Int, onResult: (List<LeaderRow>?, String?) -> Unit) {
        val db = db ?: run { onResult(null, "Connect Firebase to enable real rankings."); return }
        db.collection("leaderboard_mock_$testId").orderBy("score", Query.Direction.DESCENDING)
            .limit(100).get().addOnSuccessListener { snap ->
                onResult(snap.documents.map { d ->
                    LeaderRow(d.id, d.getString("name") ?: "Aspirant",
                        d.getLong("score")?.toInt() ?: 0,
                        d.getLong("correct")?.toInt() ?: 0,
                        d.getLong("submittedAt") ?: 0L)
                }, null)
            }.addOnFailureListener { onResult(null, "Rankings unavailable. Check your connection or Firebase rules.") }
    }
    /** Remove this anonymous identity's public rows before deleting local data. */
    fun deleteMyData(onComplete: (Boolean) -> Unit) {
        val firestore = db
        val login = auth
        if (firestore == null || login == null) { onComplete(true); return }
        val user = login.currentUser ?: run { onComplete(false); return }
        val batch = firestore.batch()
        (1..100).forEach { id -> batch.delete(firestore.collection("leaderboard_mock_$id").document(user.uid)) }
        batch.delete(firestore.collection("student_profiles").document(user.uid))
        batch.commit().addOnSuccessListener {
            user.delete().addOnCompleteListener { onComplete(it.isSuccessful) }
        }.addOnFailureListener { onComplete(false) }
    }

}
