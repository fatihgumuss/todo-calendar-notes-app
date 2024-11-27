package com.example.todo_techvidvan

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Entity(tableName = "task_item_table")
class TaskItem(
    @ColumnInfo(name = "name")var name: String,
    @ColumnInfo(name= "desc")var desc: String,
    @ColumnInfo(name = "completedDateString")var completedDateString: String?,
    @PrimaryKey(autoGenerate = true)var id: Int = 0
) {
    fun completedDate(): LocalDate? = if (completedDateString == null) null else LocalDate.parse(completedDateString, dateFormatter)
    fun isCompleted() = completedDate() != null
    fun imageResource(): Int = if(isCompleted()) R.drawable.checked_24 else R.drawable.unchecked_24
    fun imageColor(context: Context): Int = if(isCompleted()) purple(context) else black(context)

    private fun purple(context: Context) = ContextCompat.getColor(context, R.color.purple_500)
    private fun black(context: Context) = ContextCompat.getColor(context, R.color.black)

    companion object{
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE
    }
}