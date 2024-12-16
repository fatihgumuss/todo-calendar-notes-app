package com.example.thenotesapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.thenotesapp.repository.ToDoRepository

class ToDoViewModelFactory(
    private val app: Application,
    private val toDoRepository: ToDoRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ToDoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ToDoViewModel(app, toDoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}