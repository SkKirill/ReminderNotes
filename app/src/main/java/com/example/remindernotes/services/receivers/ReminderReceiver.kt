package com.example.remindernotes.services.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.remindernotes.MainActivity
import com.example.remindernotes.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteId    = intent.getStringExtra(EXTRA_NOTE_ID)    ?: return
        val noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE) ?: "Напоминание"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаём канал (нужно только один раз, но безопасно вызывать повторно)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о заметках",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о напоминаниях для заметок"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Deep-link intent: открываем MainActivity и передаём noteId
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_NOTE_ID, noteId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // добавьте иконку в drawable
            .setContentTitle("Напоминание: $noteTitle")
            .setContentText("Нажмите, чтобы открыть заметку")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(noteId.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID       = "reminders_channel"
        const val EXTRA_NOTE_ID    = "extra_note_id"
        const val EXTRA_NOTE_TITLE = "extra_note_title"
    }
}