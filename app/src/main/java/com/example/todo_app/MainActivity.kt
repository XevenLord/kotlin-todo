package com.example.todo_app

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.todo_app.adapters.TaskRVVBListAdapter
import com.example.todo_app.databinding.ActivityMainBinding
import com.example.todo_app.models.Task
import com.example.todo_app.utils.Status
import com.example.todo_app.utils.StatusResult
import com.example.todo_app.utils.StatusResult.*
import com.example.todo_app.utils.clearEditText
import com.example.todo_app.utils.hideKeyBoard
import com.example.todo_app.utils.longToastShow
import com.example.todo_app.utils.setupDialog
import com.example.todo_app.utils.validateEditText
import com.example.todo_app.viewmodels.TaskViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Calendar
import java.util.Date
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val addTaskDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.add_task_dialog)
            setOnDismissListener {
                resetAddTaskDialog()
            }
        }
    }

    private val updateTaskDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.update_task_dialog)
            setOnDismissListener {
                resetUpdateTaskDialog()
            }
        }
    }

    private val loadingDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.loading_dialog)
        }
    }

    private val taskViewModel: TaskViewModel by lazy {
        ViewModelProvider(this)[TaskViewModel::class.java]
    }

    private val isListMutableLiveData = MutableLiveData<Boolean>().apply {
        postValue(true)
    }

    private var selectedImageData: ByteArray? = null

    private var updateSelectedImageData: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        // add task start
        val addCloseImg = addTaskDialog.findViewById<ImageView>(R.id.closeImg)
        addCloseImg.setOnClickListener {
            addTaskDialog.dismiss()
        }

        val addTaskDatePicker = addTaskDialog.findViewById<DatePicker>(R.id.edTaskDate)
        val addTaskTimePicker = addTaskDialog.findViewById<TimePicker>(R.id.edTaskTimePicker)


        val addETTitle = addTaskDialog.findViewById<TextInputEditText>(R.id.edTaskTitle)
        val addETTitleL = addTaskDialog.findViewById<TextInputLayout>(R.id.edTaskTitleL)

        addETTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(addETTitle, addETTitleL)
            }
        })

        val addETDesc = addTaskDialog.findViewById<TextInputEditText>(R.id.edTaskDesc)
        val addETDescL = addTaskDialog.findViewById<TextInputLayout>(R.id.edTaskDescL)

        addETDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(addETDesc, addETDescL)
            }
        })

        val selectImageBtn = addTaskDialog.findViewById<Button>(R.id.selectImageBtn)
        selectImageBtn.setOnClickListener {
            pickImage()
        }

        mainBinding.addTaskFABtn.setOnClickListener {
            clearEditText(addETTitle, addETTitleL)
            clearEditText(addETDesc, addETDescL)
            addTaskDialog.show()
        }
        val saveTaskBtn = addTaskDialog.findViewById<Button>(R.id.saveTaskBtn)
        saveTaskBtn.setOnClickListener {
            if (validateEditText(addETTitle, addETTitleL) && validateEditText(addETDesc, addETDescL)) {
                val date = combineDateAndTime(addTaskDatePicker, addTaskTimePicker)
                val newTask = Task(
                    UUID.randomUUID().toString(),
                    addETTitle.text.toString().trim(),
                    addETDesc.text.toString().trim(),
                    date,
                    selectedImageData
                )
                hideKeyBoard(it)
                addTaskDialog.dismiss()
                taskViewModel.insertTask(newTask)
            }
        }

        // Add task end

        // Update task start
        val updateTaskDatePicker = updateTaskDialog.findViewById<DatePicker>(R.id.edTaskDate)
        val updateTaskTimePicker = updateTaskDialog.findViewById<TimePicker>(R.id.edTaskTimePicker)

        val updateETTitle = updateTaskDialog.findViewById<TextInputEditText>(R.id.edTaskTitle)
        val updateETTitleL = updateTaskDialog.findViewById<TextInputLayout>(R.id.edTaskTitleL)


        updateETTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(updateETTitle, updateETTitleL)
            }
        })

        val updateETDesc = updateTaskDialog.findViewById<TextInputEditText>(R.id.edTaskDesc)
        val updateETDescL = updateTaskDialog.findViewById<TextInputLayout>(R.id.edTaskDescL)
        val updateSelectedImg = updateTaskDialog.findViewById<ImageView>(R.id.selectedTaskImage)

        updateETDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(updateETDesc, updateETDescL)
            }
        })

        val selectUpdateImageBtn = updateTaskDialog.findViewById<Button>(R.id.selectUpdateImageBtn)
        selectUpdateImageBtn.setOnClickListener {
            pickUpdateImage()
        }

        val updateCloseImg = updateTaskDialog.findViewById<ImageView>(R.id.closeImg)
        updateCloseImg.setOnClickListener { updateTaskDialog.dismiss() }

        val updateTaskBtn = updateTaskDialog.findViewById<Button>(R.id.updateTaskBtn)
        updateTaskBtn.setOnClickListener {
            if (validateEditText(updateETTitle, updateETTitleL) && validateEditText(updateETDesc, updateETDescL)) {
                updateTaskDialog.dismiss()
                Toast.makeText(this, "validated!", Toast.LENGTH_LONG).show()
                loadingDialog.show()
            }
        }
        // Update task end

        isListMutableLiveData.observe(this) {
            if (it) {
                mainBinding.taskRV.layoutManager = LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL, false
                )
                mainBinding.listOrGridImg.setImageResource(R.drawable.ic_view_module)
            } else {
                mainBinding.taskRV.layoutManager = StaggeredGridLayoutManager(
                    2, LinearLayoutManager.VERTICAL
                )
                mainBinding.listOrGridImg.setImageResource(R.drawable.ic_view_list)
            }
        }

        mainBinding.listOrGridImg.setOnClickListener {
            isListMutableLiveData.postValue(!isListMutableLiveData.value!!)
        }

        val taskRVVBListAdapter = TaskRVVBListAdapter(isListMutableLiveData) { type, position, task ->
            if (type == "delete") {
                taskViewModel.deleteTaskUsingId(task.id)

                // Restore Deleted task
                restoreDeletedTask(task)
            } else if (type == "update") {
                updateETTitle.setText(task.title)
                updateETDesc.setText(task.description)
                updateSelectedImg.setImageBitmap(task.imageData?.let { byteArrayToBitmap(it) })
                updateTaskBtn.setOnClickListener {
                    if (validateEditText(updateETTitle, updateETTitleL)
                        && validateEditText(updateETDesc, updateETDescL)
                    ) {
                        val date = combineDateAndTime(updateTaskDatePicker, updateTaskTimePicker)
                        val updatedTask = Task(
                            task.id,
                            updateETTitle.text.toString().trim(),
                            updateETDesc.text.toString().trim(),
                            date,
                            updateSelectedImageData
                        )
                        hideKeyBoard(it)
                        updateTaskDialog.dismiss()
                        taskViewModel.updateTask(updatedTask)
                    }
                }
                updateTaskDialog.show()
            }

        }
        mainBinding.taskRV.adapter = taskRVVBListAdapter
        ViewCompat.setNestedScrollingEnabled(mainBinding.taskRV, false)
        taskRVVBListAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                mainBinding.nestedScrollView.smoothScrollTo(0, positionStart)
            }
        })
        callGetTaskList(taskRVVBListAdapter)
        callSortByLiveData()
        statusCallback()

        callSearch()
    }

    private fun combineDateAndTime(datePicker: DatePicker, timePicker: TimePicker): Date {
        val calendar = Calendar.getInstance()

        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth,
            timePicker.hour, timePicker.minute)

        return calendar.time
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
            selectedImageData = bitmapToByteArray(bitmap)
            addTaskDialog.findViewById<ImageView>(R.id.selectedTaskImage).setImageBitmap(bitmap)
        }
    }

    private val pickUpdateImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
            updateSelectedImageData = bitmapToByteArray(bitmap)
            updateTaskDialog.findViewById<ImageView>(R.id.selectedTaskImage).setImageBitmap(bitmap)
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun pickUpdateImage() {
        pickUpdateImageLauncher.launch("image/*")
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


    private fun restoreDeletedTask(deletedTask: Task) {
        val snackBar = Snackbar.make(
            mainBinding.root, "Deleted '${deletedTask.title}'",
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo") {
            taskViewModel.insertTask(deletedTask)
        }
        snackBar.show()
    }

    private fun callSearch() {
        mainBinding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(query: Editable) {
                if (query.toString().isNotEmpty()) {
                    taskViewModel.searchTaskList(query.toString())
                } else {
                    callSortByLiveData()
                }
            }
        })

        mainBinding.edSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyBoard(v)
                return@setOnEditorActionListener true
            }
            false
        }

        callSortByDialog()
    }

    private fun callSortByLiveData() {
        taskViewModel.sortByLiveData.observe(this) {
            taskViewModel.getTaskList(it.second, it.first)
        }
    }

    private fun callSortByDialog() {
        var checkedItem = 2 // 2 is default item set
        val items = arrayOf("Title Ascending", "Title Descending", "Date Ascending", "Date Descending")

        mainBinding.sortImg.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Sort By")
                .setPositiveButton("Ok") { _, _ ->
                    when (checkedItem) {
                        0 -> {
                            taskViewModel.setSortBy(Pair("title", true))
                        }
                        1 -> {
                            taskViewModel.setSortBy(Pair("title", false))
                        }
                        2 -> {
                            taskViewModel.setSortBy(Pair("date", true))
                        }
                        else -> {
                            taskViewModel.setSortBy(Pair("date", false))
                        }
                    }
                }
                .setSingleChoiceItems(items, checkedItem) { _, selectedItemIndex ->
                    checkedItem = selectedItemIndex
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun resetAddTaskDialog() {
        val imageView = addTaskDialog.findViewById<ImageView>(R.id.selectedTaskImage)
        imageView.setImageDrawable(null)
        selectedImageData = null
    }

    private fun resetUpdateTaskDialog() {
        val imageView = updateTaskDialog.findViewById<ImageView>(R.id.selectedTaskImage)
        imageView.setImageDrawable(null)
        selectedImageData = null
    }

    private fun statusCallback() {
        taskViewModel.statusLiveData.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    loadingDialog.show()
                }
                Status.SUCCESS -> {
                    loadingDialog.dismiss()
                    when (it.data as StatusResult) {
                        Added -> {
                            Log.d("StatusResult", "Added")
                        }
                        Deleted -> {
                            Log.d("StatusResult", "Deleted")
                        }
                        Updated -> {
                            Log.d("StatusResult", "Updated")
                        }
                    }
                    it.message?.let { it1 -> longToastShow(it1) }
                }
                Status.ERROR -> {
                    loadingDialog.dismiss()
                    it.message?.let { it1 -> longToastShow(it1) }
                }
            }
        }
    }

    private fun callGetTaskList(taskRecyclerViewAdapter: TaskRVVBListAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            taskViewModel.taskStateFlow.collectLatest {
                when (it.status) {
                    Status.LOADING -> {
                        loadingDialog.show()
                    }
                    Status.SUCCESS -> {
                        loadingDialog.dismiss()
                        it.data?.collect { taskList ->
                            taskRecyclerViewAdapter.submitList(taskList)
                        }
                    }
                    Status.ERROR -> {
                        loadingDialog.dismiss()
                        it.message?.let { it1 -> longToastShow(it1) }
                    }
                }
            }
        }
    }
}
