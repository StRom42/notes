package ru.itmo.notes.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.itmo.notes.entity.Note

@Dao
interface NoteRepository {

    @Query("select * from note_tab where folder_id = :folderId and status = :status")
    fun findAllByDirIdAndStatus(folderId: Long, status: String): List<Note>

    @Query("select * from note_tab where id = :id and status = :status limit 1")
    fun findFirstByIdAndStatus(id: Long, status: String): Note

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: Note): Long

    @Delete
    fun remove(note: Note)

    @Update
    fun update(note: Note)

}