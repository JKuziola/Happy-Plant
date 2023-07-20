package com.example.happyplant.dataSource

import com.example.happyplant.model.Task
import com.example.happyplant.ui.TaskListAdapter

object TaskDataSource {
    private val list = arrayListOf<Task>()

    fun getList() = list.toList()

    fun insertTask(task: Task) {
        val existingTask = list.find { it.id == task.id }
        if (existingTask != null) {
            // Update existing task
            val index = list.indexOf(existingTask)
            list[index] = task
        } else {
            // Add new task
            val newTask = task.copy(id = list.size + 1)
            list.add(newTask)
        }
    }

    fun findById(taskId: Int) = list.find { it.id == taskId }

    fun deleteTask(task: Task) {
        list.remove(task)
    }

}