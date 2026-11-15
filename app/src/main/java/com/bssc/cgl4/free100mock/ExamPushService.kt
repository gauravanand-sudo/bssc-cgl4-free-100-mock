package com.bssc.cgl4.free100mock

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/** FCM topic delivery without signup. No registration token is stored in Firestore. */
class ExamPushService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val preferences=getSharedPreferences("notifications_v1", Context.MODE_PRIVATE)
        if(!preferences.getBoolean("enabled",false)) return
        if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) return
        val title=(message.notification?.title ?: message.data["title"] ?: "BSSC CGL 4 Free 100 Mock").take(100)
        val body=(message.notification?.body ?: message.data["body"] ?: "New exam preparation update").take(320)
        ensureChannel(this)
        val intent=Intent(this,MainActivity::class.java).apply { flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
        val pi=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification=NotificationCompat.Builder(this,CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification).setContentTitle(title).setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)).setContentIntent(pi)
            .setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify((System.currentTimeMillis()%Int.MAX_VALUE).toInt(),notification)
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Firebase manages topic subscriptions. Never log or publish a registration token.
        if(getSharedPreferences("notifications_v1",Context.MODE_PRIVATE).getBoolean("enabled",false)) {
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
        }
    }
    companion object {
        const val TOPIC = "bssc_cgl4_updates"
        const val CHANNEL_ID="exam_updates"
        fun ensureChannel(context:Context) {
            if(Build.VERSION.SDK_INT>=26) {
                val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(NotificationChannel(CHANNEL_ID,"Exam and mock updates",NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description="Official exam notices and new practice tests from this independent app" })
            }
        }
    }
}

class PushPreferences(private val context: Context) {
    private val settings=context.getSharedPreferences("notifications_v1",Context.MODE_PRIVATE)
    fun enabled(): Boolean=settings.getBoolean("enabled",false)
    fun change(on:Boolean,finished:(Boolean)->Unit) {
        val firebase=CloudBackend.app(context)
        if(firebase==null && on) { finished(false); return }
        settings.edit().putBoolean("enabled",on).apply()
        if(firebase==null) {finished(true);return}
        val messaging=FirebaseMessaging.getInstance()
        messaging.isAutoInitEnabled=on
        if(on) messaging.subscribeToTopic(ExamPushService.TOPIC)
            .addOnCompleteListener { task ->
                if(!task.isSuccessful) settings.edit().putBoolean("enabled",false).apply()
                finished(task.isSuccessful)
            }
        else messaging.unsubscribeFromTopic(ExamPushService.TOPIC).addOnCompleteListener {
            messaging.deleteToken(); finished(true)
        }
    }
}
