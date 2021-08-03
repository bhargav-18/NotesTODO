package com.example.notestodo.notes.ui.addeditnote

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.notestodo.R
import com.example.notestodo.databinding.FragmentAddEditNoteBinding
import com.example.notestodo.notes.util.hideKeyboard
import com.example.notestodo.todo.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditNoteFragment : Fragment(R.layout.fragment_add_edit_note) {

    private val viewModel: AddEditNoteViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val binding = FragmentAddEditNoteBinding.bind(view)

        binding.apply {
            editTextNoteTitle.setText(viewModel.noteTitle)
            editTextNoteDescription.setText(viewModel.noteDescription)
            textViewDateCreated.isVisible = viewModel.note != null
            textViewDateCreated.text = "Created: ${viewModel.note?.createdDateFormatted}"

            editTextNoteTitle.addTextChangedListener {
                viewModel.noteTitle = it.toString()
            }

            editTextNoteDescription.addTextChangedListener {
                viewModel.noteDescription = it.toString()
            }

            fabSaveNotes.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditNoteEvent.collect { event ->
                @Suppress("IMPLICIT_CAST_TO_ANY")
                when (event) {
                    is AddEditNoteViewModel.AddEditNoteEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditNoteViewModel.AddEditNoteEvent.NavigateBackWithResult -> {
                        hideKeyboard(activity as Activity)
                        binding.editTextNoteTitle.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaustive
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard(activity as Activity)
    }
}