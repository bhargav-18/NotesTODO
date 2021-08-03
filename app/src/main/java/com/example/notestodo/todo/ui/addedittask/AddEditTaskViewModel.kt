package com.example.notestodo.todo.ui.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notestodo.ADD_TASK_RESULT_OK
import com.example.notestodo.EDIT_TASK_RESULT_OK
import com.example.notestodo.todo.data.Task
import com.example.notestodo.todo.data.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val state: SavedStateHandle
) : ViewModel() {

    val task = state.get<Task>("task")

    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }

    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    var taskTime = state.get<String>("taskTime") ?: task?.time ?: LocalDate.now()
        .toString() + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        set(value) {
            field = value
            state.set("taskTime", value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }

        if (task != null) {
            val updatedTask =
                task.copy(name = taskName, important = taskImportance, time = taskTime)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance, time = taskTime)
            createTask(newTask)
        }
    }

    private fun createTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        val calender = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd' 'HH:mm", Locale.getDefault())
        calender.time = sdf.parse(taskTime)
        addEditTaskEventChannel.send(
            AddEditTaskEvent.SetAlarm(
                task,
                calender.timeInMillis.toInt(),
                calender
            )
        )
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        val calender = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd' 'HH:mm", Locale.getDefault())
        calender.time = sdf.parse(taskTime)
        addEditTaskEventChannel.send(AddEditTaskEvent.CancelAlarm(calender.timeInMillis.toInt()))
        addEditTaskEventChannel.send(
            AddEditTaskEvent.SetAlarm(
                task,
                calender.timeInMillis.toInt(),
                calender
            )
        )
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
        data class SetAlarm(val task: Task, val requestCode: Int, val taskTime: Calendar) :
            AddEditTaskEvent()

        data class CancelAlarm(val requestCode: Int) : AddEditTaskEvent()
    }
}