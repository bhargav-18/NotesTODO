package com.example.notestodo.todo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.notestodo.todo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().taskDao()

            applicationScope.launch {
                dao.insert(Task(name = "Wash the dishes", time = LocalDate.of(LocalDate.now().year,LocalDate.now().month,LocalDate.now().dayOfMonth).toString() + " " + LocalTime.of(8,10).toString()))
                dao.insert(Task(name = "Do the laundry", time = LocalDate.of(LocalDate.now().year,LocalDate.now().month,LocalDate.now().dayOfMonth,).toString() + " " + LocalTime.of(9,0).toString()))
                dao.insert(Task(name = "Buy groceries", important = true,time = LocalDate.of(LocalDate.now().year,LocalDate.now().month,LocalDate.now().dayOfMonth).toString() + " " + LocalTime.of(10,10).toString()))
                dao.insert(Task(name = "Prepare food", completed = true, time = LocalDate.of(LocalDate.now().year,LocalDate.now().month,LocalDate.now().dayOfMonth).toString() + " " + LocalTime.of(13,15).toString()))
                dao.insert(Task(name = "Visit grandma", completed = true, time = LocalDate.of(LocalDate.now().year,LocalDate.now().month,LocalDate.now().dayOfMonth).toString() + " " + LocalTime.of(15,20).toString()))
                dao.insert(Task(name = "Repair my bike", time = LocalDate.of(LocalDate.now().year,LocalDate.now().month,LocalDate.now().dayOfMonth).toString() + " " + LocalTime.of(18,10).toString()))
                dao.insert(Task(name = "Call Elon Musk", time = LocalDate.of(LocalDate.now().year,LocalDate.now().month,LocalDate.now().dayOfMonth).toString() + " " + LocalTime.of(19,10).toString()))
            }
        }
    }
}