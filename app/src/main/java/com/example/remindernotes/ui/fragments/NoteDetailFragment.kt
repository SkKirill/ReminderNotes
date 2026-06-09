package com.example.remindernotes.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.remindernotes.databinding.FragmentNoteDetailBinding
import com.example.remindernotes.models.Note
import com.example.remindernotes.data.repository.NotesRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: NotesRepository
    private var currentNote: Note? = null
    private var reminderTime: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = NotesRepository(requireContext())

        val noteId = arguments?.getString("noteId")
        if (noteId != null) {
            currentNote = repository.getNoteById(noteId)
            populateFields()
        }

        setupButtons()
    }

    private fun populateFields() {
        currentNote?.let { note ->
            binding.etTitle.setText(note.title)
            binding.etContent.setText(note.content)
            binding.switchImportant.isChecked = note.isImportant
            binding.checkboxDone.isChecked = note.isDone
            updateReminderText()
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            saveNote()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSetReminder.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnClearReminder.setOnClickListener {
            reminderTime = null
            updateReminderText()
        }
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "Введите заголовок"
            binding.etTitle.requestFocus()
            return
        }

        val isImportant = binding.switchImportant.isChecked
        val isDone = binding.checkboxDone.isChecked

        if (currentNote == null) {
            // New note
            val newNote = Note(
                title = title,
                content = content,
                isImportant = isImportant,
                isDone = isDone
            )
            repository.addNote(newNote)

            val result = Bundle().apply { putString("action", "added") }
            parentFragmentManager.setFragmentResult("note_result", result)
        } else {
            // Update existing note
            val updatedNote = currentNote!!.copy(
                title = title,
                content = content,
                isImportant = isImportant,
                isDone = isDone
            )
            repository.updateNote(updatedNote)

            val result = Bundle().apply { putString("action", "saved") }
            parentFragmentManager.setFragmentResult("note_result", result)
        }

        findNavController().navigateUp()
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        reminderTime?.let { calendar.timeInMillis = it }

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, day, hour, minute, 0)
                        reminderTime = cal.timeInMillis
                        updateReminderText()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateReminderText() {
        if (reminderTime != null) {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            binding.tvReminderTime.text = "Напоминание: ${sdf.format(Date(reminderTime!!))}"
            binding.tvReminderTime.visibility = View.VISIBLE
            binding.btnClearReminder.visibility = View.VISIBLE
        } else {
            binding.tvReminderTime.visibility = View.GONE
            binding.btnClearReminder.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}