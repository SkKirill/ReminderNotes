package com.example.remindernotes.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.remindernotes.data.local.NoteEntity
import com.example.remindernotes.databinding.ItemNoteBinding

class NotesAdapter(
    private val onNoteClick: (NoteEntity) -> Unit,
    private val onDeleteClick: (NoteEntity) -> Unit,
    private val onImportantToggle: (NoteEntity) -> Unit
) : ListAdapter<NoteEntity, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: NoteEntity) {
            binding.apply {
                tvTitle.text = note.title
                tvContent.text = note.content.ifEmpty { "Нет текста" }

                ivImportant.setImageResource(
                    if (note.isImportant)
                        android.R.drawable.btn_star_big_on
                    else
                        android.R.drawable.btn_star_big_off
                )

                tvDoneStatus.text = if (note.isDone) "✓ Выполнено" else ""
                tvDoneStatus.visibility = if (note.isDone) View.VISIBLE else View.GONE

                root.setOnClickListener { onNoteClick(note) }
                ivDelete.setOnClickListener { onDeleteClick(note) }
                ivImportant.setOnClickListener { onImportantToggle(note) }
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem == newItem
    }
}