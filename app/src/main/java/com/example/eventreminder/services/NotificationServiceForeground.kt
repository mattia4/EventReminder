package com.example.eventreminder.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.eventreminder.R
import com.example.eventreminder.activities.MainActivity
import com.example.eventreminder.ws.FirebaseGetEventAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationServiceForeground : Service() {
    private val CHANNEL_ID = "ForegroundService Kotlin"

    companion object {
        fun startService(context: Context, message: String) {
            val startIntent = Intent(context, NotificationServiceForeground::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, NotificationServiceForeground::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            // foreground method below
            val ha = Handler(Looper.getMainLooper())
            ha.postDelayed(object : Runnable {
                override fun run() {
                    Thread {
                        FirebaseGetEventAll().getDataForNotification(baseContext)
                        ha.postDelayed(this, 1 * 60 * 1000)
                    }.start()
                }

            }, 1 * 60 * 1000)
            // stopForeground(true)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //do heavy work on a background thread
        val input = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            1, notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Notifications service")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_notification_add)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        //stopSelf();
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
}
