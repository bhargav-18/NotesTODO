package com.example.notestodo.todo.ui.tasks

import androidx.lifecycle.*
import com.example.notestodo.ADD_TASK_RESULT_OK
import com.example.notestodo.EDIT_TASK_RESULT_OK
import com.example.notestodo.todo.data.PreferencesManager
import com.example.notestodo.todo.data.SortOrder
import com.example.notestodo.todo.data.Task
import com.example.notestodo.todo.data.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    state: SavedStateHandle
) : ViewModel() {
    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val tasks = tasksFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
        if (isChecked) {
            val calender = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd' 'HH:mm", Locale.getDefault())
            calender.time = sdf.parse(task.time)
            tasksEventChannel.send(TasksEvent.CancelAlarm(calender.timeInMillis.toInt()))
        } else {
            val calender = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd' 'HH:mm", Locale.getDefault())
            calender.time = sdf.parse(task.time)
            tasksEventChannel.send(
                TasksEvent.SetAlarm(
                    task,
                    calender.timeInMillis.toInt(),
                    calender
                )
            )
        }
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        val calender = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd' 'HH:mm", Locale.getDefault())
        calender.time = sdf.parse(task.time)
        tasksEventChannel.send(TasksEvent.CancelAlarm(calender.timeInMillis.toInt()))
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        val calender = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd' 'HH:mm", Locale.getDefault())
        calender.time = sdf.parse(task.time)
        tasksEventChannel.send(
            (TasksEvent.SetAlarm(
                task,
                calender.timeInMillis.toInt(),
                calender
            ))
        )
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task updated")
        }
    }

    private fun showTaskSavedConfirmationMessage(text: String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToDeleteAllCompletedScreen)
    }

    fun onDeleteAllTasksClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToDeleteAllTasksScreen)
    }

    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
        data class SetAlarm(val task: Task, val requestCode: Int, val taskTime: Calendar) :
            TasksEvent()

        data class CancelAlarm(val requestCode: Int) : TasksEvent()
        object NavigateToDeleteAllCompletedScreen : TasksEvent()
        object NavigateToDeleteAllTasksScreen : TasksEvent()
    }
}