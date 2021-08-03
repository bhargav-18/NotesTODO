package com.example.notestodo.notes.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.notestodo.todo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Notes::class], version = 2, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao

    class Callback @Inject constructor(
        private val database: Provider<NoteDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().notesDao()

            applicationScope.launch {
                dao.insert(
                    Notes(
                        title = "Hey friend!",
                        description = "Happy to have you in out community. " +
                                "\nWe hope you enjoy our app"
                    )
                )
                dao.insert(
                    Notes(title = "Welcome to Notes", description = "  ")
                )
                dao.insert(
                    Notes(
                        title = "Create Notes and set TODO for the day",
                        description = "You can write notes. " +
                                "\nYou can also set a TODO for the day which will remind you what you have to do whole day! " +
                                "\n Enjoy our app and give us your valuable feedback!!"
                    )
                )
                dao.insert(
                    Notes(
                        title = "untitled",
                        description = "Description"
                    )
                )
            }
        }
    }
}