package com.example.todo_app.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todo_app.databinding.ViewTaskGridLayoutBinding
import com.example.todo_app.databinding.ViewTaskListLayoutBinding
import com.example.todo_app.models.Task
import java.text.SimpleDateFormat
import java.util.Locale

class TaskRVVBListAdapter(
    private val isList: MutableLiveData<Boolean>,
    private val deleteUpdateCallback: (type: String, position: Int, task: Task) -> Unit
) : ListAdapter<Task, RecyclerView.ViewHolder>(DiffCallback()) {

    class ListTaskViewHolder(
        private val viewTaskListLayoutBinding: ViewTaskListLayoutBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(viewTaskListLayoutBinding.root) {

        fun bind(task: Task, deleteUpdateCallback: (type: String, position: Int, task: Task) -> Unit) {
            viewTaskListLayoutBinding.titleTxt.text = task.title
            viewTaskListLayoutBinding.descrTxt.text = task.description

            val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss a", Locale.getDefault())
            viewTaskListLayoutBinding.dateTxt.text = dateFormat.format(task.date)

            task.imageData?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                viewTaskListLayoutBinding.taskImage.setImageBitmap(bitmap)
            }

            adjustImageSize(viewTaskListLayoutBinding.taskImage, context)

            viewTaskListLayoutBinding.deleteImg.setOnClickListener {
                if (adapterPosition != -1) {
                    deleteUpdateCallback("delete", adapterPosition, task)
                }
            }
            viewTaskListLayoutBinding.editImg.setOnClickListener {
                if (adapterPosition != -1) {
                    deleteUpdateCallback("update", adapterPosition, task)
                }
            }
        }

        private fun adjustImageSize(imageView: ImageView, context: Context) {
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val imageWidth = screenWidth / 5
            imageView.layoutParams.width = imageWidth
            imageView.layoutParams.height = imageWidth
            imageView.requestLayout()
        }
    }

    class GridTaskViewHolder(
        private val viewTaskGridLayoutBinding: ViewTaskGridLayoutBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(viewTaskGridLayoutBinding.root) {

        fun bind(task: Task, deleteUpdateCallback: (type: String, position: Int, task: Task) -> Unit) {
            viewTaskGridLayoutBinding.titleTxt.text = task.title
            viewTaskGridLayoutBinding.descrTxt.text = task.description

            val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss a", Locale.getDefault())
            viewTaskGridLayoutBinding.dateTxt.text = dateFormat.format(task.date)

            task.imageData?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                viewTaskGridLayoutBinding.taskImage.setImageBitmap(bitmap)
            }

            adjustImageSize(viewTaskGridLayoutBinding.taskImage, context)

            viewTaskGridLayoutBinding.deleteImg.setOnClickListener {
                if (adapterPosition != -1) {
                    deleteUpdateCallback("delete", adapterPosition, task)
                }
            }
            viewTaskGridLayoutBinding.editImg.setOnClickListener {
                if (adapterPosition != -1) {
                    deleteUpdateCallback("update", adapterPosition, task)
                }
            }
        }

        private fun adjustImageSize(imageView: ImageView, context: Context) {
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val imageWidth = screenWidth / 5
            imageView.layoutParams.width = imageWidth
            imageView.layoutParams.height = imageWidth
            imageView.requestLayout()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 1) { // Grid_Item
            GridTaskViewHolder(
                ViewTaskGridLayoutBinding.inflate(inflater, parent, false),
                parent.context
            )
        } else { // List_Item
            ListTaskViewHolder(
                ViewTaskListLayoutBinding.inflate(inflater, parent, false),
                parent.context
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val task = getItem(position)

        if (isList.value!!) {
            (holder as ListTaskViewHolder).bind(task, deleteUpdateCallback)
        } else {
            (holder as GridTaskViewHolder).bind(task, deleteUpdateCallback)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isList.value!!) {
            0 // List_Item
        } else {
            1 // Grid_Item
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
