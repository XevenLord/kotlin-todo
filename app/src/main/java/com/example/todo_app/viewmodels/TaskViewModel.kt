package com.example.todo_app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.todo_app.models.Task
import com.example.todo_app.repository.TaskRepository
import com.example.todo_app.utils.Resource

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository = TaskRepository(application)
    val taskStateFlow get() = taskRepository.taskStateFlow
    val statusLiveData get() = taskRepository.statusLiveData

    fun getTaskList() = taskRepository.getTaskList()

    fun insertTask(task : Task) {
        taskRepository.insertTask(task)
    }

    fun deleteTask(task : Task) {
        taskRepository.deleteTask(task)
    }

    fun deleteTaskUsingId(taskId : String) {
        taskRepository.deleteTaskUsingId(taskId)
    }

    fun updateTask(task : Task) {
        taskRepository.updateTask(task)
    }

    fun updateTaskUsingId(taskId : String, title : String, description : String) {
        taskRepository.updateTaskParticularField(taskId, title, description)
    }

    fun searchTaskList(query : String) {
        taskRepository.searchTaskList(query)
    }

}