package com.example.remindernotes.ui.edit

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
import com.example.remindernotes.databinding.FragmentNoteEditBinding
import com.example.remindernotes.repositories.NoteRepository
import com.google.android.material.snackbar.Snackbar

class NoteEditFragment : Fragment() {

    private var _binding: FragmentNoteEditBinding? = null
    private val binding get() = _binding!!

    private val args: NoteEditFragmentArgs by navArgs()

    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(NoteRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Если noteId пустой — создаём новую заметку
        val existingNote = if (args.noteId.isNotEmpty()) viewModel.getById(args.noteId) else null

        existingNote?.let {
            binding.etTitle.setText(it.title)
            binding.etText.setText(it.text)
            binding.switchImportant.isChecked = it.isImportant
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val text  = binding.etText.text.toString().trim()
            val isImportant = binding.switchImportant.isChecked

            if (title.isEmpty()) {
                binding.tilTitle.error = "Заголовок не может быть пустым"
                return@setOnClickListener
            }

            if (existingNote == null) {
                viewModel.add(title, text, isImportant)
                Snackbar.make(binding.root, "Заметка добавлена", Snackbar.LENGTH_SHORT).show()
            } else {
                viewModel.update(existingNote.copy(title = title, text = text, isImportant = isImportant))
                Snackbar.make(binding.root, "Изменения сохранены", Snackbar.LENGTH_SHORT).show()
            }

            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}