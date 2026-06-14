package com.example.remindernotes.services.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.remindernotes.data.local.NotesDatabase
import com.example.remindernotes.utils.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Читаем все заметки напрямую (BroadcastReceiver не поддерживает Hilt инъекции)
        val db = NotesDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            val notes = db.noteDao().getAllNotesSync()
            val now = System.currentTimeMillis()
            notes.filter { it.reminder != null && it.reminder > now }
                .forEach { note ->
                    ReminderScheduler.schedule(context, note.id, note.title, note.reminder!!)
                }
        }
    }
}