package com.example.remindernotes.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.remindernotes.services.receivers.ReminderReceiver
import kotlin.jvm.java

object ReminderScheduler {

    fun schedule(context: Context, noteId: String, noteTitle: String, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = buildIntent(context, noteId, noteTitle)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // setExactAndAllowWhileIdle — точное срабатывание даже в Doze-режиме
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun cancel(context: Context, noteId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.hashCode(),
            buildIntent(context, noteId, ""),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun buildIntent(context: Context, noteId: String, noteTitle: String) =
        Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_NOTE_ID, noteId)
            putExtra(ReminderReceiver.EXTRA_NOTE_TITLE, noteTitle)
        }
}