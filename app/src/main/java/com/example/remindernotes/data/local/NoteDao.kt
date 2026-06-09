package com.example.remindernotes.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("SELECT * FROM notes WHERE isImportant = 1 ORDER BY updatedAt DESC")
    fun getImportantNotes(): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDone = 1 ORDER BY updatedAt DESC")
    fun getDoneNotes(): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY title ASC")
    fun getNotesSortedByTitle(): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getNotesSortedByDate(): LiveData<List<NoteEntity>>
}