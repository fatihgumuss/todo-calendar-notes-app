package com.example.thenotesapp.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.thenotesapp.R
import com.example.thenotesapp.databinding.TodoItemLayoutBinding
import com.example.thenotesapp.model.ToDoItem
import com.example.thenotesapp.viewmodel.ToDoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ToDoAdapter(private val toDoViewModel: ToDoViewModel) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    inner class ToDoViewHolder(val binding: TodoItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<ToDoItem>() {
        override fun areItemsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        return ToDoViewHolder(
            TodoItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
    val currentToDo = differ.currentList[position]

    holder.binding.apply {
        if (currentToDo.isHoliday) {
            // Design for holiday items
            todoTextView.text = currentToDo.task
            todoDescriptionTextView.visibility = View.GONE
            todoCheckBox.visibility = View.GONE
            deleteTodoImageView.visibility = View.GONE
            todoDueDateTextView.visibility = View.GONE
            todoTextView.setTextColor(Color.BLACK)
        } else {
            // Design for regular todo items
            todoTextView.text = currentToDo.task
            todoDescriptionTextView.text = currentToDo.description
            todoDescriptionTextView.visibility = View.VISIBLE
            todoCheckBox.visibility = View.VISIBLE
            deleteTodoImageView.visibility = View.VISIBLE
            todoTextView.setTextColor(Color.BLACK)
            if (currentToDo.isCompleted) {
                todoTextView.apply {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // Strike through the text
                    setTextColor(Color.GRAY) // Change text color to gray
                }
                todoDescriptionTextView.setTextColor(Color.GRAY) // Change description color to gray
            } else {
                todoTextView.apply {
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() // Remove strike through
                    setTextColor(Color.BLACK) // Change text color back to black
                }
                todoDescriptionTextView.setTextColor(Color.BLACK) // Change description color back to black
            }
            val priorityColor = when (currentToDo.priority) {
                0 -> Color.parseColor("#D4F1C5")
                1 -> Color.parseColor("#FFD88C")
                2 -> Color.parseColor("#FF6B6B")
                else -> Color.GRAY
            }
            priorityView.setBackgroundColor(priorityColor)
            if (currentToDo.dueDate != null) {
                val dueDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dueDate = Date(currentToDo.dueDate)
                val dueDateString = dueDateFormat.format(dueDate)
                todoDueDateTextView.text = "Due Date: $dueDateString"
            } else {
                todoDueDateTextView.text = "No Due Date"
            }
        }

        todoCheckBox.setOnCheckedChangeListener(null)
        todoCheckBox.isChecked = currentToDo.isCompleted
        todoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val updatedToDo = currentToDo.copy(isCompleted = isChecked)
            toDoViewModel.updateToDo(updatedToDo)
        }
        deleteTodoImageView.setOnClickListener {
            toDoViewModel.deleteToDoById(currentToDo.id)
        }
        root.setOnClickListener {
            onItemClickListener?.let { it(currentToDo) }
        }
    }
}
    override fun getItemCount(): Int {
        return differ.currentList.size
    }
    private var onItemClickListener: ((ToDoItem) -> Unit)? = null
    fun setOnItemClickListener(listener: (ToDoItem) -> Unit) {
        onItemClickListener = listener
    }
}