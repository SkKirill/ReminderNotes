package com.example.remindernotes.services

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.remindernotes.databinding.ItemNoteBinding
import com.example.remindernotes.models.Note

class NotesAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit,
    private val onImportantToggle: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Хранит ссылки на View элемента
     */
    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
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
                tvDoneStatus.visibility =
                    if (note.isDone) android.view.View.VISIBLE else android.view.View.GONE

                root.setOnClickListener { onNoteClick(note) }
                ivDelete.setOnClickListener { onDeleteClick(note) }
                ivImportant.setOnClickListener { onImportantToggle(note) }
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
    }
}
