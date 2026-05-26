package com.example.remindernotes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>(emptyList())
    val notes: LiveData<List<Note>> = _notes

    fun loadNotes() {
        _notes.value = repository.getAll()
    }

    fun addNote(title: String, text: String) {
        Logger.d("NoteViewModel", "Adding note: title=$title")
        repository.add(title, text)
        loadNotes()
    }

    fun updateNote(note: Note) {
        Logger.d("NoteViewModel", "Updating note: id=${note.id}, title=${note.title}")
        repository.update(note)
        loadNotes()
    }

    fun deleteNote(note: Note) {
        Logger.d("NoteViewModel", "Deleting note: id=${note.id}, title=${note.title}")
        repository.delete(note)
        loadNotes()
    }
}