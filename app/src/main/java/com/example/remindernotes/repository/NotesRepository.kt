package com.example.remindernotes.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.remindernotes.models.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "notes_prefs"
        private const val KEY_NOTES = "notes_list"
    }

    fun getAllNotes(): MutableList<Note> {
        val json = prefs.getString(KEY_NOTES, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Note>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun saveAllNotes(notes: List<Note>) {
        val json = gson.toJson(notes)
        prefs.edit().putString(KEY_NOTES, json).apply()
    }

    fun addNote(note: Note) {
        val notes = getAllNotes()
        notes.add(note)
        saveAllNotes(notes)
    }

    fun updateNote(note: Note) {
        val notes = getAllNotes()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notes[index] = note
            saveAllNotes(notes)
        }
    }

    fun deleteNote(noteId: String) {
        val notes = getAllNotes()
        notes.removeAll { it.id.toString() == noteId }
        saveAllNotes(notes)
    }

    fun getNoteById(noteId: String): Note? {
        return getAllNotes().find { it.id.toString() == noteId }
    }
}
