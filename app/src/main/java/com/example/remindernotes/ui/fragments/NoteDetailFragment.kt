package com.example.remindernotes.ui.fragments

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.remindernotes.R
import com.example.remindernotes.data.local.NoteEntity
import com.example.remindernotes.data.repository.NotesRepository
import com.example.remindernotes.databinding.FragmentNoteDetailBinding
import com.example.remindernotes.utils.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var repository: NotesRepository

    private var currentNote: NoteEntity? = null
    private var reminderTime: Long? = null

    // ---- Launchers для разрешений и голосового ввода ----

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(requireContext(),
                "Разрешение на уведомления отклонено. Напоминания не будут показаны.",
                Toast.LENGTH_LONG).show()
        }
    }

    private val microphonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchSpeechRecognizer()
        else Toast.makeText(requireContext(),
            "Разрешение на микрофон отклонено.",
            Toast.LENGTH_SHORT).show()
    }

    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!spokenText.isNullOrBlank()) {
            // Добавляем распознанный текст к уже имеющемуся содержимому
            val current = binding.etContent.text.toString()
            binding.etContent.setText(
                if (current.isEmpty()) spokenText else "$current $spokenText"
            )
            binding.etContent.setSelection(binding.etContent.text?.length ?: 0)
        }
    }

    // ---- Lifecycle ----

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestNotificationPermissionIfNeeded()

        val noteId = arguments?.getString("noteId")
        if (noteId != null) {
            lifecycleScope.launch {
                currentNote = repository.getNoteById(noteId)
                populateFields()
            }
        }

        setupButtons()
    }

    // ---- UI ----

    private fun populateFields() {
        currentNote?.let { note ->
            binding.etTitle.setText(note.title)
            binding.etContent.setText(note.content)
            binding.switchImportant.isChecked = note.isImportant
            binding.checkboxDone.isChecked = note.isDone
            reminderTime = note.reminder
            updateReminderText()
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener { saveNote() }
        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }
        binding.btnSetReminder.setOnClickListener { showDateTimePicker() }
        binding.btnClearReminder.setOnClickListener {
            currentNote?.let { ReminderScheduler.cancel(requireContext(), it.id) }
            reminderTime = null
            updateReminderText()
        }
        binding.tilContent.setEndIconOnClickListener { checkMicAndStartVoice() }
    }

    private fun updateReminderText() {
        if (reminderTime != null) {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            binding.tvReminderTime.text = getString(R.string.reminder_time, sdf.format(Date(reminderTime!!)))
            binding.tvReminderTime.visibility = View.VISIBLE
            binding.btnClearReminder.visibility = View.VISIBLE
        } else {
            binding.tvReminderTime.visibility = View.GONE
            binding.btnClearReminder.visibility = View.GONE
        }
    }

    // ---- Сохранение ----

    private fun saveNote() {
        val title   = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "Введите заголовок"
            binding.etTitle.requestFocus()
            return
        }

        val isImportant = binding.switchImportant.isChecked
        val isDone      = binding.checkboxDone.isChecked

        lifecycleScope.launch {
            val noteId: String
            if (currentNote == null) {
                noteId = UUID.randomUUID().toString()
                val newNote = NoteEntity(
                    id          = noteId,
                    title       = title,
                    content     = content,
                    isImportant = isImportant,
                    isDone      = isDone,
                    reminder    = reminderTime,
                    createdAt   = System.currentTimeMillis(),
                    updatedAt   = System.currentTimeMillis()
                )
                repository.insertNote(newNote)
                parentFragmentManager.setFragmentResult("note_result",
                    Bundle().apply { putString("action", "added") })
            } else {
                noteId = currentNote!!.id
                val updatedNote = currentNote!!.copy(
                    title       = title,
                    content     = content,
                    isImportant = isImportant,
                    isDone      = isDone,
                    reminder    = reminderTime,
                    updatedAt   = System.currentTimeMillis()
                )
                repository.updateNote(updatedNote)
                parentFragmentManager.setFragmentResult("note_result",
                    Bundle().apply { putString("action", "saved") })
            }

            // Планируем или отменяем напоминание
            scheduleOrCancelReminder(noteId, title)

            findNavController().navigateUp()
        }
    }

    private fun scheduleOrCancelReminder(noteId: String, title: String) {
        val time = reminderTime
        if (time != null && time > System.currentTimeMillis()) {
            ReminderScheduler.schedule(requireContext(), noteId, title, time)
        } else {
            ReminderScheduler.cancel(requireContext(), noteId)
        }
    }

    // ---- DateTimePicker ----

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

    // ---- Голосовой ввод ----

    private fun checkMicAndStartVoice() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> launchSpeechRecognizer()

            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(requireContext(),
                    "Доступ к микрофону нужен для голосового ввода",
                    Toast.LENGTH_LONG).show()
                microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            else -> microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun launchSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите…")
        }
        // Проверяем, есть ли на устройстве приложение для распознавания речи
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            speechRecognizerLauncher.launch(intent)
        } else {
            Toast.makeText(requireContext(),
                "Распознавание речи недоступно на этом устройстве",
                Toast.LENGTH_SHORT).show()
        }
    }

    // ---- Разрешение на уведомления (Android 13+) ----

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}