package com.devinfusion.eventiafinal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.notification!=null){
            pushNotification(remoteMessage.notification!!.title!!,remoteMessage.notification!!.body!!)
        }
    }

    private fun pushNotification(title: String, message: String) {

        val notificationManager : NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel_id : String = "General"
        val notification : Notification

        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pi : PendingIntent = PendingIntent.getActivity(this,100,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val name = "Events Channel"
            val description = "Channel for events update"
            val importance : Int = NotificationManager.IMPORTANCE_HIGH
            val channel : NotificationChannel = NotificationChannel(channel_id,name,importance)
            channel.description = description

            if (notificationManager!=null){
                notificationManager.createNotificationChannel(channel)

                notification = Notification.Builder(this)
                    .setSmallIcon(R.drawable.eventialogo)
                    .setContentIntent(pi)
                    .setContentTitle(title)
                    .setSubText(message)
                    .setAutoCancel(true)
                    .setChannelId(channel_id)
                    .build()
            }
            else{
                notification = Notification.Builder(this)
                    .setSmallIcon(R.drawable.eventialogo)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setSubText(message)
                    .build()
            }
            if (notificationManager!=null){
                notificationManager.notify(1,notification)
            }

        }
    }

}