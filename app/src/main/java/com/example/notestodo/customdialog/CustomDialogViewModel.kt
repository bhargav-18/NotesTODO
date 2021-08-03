package com.example.notestodo.customdialog

import androidx.lifecycle.ViewModel
import com.example.notestodo.notes.data.NotesDao
import com.example.notestodo.todo.data.TaskDao
import com.example.notestodo.todo.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomDialogViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val notesDao: NotesDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    fun onConfirmClick() = applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }

    fun onConfirmClickNote() = applicationScope.launch {
        notesDao.deleteAllNotes()
    }

    fun onConfirmDeleteAllTasks() = applicationScope.launch {
        taskDao.deleteAllTasks()
    }

}