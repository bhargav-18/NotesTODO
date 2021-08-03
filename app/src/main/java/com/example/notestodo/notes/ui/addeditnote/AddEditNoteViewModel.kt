package com.example.notestodo.notes.ui.addeditnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notestodo.ADD_NOTE_RESULT_OK
import com.example.notestodo.EDIT_NOTE_RESULT_OK
import com.example.notestodo.notes.data.Notes
import com.example.notestodo.notes.data.NotesDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val notesDao: NotesDao,
    private val state: SavedStateHandle
) : ViewModel() {

    val note = state.get<Notes>("note")

    var noteTitle = state.get<String>("noteTitle") ?: note?.title ?: ""
        set(value) {
            field = value
            state.set("noteTitle", value)
        }

    var noteDescription = state.get<String>("noteDescription") ?: note?.description ?: ""
        set(value) {
            field = value
            state.set("noteDescription", value)
        }

    private val addEditNoteEventChannel = Channel<AddEditNoteEvent>()
    val addEditNoteEvent = addEditNoteEventChannel.receiveAsFlow()

    fun onSaveClick() {

        if (noteDescription.isBlank() && noteTitle.isBlank()) {
            showInvalidInputMessage("Title cannot be empty")
            return
        }
        if (noteDescription.isNotBlank() && noteTitle.isBlank()) {
            noteTitle = "untitled"
        }

        if (note != null) {
            val updatedNote =
                note.copy(title = noteTitle, description = noteDescription)
            updateNote(updatedNote)
        } else {
            val newNotes = Notes(title = noteTitle, description = noteDescription)
            createNote(newNotes)
        }
    }

    private fun createNote(notes: Notes) = viewModelScope.launch {
        notesDao.insert(notes)
        addEditNoteEventChannel.send(AddEditNoteEvent.NavigateBackWithResult(ADD_NOTE_RESULT_OK))
    }

    private fun updateNote(notes: Notes) = viewModelScope.launch {
        notesDao.update(notes)
        addEditNoteEventChannel.send(AddEditNoteEvent.NavigateBackWithResult(EDIT_NOTE_RESULT_OK))
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditNoteEventChannel.send(AddEditNoteEvent.ShowInvalidInputMessage(text))
    }

    sealed class AddEditNoteEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditNoteEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditNoteEvent()
    }
}