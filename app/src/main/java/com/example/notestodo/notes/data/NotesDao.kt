package com.example.notestodo.notes.data

import androidx.room.*
import com.example.notestodo.todo.data.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    fun getNotes(query: String, sortOrder: SortOrder): Flow<List<Notes>> =
        when (sortOrder) {
            SortOrder.BY_DATE -> getNotesSortedByDateCreated(query)
            SortOrder.BY_NAME -> getNotesSortedByName(query)
        }

    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :searchQuery || '%' ORDER BY title")
    fun getNotesSortedByName(searchQuery: String): Flow<List<Notes>>

    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :searchQuery || '%' ORDER BY created")
    fun getNotesSortedByDateCreated(searchQuery: String): Flow<List<Notes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notes: Notes)

    @Update
    suspend fun update(notes: Notes)

    @Delete
    suspend fun delete(notes: Notes)

    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM note_table")
    suspend fun getAllNotes() : List<Notes>
}