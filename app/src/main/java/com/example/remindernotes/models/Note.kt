package com.example.remindernotes.models

import java.util.UUID

data class Note(
    val id: UUID = UUID.randomUUID(),

    var title: String = "",
    var content: String = "",
    var isImportant: Boolean = false,
    var isDone: Boolean = false
)
