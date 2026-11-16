package com.bssc.cgl4.free100mock

import android.content.Context
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges

/** Admin-only Firestore document: app_config/monetization.
 *  A missing document means rewarded for 50–100. Only server-confirmed
 *  values are trusted. Local cache cannot turn premium papers into ads.
 */
class MonetizationRepository(private val context: Context) {
    fun observe(onUpdate: (MockAccess.Mode) -> Unit): () -> Unit {
        var registration: ListenerRegistration? = null
        var disposed = false
        CloudBackend.withIdentity(context, ready = { db ->
            if (!disposed) {
                registration = db.collection("app_config").document("monetization")
                    .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                        if (!disposed) {
                            if (error != null || snapshot == null || snapshot.metadata.isFromCache) {
                                onUpdate(MockAccess.Mode())
                            } else if (!snapshot.exists()) {
                                onUpdate(MockAccess.Mode(premiumEnabled = false, verified = true))
                            } else {
                                val flag = snapshot.getBoolean("premiumEnabled")
                                onUpdate(if (flag == null) MockAccess.Mode()
                                    else MockAccess.Mode(premiumEnabled = flag, verified = true))
                            }
                        }
                    }
            }
        }, failed = { if (!disposed) onUpdate(MockAccess.Mode()) })
        return {
            disposed = true
            registration?.remove()
        }
    }
}
