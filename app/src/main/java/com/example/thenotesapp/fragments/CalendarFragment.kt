package com.example.thenotesapp.fragments

import CalendarificApiService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thenotesapp.Holiday
import com.example.thenotesapp.HolidayResponse
import com.example.thenotesapp.R
import com.example.thenotesapp.adapter.ToDoAdapter
import com.example.thenotesapp.database.NoteDatabase
import com.example.thenotesapp.databinding.FragmentCalendarBinding
import com.example.thenotesapp.model.ToDoItem
import com.example.thenotesapp.repository.ToDoRepository
import com.example.thenotesapp.viewmodel.ToDoViewModel
import com.example.thenotesapp.viewmodel.ToDoViewModelFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var toDoViewModel: ToDoViewModel
    private lateinit var toDoAdapter: ToDoAdapter
    private lateinit var calendarificApiService: CalendarificApiService
    private val holidayCache = mutableMapOf<Int, List<Holiday>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://calendarific.com/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        calendarificApiService = retrofit.create(CalendarificApiService::class.java)

        val toDoRepository = ToDoRepository(NoteDatabase(requireContext()))
        val viewModelFactory = ToDoViewModelFactory(requireActivity().application, toDoRepository)
        toDoViewModel = ViewModelProvider(this, viewModelFactory)[ToDoViewModel::class.java]
        setupRecyclerView()
        var selectedDate: Long = binding.calendarView.date
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
            toDoViewModel.getToDosByDate(selectedDate).observe(viewLifecycleOwner) { todos ->
                toDoAdapter.differ.submitList(todos)
            }
        }
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            loadTasksForDate(selectedDate)
        }
        binding.fabAddTodo.setOnClickListener {
            val action = CalendarFragmentDirections.actionCalendarFragmentToAddTodoFragment(selectedDate)
            view?.findNavController()?.navigate(action)
        }
    }

    private fun loadHolidays(date: Long, callback: (List<ToDoItem>) -> Unit) {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        val year = calendar.get(Calendar.YEAR)

        // Check if holidays for the year are already cached
        if (holidayCache.containsKey(year)) {
            val holidays = holidayCache[year] ?: emptyList()
            filterAndCallbackHolidays(holidays, date, callback)
            return
        }

        val apiKey = "iNpofDFNq6Xu2fgaOcck9obQxtj9le1a"
        val call = calendarificApiService.getHolidays(apiKey, "TR", year,"tr")
        call.enqueue(object : retrofit2.Callback<HolidayResponse> {
            override fun onResponse(call: Call<HolidayResponse>, response: retrofit2.Response<HolidayResponse>) {
                if (response.isSuccessful) {
                    val holidays = response.body()?.response?.holidays ?: emptyList()
                    // Cache the holidays
                    holidayCache[year] = holidays
                    filterAndCallbackHolidays(holidays, date, callback)
                } else {
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<HolidayResponse>, t: Throwable) {
                callback(emptyList())
            }
        })
    }
    private fun filterAndCallbackHolidays(holidays: List<Holiday>, date: Long, callback: (List<ToDoItem>) -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val selectedDateString = dateFormat.format(date)
    val filteredHolidays = holidays.filter { it.date.iso == selectedDateString }
    val holidayItems = filteredHolidays.map { holiday ->
        ToDoItem(0, holiday.name, "", false, 0, date, null, isHoliday = true)
    }
    callback(holidayItems)
}

    private fun setupRecyclerView() {
        toDoAdapter = ToDoAdapter(toDoViewModel)
        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = toDoAdapter
        }
        toDoAdapter.setOnItemClickListener { todo ->
            val action = CalendarFragmentDirections.actionCalendarFragmentToEditTodoFragment(todo)
            view?.findNavController()?.navigate(action)
        }
    }

    private fun loadTasksForDate(date: Long) {
        toDoViewModel.getToDosByDate(date).observe(viewLifecycleOwner) { todos ->
            loadHolidays(date) { holidays ->
                val combinedList = holidays + todos
                toDoAdapter.differ.submitList(combinedList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}