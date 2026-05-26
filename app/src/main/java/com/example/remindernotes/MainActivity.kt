package com.example.remindernotes

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.remindernotes.data.viewmodels.NoteViewModel
import com.example.remindernotes.data.viewmodels.NoteViewModelFactory
import com.example.remindernotes.databinding.ActivityMainBinding
import com.example.remindernotes.ui.list.NoteAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.init(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}