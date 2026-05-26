package com.example.remindernotes.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.remindernotes.data.viewmodels.NoteViewModel
import com.example.remindernotes.data.viewmodels.NoteViewModelFactory
import com.example.remindernotes.databinding.FragmentNoteDetailBinding
import com.example.remindernotes.repositories.NoteRepository
import com.google.android.material.snackbar.Snackbar

class NoteDetailFragment : Fragment() {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    private val args: NoteDetailFragmentArgs by navArgs()

    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(NoteRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val note = viewModel.getById(args.noteId)
        if (note == null) {
            findNavController().popBackStack()
            return
        }

        binding.tvDetailTitle.text = note.title
        binding.tvDetailText.text = note.text
        binding.ivDetailImportant.setImageResource(
            if (note.isImportant) R.drawable.ic_star_filled
            else R.drawable.ic_star_outline
        )

        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                NoteDetailFragmentDirections.actionNoteDetailToNoteEdit(note.id)
            )
        }

        binding.btnDelete.setOnClickListener {
            viewModel.delete(note)
            Snackbar.make(binding.root, "Заметка удалена", Snackbar.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}