package com.example.remindernotes

class NoteRepository {

    private val notes = mutableListOf<Note>()

    fun getAll(): List<Note> = notes.toList()

    fun add(title: String, text: String) {
        notes.add(Note(title = title, text = text))
    }

    fun update(note: Note) {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) notes[index] = note
    }

    fun delete(note: Note) {
        notes.removeAll { it.id == note.id }
    }
}