package com.example.thenotesapp

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.thenotesapp.database.NoteDatabase
import com.example.thenotesapp.databinding.ActivityMainBinding
import com.example.thenotesapp.repository.NoteRepository
import com.example.thenotesapp.repository.ToDoRepository
import com.example.thenotesapp.viewmodel.NoteViewModel
import com.example.thenotesapp.viewmodel.NoteViewModelFactory
import com.example.thenotesapp.viewmodel.ToDoViewModel
import com.example.thenotesapp.viewmodel.ToDoViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var noteViewModel: NoteViewModel
    lateinit var toDoViewModel: ToDoViewModel
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        setupNavigationDrawer()
        notificationHelper = NotificationHelper(this)
    }

    private fun setupViewModel() {
        val noteRepository = NoteRepository(NoteDatabase(this))
        val viewModelProviderFactory = NoteViewModelFactory(application, noteRepository)
        noteViewModel = ViewModelProvider(this, viewModelProviderFactory)[NoteViewModel::class.java]

        val toDoRepository = ToDoRepository(NoteDatabase(this))
        val toDoViewModelProviderFactory = ToDoViewModelFactory(application, toDoRepository)
        toDoViewModel = ViewModelProvider(this, toDoViewModelProviderFactory)[ToDoViewModel::class.java]
    }

    private fun setupNavigationDrawer() {
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.notesFragment -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    findNavController(R.id.fragmentContainerView).navigate(R.id.homeFragment)
                }
                R.id.todoListFragment -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    findNavController(R.id.fragmentContainerView).navigate(R.id.todoListFragment)
                }
                R.id.nav_calendar -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    findNavController(R.id.fragmentContainerView).navigate(R.id.calendarFragment)
                }
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}