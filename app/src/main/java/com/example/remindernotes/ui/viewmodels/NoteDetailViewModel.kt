package com.example.remindernotes.ui.viewmodel

import androidx.lifecycle.*
import com.example.remindernotes.data.local.NoteEntity
import com.example.remindernotes.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    private val _note = MutableLiveData<NoteEntity?>()
    val note: LiveData<NoteEntity?> = _note

    fun loadNote(id: String) = viewModelScope.launch {
        _note.value = repository.getNoteById(id)
    }

    fun saveNote(
        id: String?,
        title: String,
        content: String,
        isImportant: Boolean,
        isDone: Boolean,
        reminder: Long?
    ): String {
        val now = System.currentTimeMillis()
        val entity = if (id != null) {
            NoteEntity(
                id = id,
                title = title,
                content = content,
                isImportant = isImportant,
                isDone = isDone,
                reminder = reminder,
                createdAt = _note.value?.createdAt ?: now,
                updatedAt = now
            )
        } else {
            NoteEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                isImportant = isImportant,
                isDone = isDone,
                reminder = reminder,
                createdAt = now,
                updatedAt = now
            )
        }

        viewModelScope.launch {
            if (id != null) repository.updateNote(entity)
            else repository.insertNote(entity)
        }

        return if (id != null) "saved" else "added"
    }
}