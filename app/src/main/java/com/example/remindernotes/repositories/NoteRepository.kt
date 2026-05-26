package com.example.remindernotes.repositories

import android.content.Context
import com.example.remindernotes.Logger
import com.example.remindernotes.data.models.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.collections.toMutableList

class NoteRepository(private val context: Context) {

    private val gson = Gson()
    private val fileName = "notes.json"

    private fun getFile() = File(context.filesDir, fileName)

    private fun saveToFile(notes: List<Note>) {
        try {
            val json = gson.toJson(notes)
            getFile().writeText(json)
            Logger.d("NoteRepository", "Saved ${notes.size} notes to file")
        } catch (e: Exception) {
            Logger.e("NoteRepository", "Failed to save notes", e)
        }
    }

    fun getAll(): List<Note> {
        return try {
            val file = getFile()
            if (!file.exists()) return emptyList()
            val json = file.readText()
            val type = object : TypeToken<List<Note>>() {}.type
            val notes: List<Note> = gson.fromJson(json, type)
            Logger.d("NoteRepository", "Loaded ${notes.size} notes from file")
            notes
        } catch (e: Exception) {
            Logger.e("NoteRepository", "Failed to load notes", e)
            emptyList()
        }
    }

    fun add(title: String, text: String) {
        val notes = getAll().toMutableList()
        notes.add(Note(title = title, text = text))
        saveToFile(notes)
    }

    fun update(note: Note) {
        val notes = getAll().toMutableList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notes[index] = note
            saveToFile(notes)
        }
    }

    fun delete(note: Note) {
        val notes = getAll().toMutableList()
        notes.removeAll { it.id == note.id }
        saveToFile(notes)
    }
}