package com.example.todo_techvidvan

interface TaskItemListener {
    fun editTaskItem(taskItem: TaskItem)
    fun completeTaskItem(taskItem: TaskItem)
}