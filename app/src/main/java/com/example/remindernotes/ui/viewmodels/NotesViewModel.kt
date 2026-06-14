package com.example.remindernotes.ui.viewmodels

import androidx.lifecycle.*
import com.example.remindernotes.data.local.NoteEntity
import com.example.remindernotes.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FilterType { ALL, IMPORTANT, DONE }
enum class SortType { DATE, TITLE }

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    private val _filterType = MutableLiveData(FilterType.ALL)
    private val _sortType = MutableLiveData(SortType.DATE)

    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: LiveData<String?> = _snackbarMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Переключаемся между источниками в зависимости от фильтра/сортировки
    val notes: LiveData<List<NoteEntity>> = MediatorLiveData<List<NoteEntity>>().apply {
        fun update() {
            removeSource(repository.getAllNotes())
            removeSource(repository.getImportantNotes())
            removeSource(repository.getDoneNotes())
            removeSource(repository.getNotesSortedByTitle())

            val source = when {
                _filterType.value == FilterType.IMPORTANT -> repository.getImportantNotes()
                _filterType.value == FilterType.DONE -> repository.getDoneNotes()
                _sortType.value == SortType.TITLE -> repository.getNotesSortedByTitle()
                else -> repository.getNotesSortedByDate()
            }
            addSource(source) { value = it }
        }

        addSource(_filterType) { update() }
        addSource(_sortType) { update() }
        update()
    }

    fun setFilter(filter: FilterType) { _filterType.value = filter }
    fun setSort(sort: SortType) { _sortType.value = sort }

    fun addNote(note: NoteEntity) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun updateNote(note: NoteEntity) = viewModelScope.launch {
        repository.updateNote(note)
    }

    fun deleteNote(note: NoteEntity) = viewModelScope.launch {
        repository.deleteNote(note.id)
    }

    fun restoreNote(note: NoteEntity) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun toggleImportant(note: NoteEntity) = viewModelScope.launch {
        repository.updateNote(note.copy(isImportant = !note.isImportant, updatedAt = System.currentTimeMillis()))
    }

    fun refreshFromServer() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.fetchAndMergeFromServer()
            result.onFailure { _snackbarMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun snackbarShown() { _snackbarMessage.value = null }
}