package com.example.thenotesapp.repository

import com.example.thenotesapp.database.NoteDatabase
import com.example.thenotesapp.model.ToDoItem

class ToDoRepository(private val db: NoteDatabase) {
    suspend fun deleteToDoById(todoId: Int) = db.getToDoDao().deleteToDoById(todoId)
    suspend fun insertToDo(toDoItem: ToDoItem) = db.getToDoDao().insertToDo(toDoItem)
    suspend fun deleteToDo(toDoItem: ToDoItem) = db.getToDoDao().deleteToDo(toDoItem)
    suspend fun updateToDo(toDoItem: ToDoItem) = db.getToDoDao().updateToDo(toDoItem)
    fun getAllToDos() = db.getToDoDao().getAllToDos()
    fun getToDosByDate(date: Long) = db.getToDoDao().getToDosByDate(date)
}