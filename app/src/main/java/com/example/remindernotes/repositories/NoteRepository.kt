package com.example.remindernotes.repositories

import android.content.Context
import com.example.remindernotes.data.models.Note
import org.json.JSONArray
import org.json.JSONObject

class NoteRepository(context: Context) {

    private val prefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)
    private val key = "notes_json"

    fun getAll(): List<Note> {
        val json = prefs.getString(key, "[]") ?: "[]"
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Note(
                id          = obj.getString("id"),
                title       = obj.getString("title"),
                text        = obj.getString("text"),
                isImportant = obj.optBoolean("isImportant", false)
            )
        }
    }

    fun save(notes: List<Note>) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(JSONObject().apply {
                put("id",          note.id)
                put("title",       note.title)
                put("text",        note.text)
                put("isImportant", note.isImportant)
            })
        }
        prefs.edit().putString(key, array.toString()).apply()
    }

    fun add(title: String, text: String, isImportant: Boolean = false) {
        val notes = getAll().toMutableList()
        notes.add(Note(title = title, text = text, isImportant = isImportant))
        save(notes)
    }

    fun update(note: Note) {
        val notes = getAll().toMutableList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) notes[index] = note
        save(notes)
    }

    fun delete(note: Note) {
        val notes = getAll().toMutableList()
        notes.removeAll { it.id == note.id }
        save(notes)
    }

    fun getById(id: String): Note? = getAll().find { it.id == id }
}