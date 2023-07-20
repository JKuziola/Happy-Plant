package com.example.happyplant.ui

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.happyplant.dataSource.TaskDataSource
import com.example.happyplant.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TaskNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private fun addDaysToTaskDate(task: Task): Task {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val originalDate = dateFormat.parse(task.date)

        if (originalDate != null) {
            calendar.time = originalDate
            calendar.add(Calendar.DAY_OF_MONTH, task.cycle.toInt())

            val newDate = calendar.time
            task.date = dateFormat.format(newDate)

            return task
        }

        return task
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        val tasks = TaskDataSource.getList()
        val currentTime = Calendar.getInstance().time

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        Log.d("Worker", "looking for tasks at $currentTime")
        for (task in tasks) {
            val taskDate = dateFormat.parse(task.date)
            val taskTime = timeFormat.parse(task.hour)

            if (taskDate != null && taskTime != null) {
                val taskDateTime = mergeDateTime(taskDate, taskTime)

                if (taskDateTime <= currentTime) {
                    Log.d("Worker","found the task")
                    val title = "${task.title} needs watering"
                    val message = "Water ${task.title} or else..."
                    showNotification(applicationContext, title, message)
                    addDaysToTaskDate(task)
                }
            }
        }

        Result.success()
    }

    private fun mergeDateTime(date: Date, time: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val timeCalendar = Calendar.getInstance()
        timeCalendar.time = time

        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))

        return calendar.time
    }

    private fun showNotification(context: Context, title: String, message: String) {
        // Use the NotificationUtils.showNotification method from the previous solution
        NotificationUtils.showNotification(context, title, message)
    }
}
