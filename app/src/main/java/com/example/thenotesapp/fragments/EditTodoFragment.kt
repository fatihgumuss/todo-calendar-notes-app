package com.example.thenotesapp.fragments

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.thenotesapp.MainActivity
import com.example.thenotesapp.R
import com.example.thenotesapp.databinding.FragmentEditTodoBinding
import com.example.thenotesapp.model.ToDoItem
import com.example.thenotesapp.viewmodel.ToDoViewModel
import java.text.SimpleDateFormat
import java.util.*

class EditTodoFragment : Fragment(R.layout.fragment_edit_todo) {

    private var _binding: FragmentEditTodoBinding? = null
    private val binding get() = _binding!!

    private lateinit var toDoViewModel: ToDoViewModel
    private val args: EditTodoFragmentArgs by navArgs()
    private lateinit var currentTodo: ToDoItem
    private var dueDate: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toDoViewModel = (activity as MainActivity).toDoViewModel
        currentTodo = args.todo!!

        binding.etTodo.setText(currentTodo.task)
        binding.etTodoDescription.setText(currentTodo.description)
        binding.spinnerPriority.setSelection(currentTodo.priority)

        // Set due date if it exists
        dueDate = currentTodo.dueDate
        if (dueDate != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDueDate = dateFormat.format(dueDate)
            binding.btnDueDate.text = formattedDueDate
        }

        // Set up due date picker
        binding.btnDueDate.setOnClickListener {
            showDatePicker()
        }
        binding.backButton.setOnClickListener {
            view.findNavController().navigateUp()
        }
        binding.saveButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                updateTodo()
            }
        }

        setHasOptionsMenu(true)
    }

    private fun updateTodo() {
        val todoTask = binding.etTodo.text.toString()
        val todoDescription = binding.etTodoDescription.text.toString()
        val priority = binding.spinnerPriority.selectedItemPosition

        if (todoTask.isNotEmpty()) {
            val updatedTodo = ToDoItem(currentTodo.id, todoTask, todoDescription, currentTodo.isCompleted, priority, dueDate)
            toDoViewModel.updateToDo(updatedTodo)
            view?.findNavController()?.navigate(R.id.action_editTodoFragment_to_todoListFragment)
        } else {
            Toast.makeText(requireContext(), "Please enter a task", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Date Picker ---

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                // Set the due date directly (at the start of the day)
                val calendar = Calendar.getInstance()
                calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0)
                dueDate = calendar.timeInMillis

                // Format the date and display it
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDueDate = dateFormat.format(dueDate)
                binding.btnDueDate.text = formattedDueDate
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    // --- Menu ---

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_add_todo, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.saveTodo -> {
                updateTodo()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}