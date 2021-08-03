package com.example.notestodo.todo.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "task_table")
@Parcelize
data class Task(
    val name: String,
    val important: Boolean = false,
    val completed: Boolean = false,
    val time: String = LocalDate.now().toString() + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
    ) : Parcelable