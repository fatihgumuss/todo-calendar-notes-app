package com.example.thenotesapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.thenotesapp.model.ToDoItem

@Dao
interface ToDoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToDo(toDoItem: ToDoItem)

    @Update
    suspend fun updateToDo(toDoItem: ToDoItem)

    @Delete
    suspend fun deleteToDo(toDoItem: ToDoItem)

    @Query("SELECT * FROM todos ORDER BY id DESC")
    fun getAllToDos(): LiveData<List<ToDoItem>>

    @Query("DELETE FROM todos WHERE id = :todoId")
    suspend fun deleteToDoById(todoId: Int)

    @Query("SELECT * FROM todos WHERE date(dueDate / 1000, 'unixepoch') = date(:date / 1000, 'unixepoch')")
    fun getToDosByDate(date: Long): LiveData<List<ToDoItem>>
}
