package com.example.remindernotes.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.remindernotes.Logger
import com.example.remindernotes.data.viewmodels.NoteViewModel
import com.example.remindernotes.data.viewmodels.NoteViewModelFactory
import com.example.remindernotes.databinding.FragmentNoteListBinding
import com.example.remindernotes.repositories.NoteRepository
import com.google.android.material.snackbar.Snackbar

class NoteListFragment : Fragment() {

    private var _binding: FragmentNoteListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(NoteRepository(requireContext()))
    }

    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NoteAdapter(
            onClick = { note ->
                Logger.d("NoteListFragment", "Open detail: id=${note.id}")
                val action = NoteListFragmentDirections
                    .actionNoteListToNoteDetail(note.id)
                findNavController().navigate(action)
            },
            onDelete = { note ->
                viewModel.delete(note)
                Snackbar.make(binding.root, "Заметка удалена", Snackbar.LENGTH_SHORT).show()
                Logger.d("NoteListFragment", "Deleted: id=${note.id}")
            }
        )

        binding.recyclerView.adapter = adapter

        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
            binding.tvEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(
                NoteListFragmentDirections.actionNoteListToNoteEdit(noteId = "")
            )
        }

        viewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}