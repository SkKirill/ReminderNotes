package com.example.remindernotes.data.repository

import androidx.lifecycle.LiveData
import com.example.remindernotes.data.local.NoteDao
import com.example.remindernotes.data.local.NoteEntity
import com.example.remindernotes.data.remote.NotesApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepository @Inject constructor(
    private val dao: NoteDao,
    private val api: NotesApiService
) {
    fun getAllNotes(): LiveData<List<NoteEntity>> = dao.getAllNotes()
    fun getImportantNotes(): LiveData<List<NoteEntity>> = dao.getImportantNotes()
    fun getDoneNotes(): LiveData<List<NoteEntity>> = dao.getDoneNotes()
    fun getNotesSortedByTitle(): LiveData<List<NoteEntity>> = dao.getNotesSortedByTitle()
    fun getNotesSortedByDate(): LiveData<List<NoteEntity>> = dao.getNotesSortedByDate()

    suspend fun getNoteById(id: String): NoteEntity? = dao.getNoteById(id)
    suspend fun insertNote(note: NoteEntity) = dao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = dao.updateNote(note)
    suspend fun deleteNote(id: String) = dao.deleteNoteById(id)

    suspend fun fetchAndMergeFromServer(): Result<Unit> {
        return try {
            val response = api.getNotes()
            if (response.isSuccessful) {
                val serverNotes = response.body()?.notes ?: emptyList()
                serverNotes.forEach { serverNote ->
                    val existing = dao.getNoteById(serverNote.id)
                    dao.insertNote(
                        NoteEntity(
                            id = serverNote.id,
                            title = serverNote.title,
                            content = serverNote.content,
                            // сохраняем локальные статусы если заметка уже есть
                            isImportant = existing?.isImportant ?: false,
                            isDone = existing?.isDone ?: false,
                            reminder = existing?.reminder,
                            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}