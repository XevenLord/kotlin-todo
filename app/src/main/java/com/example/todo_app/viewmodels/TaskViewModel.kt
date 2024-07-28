package com.example.todo_app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.todo_app.models.Task
import com.example.todo_app.repository.TaskRepository
import com.example.todo_app.utils.Resource

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository = TaskRepository(application)

    fun getTaskList() = taskRepository.getTaskList()

    fun insertTask(task : Task) : MutableLiveData<Resource<Long>> {
        return taskRepository.insertTask(task)
    }

}