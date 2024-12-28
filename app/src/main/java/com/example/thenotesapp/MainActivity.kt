package com.example.thenotesapp

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.thenotesapp.database.NoteDatabase
import com.example.thenotesapp.databinding.ActivityMainBinding
import com.example.thenotesapp.fragments.CalendarFragment
import com.example.thenotesapp.fragments.TodoListFragment
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
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        notificationHelper = NotificationHelper(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        // Setup bottom navigation
        binding.bottomNavigationView.setupWithNavController(navController)

        // Handle FAB clicks based on current destination
        binding.fab.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            val currentFragment = navHostFragment.childFragmentManager.fragments[0]

            when (currentFragment) {
                is CalendarFragment -> {
                    val bundle = Bundle().apply {
                        putLong("selectedDate", currentFragment.getSelectedDate())
                    }
                    navController.navigate(R.id.action_calendarFragment_to_addTodoFragment, bundle)
                }
                is TodoListFragment -> {
                    navController.navigate(R.id.action_todoListFragment_to_addTodoFragment)
                }
                else -> {
                    // Handle navigation to AddNoteFragment for the notes section
                    navController.navigate(R.id.action_homeFragment_to_addNoteFragment)
                }
            }
        }

        // Hide FAB on destinations where it's not needed
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.addNoteFragment, R.id.editNoteFragment,
                R.id.addTodoFragment, R.id.editTodoFragment -> {
                    binding.fab.hide()
                    binding.bottomAppBar.performHide()
                }
                else -> {
                    binding.fab.show()
                    binding.bottomAppBar.performShow()
                }
            }
        }
    }

    private fun setupViewModel() {
        val noteRepository = NoteRepository(NoteDatabase(this))
        val viewModelProviderFactory = NoteViewModelFactory(application, noteRepository)
        noteViewModel = ViewModelProvider(this, viewModelProviderFactory)[NoteViewModel::class.java]

        val toDoRepository = ToDoRepository(NoteDatabase(this))
        val toDoViewModelProviderFactory = ToDoViewModelFactory(application, toDoRepository)
        toDoViewModel = ViewModelProvider(this, toDoViewModelProviderFactory)[ToDoViewModel::class.java]
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}