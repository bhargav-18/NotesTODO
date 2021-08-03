package com.example.notestodo.notes.ui.note

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notestodo.R
import com.example.notestodo.databinding.FragmentNoteBinding
import com.example.notestodo.notes.data.Notes
import com.example.notestodo.notes.ui.note.NotesViewModel.NotesEvent.NavigateToAddNoteScreen
import com.example.notestodo.notes.util.exhaustive
import com.example.notestodo.notes.util.onQueryTextChanged
import com.example.notestodo.todo.data.SortOrder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class NotesFragment : Fragment(R.layout.fragment_note), NotesAdapter.OnItemClickListener {
    val viewModel: NotesViewModel by viewModels()

    private lateinit var searchView: SearchView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNoteBinding.bind(view)

        val notesAdapter = NotesAdapter(this)

        binding.apply {
            recyclerViewNotes.apply {
                adapter = notesAdapter
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                setHasFixedSize(true)
            }

            fabAddNotes.setOnClickListener {
                viewModel.onAddNewNoteClick()
            }

        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        viewModel.notes.observe(viewLifecycleOwner) {
            notesAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.notesEvent.collect { event ->
                when (event) {
                    is NotesViewModel.NotesEvent.ShowUndoDeleteNoteMessage -> {
                        Snackbar.make(requireView(), "Note deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.notes)
                            }.show()
                    }
                    is NavigateToAddNoteScreen -> {
                        val action =
                            NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(
                                null,
                                "New Note"
                            )
                        findNavController().navigate(action)
                    }
                    is NotesViewModel.NotesEvent.NavigateToEditNoteScreen -> {
                        val action =
                            NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(
                                event.notes,
                                "Edit Note"
                            )
                        findNavController().navigate(action)
                    }
                    is NotesViewModel.NotesEvent.ShowNoteSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    is NotesViewModel.NotesEvent.NavigateToDeleteAllScreen -> {
                        val action =
                            NotesFragmentDirections.actionGlobalCustomDialogFragment(
                                "Do you want to delete all notes?",
                                "note",
                                "Delete"
                            )
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onItemClick(notes: Notes) {
        viewModel.onNoteSelected(notes)
    }

    override fun onEditPopupClick(notes: Notes) {
        viewModel.onNoteSelected(notes)
    }

    override fun onDeletePopupClick(notes: Notes) {
        viewModel.onDeleteClicked(notes)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_notes, menu)

        val searchItem = menu.findItem(R.id.action_search_notes)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name_notes -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created_notes -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_delete_all_notes -> {
                viewModel.onDeleteAllClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }
}