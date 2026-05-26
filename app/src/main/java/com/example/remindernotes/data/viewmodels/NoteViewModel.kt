package com.example.remindernotes.data.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.remindernotes.Logger
import com.example.remindernotes.data.models.Note
import com.example.remindernotes.repositories.NoteRepository

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>(emptyList())
    val notes: LiveData<List<Note>> = _notes

    fun load() {
        _notes.value = repository.getAll()
    }

    fun getById(id: String): Note? = repository.getById(id)

    fun add(title: String, text: String, isImportant: Boolean) {
        Logger.d("NoteViewModel", "add: title=$title, important=$isImportant")
        repository.add(title, text, isImportant)
        load()
    }

    fun update(note: Note) {
        Logger.d("NoteViewModel", "update: id=${note.id}")
        repository.update(note)
        load()
    }

    fun delete(note: Note) {
        Logger.d("NoteViewModel", "delete: id=${note.id}")
        repository.delete(note)
        load()
    }
}