package com.example.happyplant.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.happyplant.dataSource.TaskDataSource
import com.example.happyplant.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val adapter by lazy { TaskListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerTask.adapter = adapter
        updateList()

        insertListeners()

        // Create the notification channel
        NotificationUtils.createNotificationChannel(this)

        // Schedule the periodic work request
        scheduleTaskNotificationWorker()
    }

    private fun scheduleTaskNotificationWorker() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<TaskNotificationWorker>(
            repeatInterval = 15, // Set the desired interval in minutes
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "TaskNotificationWorker",
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )
    }

    private lateinit var addTaskActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var editTaskActivityResultLauncher: ActivityResultLauncher<Intent>
    private fun insertListeners() {
        addTaskActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                updateList()
            }
        }
        editTaskActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                updateList()
            }
        }

        binding.fab.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            addTaskActivityResultLauncher.launch(intent)
        }

        adapter.listenerEdit = { task ->
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtra(AddTaskActivity.TASK_ID, task.id)
            editTaskActivityResultLauncher.launch(intent)
        }

        adapter.listenerDelete = { task ->
            TaskDataSource.deleteTask(task)
            updateList()
        }
    }

    private fun updateList() {
        val list = TaskDataSource.getList()
        if (list.isEmpty()) {
            binding.emptyInclude.stateEmptyCs.visibility = View.VISIBLE
        } else {
            binding.emptyInclude.stateEmptyCs.visibility = View.GONE
        }
        adapter.submitList(list)
    }

    companion object {
        private const val CREATE_NEW_TASK = 1000
    }

}