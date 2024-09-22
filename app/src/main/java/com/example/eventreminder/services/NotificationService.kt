package com.example.eventreminder.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_LIGHTS
import androidx.core.content.ContextCompat.getSystemService
import com.example.eventreminder.Constants.Companion.HAPTIC_FEEDBACK_DURATION
import com.example.eventreminder.Constants.Companion.NOTIFY_CHANNEL_ID
import com.example.eventreminder.Constants.Companion.SHORT_HAPTIC_FEEDBACK_DURATION
import com.example.eventreminder.R
import com.example.eventreminder.activities.EventListActivity
import com.example.eventreminder.activities.MainActivity
import java.util.*
import java.util.concurrent.TimeUnit


class NotificationService (private val context: Context?) {
    private val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var tts: TextToSpeech? = null

    fun showNotification(title: String, description: String) {
        val activityIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 1 /* Request code */, activityIntent,
           if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notifyIntent = PendingIntent.getBroadcast(
            context,2, Intent(context, NotificationReceiver::class.java),
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        val notification = NotificationCompat.Builder(context!!, NOTIFY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message_24)
            .setContentTitle(title)
            .setContentText(description)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_arrow_back_24, "shutDownNotify", notifyIntent)
            .build()

        notificationManager.notify(1, notification)
    }

    fun showMultipleNotification(title: String, description: String, idNotification: Int, group: String) {

        val activityIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 1, activityIntent,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        val notifyIntent = PendingIntent.getBroadcast(
            context,2, Intent(context, NotificationReceiver::class.java),
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notification = NotificationCompat.Builder(context!!, NOTIFY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message_24)
            .setContentTitle(title)
            .setContentText(description)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setDefaults(DEFAULT_LIGHTS)
            .setLights(0xff00ff00.toInt(), 300, 100)
            .setLights(Color.parseColor("#039be5"), 500, 500)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setGroup(group)
            .addAction(R.drawable.ic_arrow_back_24, "Close event", notifyIntent)
            if(idNotification == 0) {
                notification = notification
                    .setGroupSummary(true)
            }

        val vibrator = getSystemService(context, Vibrator::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(HAPTIC_FEEDBACK_DURATION, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator?.vibrate(vibrationEffect)
        } else {
            vibrator?.vibrate(TimeUnit.MILLISECONDS.toMillis(SHORT_HAPTIC_FEEDBACK_DURATION))
        }
        notificationManager.notify(idNotification, notification.build())
    }

    fun showMultipleNotificationBigText(title: String, description: String, idNotification: Int, group: String) {
        var mBuilder = NotificationCompat.Builder(context!!.applicationContext, "notify")
        val ii = Intent(context.applicationContext, EventListActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 2, ii, 0)

        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText(description)
        bigText.setBigContentTitle(title)
        bigText.setSummaryText("notifications")

        mBuilder.setContentIntent(pendingIntent)
        mBuilder.setSmallIcon(R.drawable.ic_message_24)
        mBuilder.setContentTitle(title)
        mBuilder.setContentText(description)
        mBuilder.priority = Notification.PRIORITY_MAX
        mBuilder.setGroup(group)
        if(idNotification == 0) {
            mBuilder = mBuilder.setGroupSummary(true)
        }
        mBuilder.setStyle(bigText)

        val mNotificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "notify"
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            mNotificationManager.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }
        val vibrator = getSystemService(context, Vibrator::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(HAPTIC_FEEDBACK_DURATION, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator?.vibrate(vibrationEffect)
        } else {
            vibrator?.vibrate(TimeUnit.MILLISECONDS.toMillis(SHORT_HAPTIC_FEEDBACK_DURATION))
        }
        mNotificationManager.notify(idNotification, mBuilder.build())
        tts = TextToSpeech(context, TextToSpeech.OnInitListener {
            tts?.language = Locale.ITALIAN
            tts?.speak(description, TextToSpeech.QUEUE_FLUSH, null);
        })
    }
}