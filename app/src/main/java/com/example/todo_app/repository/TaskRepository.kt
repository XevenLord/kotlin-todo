package com.example.todo_app.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.todo_app.dao.TaskDao
import com.example.todo_app.database.TaskDatabase
import com.example.todo_app.models.Task
import com.example.todo_app.utils.Resource
import com.example.todo_app.utils.Resource.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class TaskRepository(application: Application) {

    private val taskDao: TaskDao = TaskDatabase.getInstance(application).taskDao

    fun getTaskList() =  flow {
        emit(Loading())
        try {
            val result = taskDao.getTaskList()
            emit(Success(result))
        } catch (e:Exception) {
            emit(Error(e.message.toString()))
        }
    }

    fun insertTask(task : Task) = MutableLiveData<Resource<Long>>().apply {
        postValue(Loading())
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val result = taskDao.insertTask(task)
                postValue(Success(result))
            }
        } catch (e : Exception) {
            postValue(Error(e.message.toString()))
        }
    }

}