package com.example.remindernotes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.remindernotes.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfig = AppBarConfiguration(setOf(R.id.notesListFragment))
        setupActionBarWithNavController(navController, appBarConfig)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.title = when (destination.id) {
                R.id.notesListFragment -> "Мои заметки"
                R.id.noteDetailFragment -> "Заметка"
                else -> "Notes"
            }
        }

        // Открываем заметку, если запущены из уведомления
        handleNotificationIntent(intent)
    }

    // Вызывается при повторном onNewIntent (launchMode="singleTop")
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: android.content.Intent?) {
        val noteId = intent?.getStringExtra(EXTRA_OPEN_NOTE_ID) ?: return
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment ?: return

        // Небольшая задержка, чтобы NavController успел инициализироваться
        binding.root.post {
            val bundle = Bundle().apply { putString("noteId", noteId) }
            navHostFragment.navController.navigate(R.id.noteDetailFragment, bundle)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        const val EXTRA_OPEN_NOTE_ID = "extra_open_note_id"
    }
}