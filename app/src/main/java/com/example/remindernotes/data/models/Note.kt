package com.example.remindernotes.data.models

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var text: String,
    var isImportant: Boolean = false
)