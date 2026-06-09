package com.example.remindernotes.services

import com.example.remindernotes.models.NotesListResponse
import retrofit2.Response
import retrofit2.http.GET

interface NotesApiService {
    @GET("reminder-notes")
    suspend fun getNotes(): Response<NotesListResponse>
}