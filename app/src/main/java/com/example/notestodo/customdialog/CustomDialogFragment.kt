package com.example.notestodo.customdialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notestodo.todo.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class CustomDialogFragment : DialogFragment() {

    private val viewModel: CustomDialogViewModel by viewModels()
    private val args: CustomDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(args.title)
            .setMessage(args.message)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Yes") { _, _ ->
                when (args.type) {
                    "note" -> {
                        viewModel.onConfirmClickNote()
                    }
                    "tasks" -> {
                        viewModel.onConfirmClick()
                    }
                    "tasksDeleteAll" -> {
                        viewModel.onConfirmDeleteAllTasks()
                    }
                }
            }
            .create()
}