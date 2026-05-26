package com.example.remindernotes

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.remindernotes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NoteAdapter

    private val viewModel: NoteViewModel by viewModels {
        NoteViewModelFactory(NoteRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.init(this)
        Logger.i("MainActivity", "Activity created")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = NoteAdapter(
            onEdit = { note ->
                AddEditNoteDialog.show(this, note) { title, text ->
                    viewModel.updateNote(note.copy(title = title, text = text))
                }
            },
            onDelete = { note ->
                viewModel.deleteNote(note)
            }
        )

        binding.recyclerView.adapter = adapter

        viewModel.notes.observe(this) { notes ->
            adapter.submitList(notes)
            binding.tvEmpty.visibility =
                if (notes.isEmpty()) android.view.View.VISIBLE
                else android.view.View.GONE
        }

        binding.fab.setOnClickListener {
            Logger.d("MainActivity", "FAB clicked — opening AddNoteDialog")
            AddEditNoteDialog.show(this) { title, text ->
                viewModel.addNote(title, text)
            }
        }

        viewModel.loadNotes()
    }
}