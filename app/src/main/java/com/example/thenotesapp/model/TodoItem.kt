package com.example.thenotesapp.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "todos")
@Parcelize
data class ToDoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val task: String,
    val description: String?,
    val isCompleted: Boolean,
    val priority: Int,  // Add priority (e.g., 1 - low, 2 - medium, 3 - high)
    val dueDate: Long?,  // Add due date (milliseconds since epoch)
    val notificationTime: Long? = null, // Timestamp for the notification
    val isHoliday: Boolean = false
) : Parcelable