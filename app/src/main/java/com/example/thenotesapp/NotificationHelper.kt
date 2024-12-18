package com.example.thenotesapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat


class NotificationHelper(private val context: Context) {

    private val channelId = "todo_notifications"
    private val channelName = "To-Do Notifications"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for to-do items"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    fun sendNotification(
        title: String,
        message: String,
        isMeeting: Boolean = false,
        contact: Contact? = null
    ) {
        val notificationId = System.currentTimeMillis().toInt() // Unique ID

        // Intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification) // Replace with your app icon
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (isMeeting && contact != null) {
            // Intent for "Send Message" action
            val sendMessageIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:${contact.phoneNumber}")
                putExtra("sms_body", "Hi ${contact.name}, just a reminder about our meeting scheduled for today.")
            }
            val sendMessagePendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 1,
                sendMessageIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            // Add action button
            builder.addAction(
                R.drawable.ic_message, // Replace with your icon
                "Send Message",
                sendMessagePendingIntent
            )
        }

        // "Dismiss Reminder" action (does nothing but dismisses)
        val dismissIntent = Intent(context, DismissReceiver::class.java).apply {
            putExtra("notificationId", notificationId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            dismissIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            R.drawable.ic_dismiss, // Replace with your icon
            "Dismiss Reminder",
            dismissPendingIntent
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }
}