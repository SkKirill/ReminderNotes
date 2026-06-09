package com.example.remindernotes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remindernotes.R
import com.example.remindernotes.databinding.FragmentNotesListBinding
import com.example.remindernotes.models.Note
import com.example.remindernotes.models.RetrofitClient
import com.example.remindernotes.repository.NotesRepository
import com.example.remindernotes.services.NotesAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

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
        setupSwipeRefresh()
        loadNotes()
        fetchNotesFromServer()

        parentFragmentManager.setFragmentResultListener(
            "note_result", viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getString("action")) {
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
                val bundle = Bundle().apply { putString("noteId", note.id.toString()) }
                findNavController().navigate(R.id.noteDetailFragment, bundle)
            },
            onDeleteClick = { note -> deleteNote(note) },
            onImportantToggle = { note -> toggleImportant(note) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(R.id.noteDetailFragment)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchNotesFromServer()
        }
    }

    private fun fetchNotesFromServer() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.swipeRefreshLayout.isRefreshing = true
            try {
                val response = RetrofitClient.api.getNotes()
                if (response.isSuccessful) {
                    val serverNotes = response.body()?.notes ?: emptyList()
                    repository.mergeWithServerNotes(serverNotes)
                    loadNotes()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Ошибка сервера: ${response.code()}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Snackbar.make(
                    binding.root,
                    "Нет соединения: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun loadNotes() {
        notes = repository.getAllNotes()
        adapter.submitList(notes.toList())
        updateEmptyState()
    }

    private fun deleteNote(note: Note) {
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
        repository.updateNote(note.copy(isImportant = !note.isImportant))
        loadNotes()
    }

    private fun updateEmptyState() {
        binding.tvEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (notes.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}