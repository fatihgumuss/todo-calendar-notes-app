package com.example.thenotesapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "You have a task to complete."

        val notificationHelper = NotificationHelper(context)
        notificationHelper.sendNotification(title, message)
    }
}