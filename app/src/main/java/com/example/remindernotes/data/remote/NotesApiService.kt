package com.example.remindernotes.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface NotesApiService {
    @GET("reminder-notes")
    suspend fun getNotes(): Response<NotesListResponse>
}