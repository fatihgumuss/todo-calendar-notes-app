package com.example.thenotesapp.fragments

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.thenotesapp.MainActivity
import com.example.thenotesapp.NotificationReceiver
import com.example.thenotesapp.R
import com.example.thenotesapp.databinding.FragmentAddTodoBinding
import com.example.thenotesapp.model.ToDoItem
import com.example.thenotesapp.viewmodel.ToDoViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi

class AddTodoFragment : Fragment(R.layout.fragment_add_todo) {

    private var _binding: FragmentAddTodoBinding? = null
    private val binding get() = _binding!!

    private lateinit var toDoViewModel: ToDoViewModel
    private var dueDate: Long? = null // Store the due date in milliseconds
    private var notificationTime: Long? = null // Store the notification time in milliseconds
    private var selectedDate: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toDoViewModel = (activity as MainActivity).toDoViewModel

        setHasOptionsMenu(true)

        // Retrieve the selected date from arguments
        selectedDate = arguments?.getLong("selectedDate")

        // Set the due date button text if selected date is not null
        selectedDate?.let {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDueDate = dateFormat.format(it)
            binding.btnDueDate.text = formattedDueDate
        }

        // Set up due date picker
        binding.btnDueDate.setOnClickListener {
            showDatePicker()
        }

        // Set up notification time picker
        binding.btnNotificationTime.setOnClickListener {
            showTimePicker()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
private fun saveTodo() {
    val todoTask = binding.etTodo.text.toString()
    val todoDescription = binding.etTodoDescription.text.toString()
    val priority = binding.spinnerPriority.selectedItemPosition

    if (todoTask.isNotEmpty()) {
        val todo = ToDoItem(0, todoTask, todoDescription, false, priority, selectedDate, notificationTime)
        toDoViewModel.insertToDo(todo)

        // Schedule notification if notification time is set
        if (notificationTime != null) {
            scheduleNotification(todoTask, todoDescription, notificationTime!!)
        }

        view?.findNavController()?.navigate(R.id.action_addTodoFragment_to_todoListFragment)
    } else {
        Toast.makeText(requireContext(), "Please enter a task", Toast.LENGTH_SHORT).show()
    }
}

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0) // Set time to midnight
                selectedDate = calendar.timeInMillis

                // Format the date and display it
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDueDate = dateFormat.format(selectedDate)
                binding.btnDueDate.text = formattedDueDate
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minuteOfHour ->
                if (selectedDate != null) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = selectedDate!!
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minuteOfHour)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    notificationTime = calendar.timeInMillis

                    // Format the time and display it
                    val timeFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                    val formattedTime = timeFormat.format(notificationTime)
                    binding.btnNotificationTime.text = formattedTime
                } else {
                    Toast.makeText(requireContext(), "Please select a due date first", Toast.LENGTH_SHORT).show()
                }
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
private fun scheduleNotification(title: String, description: String, timeInMillis: Long) {
    if (!canScheduleExactAlarms()) {
        Toast.makeText(requireContext(), "Exact alarm permission is required", Toast.LENGTH_SHORT).show()
        requestExactAlarmPermission()
        return
    }

    val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("message", description)
    }

    val requestCode = System.currentTimeMillis().toInt() // Use unique request code
    val pendingIntent = PendingIntent.getBroadcast(
        requireContext(),
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    val scheduledTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(timeInMillis))
    Log.d("AddTodoFragment", "Notification scheduled for: $scheduledTime")
}


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_add_todo, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.saveTodo -> {
                saveTodo()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun canScheduleExactAlarms(): Boolean {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        startActivity(intent)
    }

}
