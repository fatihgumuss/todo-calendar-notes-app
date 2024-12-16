package com.example.thenotesapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thenotesapp.MainActivity
import com.example.thenotesapp.R
import com.example.thenotesapp.adapter.ToDoAdapter
import com.example.thenotesapp.database.NoteDatabase
import com.example.thenotesapp.databinding.FragmentTodoListBinding
import com.example.thenotesapp.repository.ToDoRepository
import com.example.thenotesapp.viewmodel.ToDoViewModel
import com.example.thenotesapp.viewmodel.ToDoViewModelFactory

class TodoListFragment : Fragment(R.layout.fragment_todo_list) {

    private var _binding: FragmentTodoListBinding? = null
    private val binding get() = _binding!!

    private lateinit var toDoViewModel: ToDoViewModel
    private lateinit var toDoAdapter: ToDoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toDoViewModel = (activity as MainActivity).toDoViewModel
        setupRecyclerView()

        binding.addTodoFab.setOnClickListener {
            it.findNavController().navigate(R.id.action_todoListFragment_to_addTodoFragment)
        }
    }

    private fun setupRecyclerView() {
        toDoAdapter = ToDoAdapter(toDoViewModel)
        binding.todoRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = toDoAdapter
        }

        activity?.let {
            toDoViewModel.getAllToDos().observe(viewLifecycleOwner) { toDo ->
                toDoAdapter.differ.submitList(toDo)
            }
        }
        toDoAdapter.setOnItemClickListener { todo ->  // Set click listener
            val action = TodoListFragmentDirections.actionTodoListFragmentToEditTodoFragment(todo)
            view?.findNavController()?.navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.title = "To-do List"
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}