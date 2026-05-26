package com.example.remindernotes

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.remindernotes.databinding.DialogAddEditNoteBinding

object AddEditNoteDialog {

    fun show(
        context: Context,
        existingNote: Note? = null,
        onSave: (title: String, text: String) -> Unit
    ) {
        val binding = DialogAddEditNoteBinding.inflate(
            android.view.LayoutInflater.from(context)
        )

        existingNote?.let {
            binding.etTitle.setText(it.title)
            binding.etText.setText(it.text)
        }

        AlertDialog.Builder(context)
            .setTitle(if (existingNote == null) "New note" else "Edit note")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val title = binding.etTitle.text.toString().trim()
                val text = binding.etText.text.toString().trim()
                if (title.isNotEmpty()) onSave(title, text)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}