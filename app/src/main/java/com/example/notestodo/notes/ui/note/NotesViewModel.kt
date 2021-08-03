package com.example.notestodo.notes.ui.note

import androidx.lifecycle.*
import com.example.notestodo.ADD_NOTE_RESULT_OK
import com.example.notestodo.EDIT_NOTE_RESULT_OK
import com.example.notestodo.notes.data.Notes
import com.example.notestodo.notes.data.NotesDao
import com.example.notestodo.todo.data.PreferencesManager
import com.example.notestodo.todo.data.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesDao: NotesDao,
    private val preferencesManager: PreferencesManager,
    private val state: SavedStateHandle
) : ViewModel() {
    val searchQuery = state.getLiveData("searchQuery", "")

    private val preferencesFlow = preferencesManager.preferencesFlowNotes

    private val notesEventChannel = Channel<NotesEvent>()
    val notesEvent = notesEventChannel.receiveAsFlow()

    private val notesFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        notesDao.getNotes(query, filterPreferences.sortOrder)
    }

    val notes = notesFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onNoteSelected(notes: Notes) = viewModelScope.launch {
        notesEventChannel.send(NotesEvent.NavigateToEditNoteScreen(notes))
    }

    fun onDeleteClicked(notes: Notes) = viewModelScope.launch {
        notesDao.delete(notes)
        notesEventChannel.send(NotesEvent.ShowUndoDeleteNoteMessage(notes))
    }

    fun onUndoDeleteClick(notes: Notes) = viewModelScope.launch {
        notesDao.insert(notes)
    }

    fun onAddNewNoteClick() = viewModelScope.launch {
        notesEventChannel.send(NotesEvent.NavigateToAddNoteScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_NOTE_RESULT_OK -> showNoteSavedConfirmationMessage("Note added")
            EDIT_NOTE_RESULT_OK -> showNoteSavedConfirmationMessage("Note updated")
        }
    }

    private fun showNoteSavedConfirmationMessage(text: String) = viewModelScope.launch {
        notesEventChannel.send(NotesEvent.ShowNoteSavedConfirmationMessage(text))
    }

    fun onDeleteAllClick() = viewModelScope.launch {
        notesEventChannel.send(NotesEvent.NavigateToDeleteAllScreen)
    }

    sealed class NotesEvent {
        object NavigateToAddNoteScreen : NotesEvent()
        data class NavigateToEditNoteScreen(val notes: Notes) : NotesEvent()
        data class ShowUndoDeleteNoteMessage(val notes: Notes) : NotesEvent()
        data class ShowNoteSavedConfirmationMessage(val msg: String) : NotesEvent()
        object NavigateToDeleteAllScreen : NotesEvent()
    }
}