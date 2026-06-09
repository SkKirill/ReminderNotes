package com.example.remindernotes.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class NoteResponse(
    @SerializedName("id")       val id: UUID,
    @SerializedName("title")    val title: String,
    @SerializedName("content")  val content: String,
    @SerializedName("createdAt")   val createdAt: String?,
    @SerializedName("updatedAt")   val updatedAt: String?,
    @SerializedName("reminderAt")  val reminderAt: String?
)

data class NotesListResponse(
    @SerializedName("notes") val notes: List<NoteResponse>
)