package com.example.thenotesapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thenotesapp.repository.ToDoRepository
import com.example.thenotesapp.model.ToDoItem
import kotlinx.coroutines.launch

class ToDoViewModel(app: Application, private val toDoRepository: ToDoRepository) :
    AndroidViewModel(app) {

    fun insertToDo(toDoItem: ToDoItem) = viewModelScope.launch {
        toDoRepository.insertToDo(toDoItem)
    }

    fun deleteToDo(toDoItem: ToDoItem) = viewModelScope.launch {
        toDoRepository.deleteToDo(toDoItem)
    }

    fun updateToDo(toDoItem: ToDoItem) = viewModelScope.launch {
        toDoRepository.updateToDo(toDoItem)
    }
    fun deleteToDoById(todoId: Int) = viewModelScope.launch {
        toDoRepository.deleteToDoById(todoId)
    }

    fun getAllToDos() = toDoRepository.getAllToDos()

    fun getToDosByDate(date: Long) = toDoRepository.getToDosByDate(date)
}