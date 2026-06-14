package com.example.remindernotes.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remindernotes.R
import com.example.remindernotes.databinding.FragmentNotesListBinding
import com.example.remindernotes.services.NotesAdapter
import com.example.remindernotes.ui.viewmodels.FilterType
import com.example.remindernotes.ui.viewmodels.NotesViewModel
import com.example.remindernotes.ui.viewmodels.SortType
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotesListFragment : Fragment() {

    private var _binding: FragmentNotesListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotesViewModel by viewModels()
    private lateinit var adapter: NotesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        setupSwipeRefresh()
        observeViewModel()

        viewModel.refreshFromServer()

        parentFragmentManager.setFragmentResultListener("note_result", viewLifecycleOwner) { _, bundle ->
            val msg = when (bundle.getString("action")) {
                "added" -> "Заметка добавлена"
                "saved" -> "Изменения сохранены"
                else -> null
            }
            msg?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show() }
        }
    }

    private fun observeViewModel() {
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
            binding.tvEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (notes.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefreshLayout.isRefreshing = loading
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.snackbarShown()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter(
            onNoteClick = { note ->
                val bundle = Bundle().apply { putString("noteId", note.id) }
                findNavController().navigate(R.id.noteDetailFragment, bundle)
            },
            onDeleteClick = { note ->
                viewModel.deleteNote(note)
                Snackbar.make(binding.root, "Заметка удалена", Snackbar.LENGTH_LONG)
                    .setAction("Отмена") { viewModel.restoreNote(note) }
                    .show()
            },
            onImportantToggle = { note -> viewModel.toggleImportant(note) }
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
            viewModel.refreshFromServer()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_notes_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter_all -> { viewModel.setFilter(FilterType.ALL); true }
            R.id.filter_important -> { viewModel.setFilter(FilterType.IMPORTANT); true }
            R.id.filter_done -> { viewModel.setFilter(FilterType.DONE); true }
            R.id.sort_date -> { viewModel.setSort(SortType.DATE); true }
            R.id.sort_title -> { viewModel.setSort(SortType.TITLE); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}