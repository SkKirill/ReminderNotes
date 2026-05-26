package com.example.remindernotes.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.remindernotes.Logger
import com.example.remindernotes.databinding.ItemNoteBinding
import com.example.remindernotes.data.models.Note

class NoteAdapter(
    private val onClick: (Note) -> Unit,
    private val onDelete: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.tvTitle.text = note.title
            binding.tvText.text = note.text
            binding.ivImportant.setImageResource(
                if (note.isImportant) R.drawable.ic_star_filled
                else R.drawable.ic_star_outline
            )
            binding.root.setOnClickListener { onClick(note) }
            binding.btnDelete.setOnClickListener { onDelete(note) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NoteViewHolder(
            ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) =
        holder.bind(getItem(position))
}

class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(old: Note, new: Note) = old.id == new.id
    override fun areContentsTheSame(old: Note, new: Note) = old == new
}