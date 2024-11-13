package com.example.personalmassenger

import Utils.FirebaseUtil
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random

class FCMNotificationService : FirebaseMessagingService() {
    private val channelID = "class-update"
    private val channelName = "class-updates"
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if(message.data["title"]==FirebaseUtil.currentUserEmail()){
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        val mainIntent = Intent(this,MainActivity::class.java)
        val mainPendingIntent:PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(mainIntent)
            getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelID)
            .setSmallIcon(IconCompat.createWithResource(applicationContext, R.mipmap.ic_launcher_foreground))
            .setColor(applicationContext.getColor(R.color.black))
            .setColorized(true)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setBadgeIconType(R.mipmap.ic_launcher_foreground)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(mainPendingIntent)
           // .setLights(ContextCompat.getColor(applicationContext, R.color.black), 5000, 5000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            with(NotificationManagerCompat.from(applicationContext)) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                notify(Random().nextInt(3000), builder.build())

            }
        } else {
            NotificationManagerCompat.from(applicationContext)
                .notify(Random().nextInt(3000), builder.build())
        }
//        if (message.notification != null) {
//            Log.d("Push Notification", message.data.toString())
//            pushNotification(
//                message.notification?.title.toString(),
//                message.notification?.body.toString()
//            )
//        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel= NotificationChannel(channelID,channelName,NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onNewToken(token: String) {
        Log.d("token", "Refreshed token: $token")
        super.onNewToken(token)
    }

    private fun pushNotification(title: String, body: String) {

    }

}