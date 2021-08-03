package com.example.notestodo.todo.ui.addedittask

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.notestodo.R
import com.example.notestodo.databinding.FragmentAddEditTaskBinding
import com.example.notestodo.notes.util.hideKeyboard
import com.example.notestodo.todo.AlarmReceiver
import com.example.notestodo.todo.data.Task
import com.example.notestodo.todo.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {
    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            editTextTaskName.setText(viewModel.taskName)
            checkBoxImportant.isChecked = viewModel.taskImportance
            checkBoxImportant.jumpDrawablesToCurrentState()
            textViewTime.text = viewModel.taskTime

            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            selectTimeButton.setOnClickListener {
                val mcurrentTime: Calendar = Calendar.getInstance()
                val hour: Int = mcurrentTime.get(Calendar.HOUR_OF_DAY)
                val minute: Int = mcurrentTime.get(Calendar.MINUTE)
                val mTimePicker = TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        textViewTime.text = "Time : $selectedHour:$selectedMinute"
                        val time = LocalDate.now().toString() + " " + LocalTime.of(
                            selectedHour,
                            selectedMinute
                        )
                        viewModel.taskTime = time
                    },
                    hour,
                    minute,
                    true
                )

                mTimePicker.setTitle("Select Time")
                mTimePicker.show()
            }

            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                @Suppress("IMPLICIT_CAST_TO_ANY")
                when (event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        hideKeyboard(activity as Activity)
                        binding.editTextTaskName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.SetAlarm -> {

                        setAlarm(event.requestCode, event.taskTime, event.task)
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.CancelAlarm -> {
                        cancelAlarm(event.requestCode)
                    }
                }.exhaustive
            }
        }
    }

    private fun setAlarm(id: Int, calendar: Calendar, task: Task) {
        val alarmManager = this.context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(this.context, AlarmReceiver::class.java)
        intent.putExtra("notificationTitle", task.name)
        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_ONE_SHOT)

        alarmManager?.set(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
        )
    }

    private fun cancelAlarm(id: Int) {
        val alarmManager = this.context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(this.context, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_ONE_SHOT)

        alarmManager?.cancel(pendingIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard(activity as Activity)
    }
}