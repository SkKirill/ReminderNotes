package com.example.remindernotes.ui.fragments

import com.example.remindernotes.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remindernotes.databinding.FragmentNotesListBinding
import com.example.remindernotes.models.Note
import com.example.remindernotes.repository.NotesRepository
import com.example.remindernotes.services.NotesAdapter
import com.google.android.material.snackbar.Snackbar

class NotesListFragment : Fragment() {

    private var _binding: FragmentNotesListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: NotesRepository
    private lateinit var adapter: NotesAdapter
    private var notes = mutableListOf<Note>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = NotesRepository(requireContext())

        setupRecyclerView()
        setupFab()
        loadNotes()

        // Check for result from detail fragment (new note added or note saved)
        parentFragmentManager.setFragmentResultListener(
            "note_result", viewLifecycleOwner
        ) { _, bundle ->
            val action = bundle.getString("action")
            when (action) {
                "added" -> {
                    loadNotes()
                    Snackbar.make(binding.root, "Заметка добавлена", Snackbar.LENGTH_SHORT).show()
                }
                "saved" -> {
                    loadNotes()
                    Snackbar.make(binding.root, "Изменения сохранены", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter(
            onNoteClick = { note ->
                val bundle = Bundle().apply {
                    putString("noteId", note.id.toString())
                }
                findNavController().navigate(R.id.noteDetailFragment, bundle)
            },
            onDeleteClick = { note ->
                deleteNote(note)
            },
            onImportantToggle = { note ->
                toggleImportant(note)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(R.id.noteDetailFragment)
        }
    }

    private fun loadNotes() {
        notes = repository.getAllNotes()
        adapter.submitList(notes.toList())
        updateEmptyState()
    }

    private fun deleteNote(note: Note) {
        val position = notes.indexOfFirst { it.id == note.id }
        repository.deleteNote(note.id.toString())
        loadNotes()

        Snackbar.make(binding.root, "Заметка удалена", Snackbar.LENGTH_LONG)
            .setAction("Отмена") {
                repository.addNote(note)
                loadNotes()
            }
            .show()
    }

    private fun toggleImportant(note: Note) {
        val updatedNote = note.copy(isImportant = !note.isImportant)
        repository.updateNote(updatedNote)
        loadNotes()
    }

    private fun updateEmptyState() {
        if (notes.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}