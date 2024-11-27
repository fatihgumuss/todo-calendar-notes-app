package com.example.todo_techvidvan

import android.content.Context
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import com.example.todo_techvidvan.databinding.TaskItemCellBinding


class TaskItemViewHolder(
    private val context: Context,
    private val binding: TaskItemCellBinding,
    private val clickListener: TaskItemListener
): RecyclerView.ViewHolder(binding.root) {
    fun bindTaskItem(taskItem: TaskItem)
    {
        binding.name.text = taskItem.name

        if(taskItem.isCompleted()){
            binding.name.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }

        binding.completeButton.setImageResource(taskItem.imageResource())
        binding.completeButton.setColorFilter(taskItem.imageColor(context))

        binding.completeButton.setOnClickListener {
            clickListener.completeTaskItem(taskItem)
        }

        binding.taskCellContainer.setOnClickListener{
            clickListener.editTaskItem(taskItem)
        }
    }
}