package com.example.remindernotes.models

import com.example.remindernotes.services.NotesApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://gateway-staging.superglue.games/android/api/v1/public/mocks/"

    val api: NotesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotesApiService::class.java)
    }
}