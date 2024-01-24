package ru.itmo.notes.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import ru.itmo.notes.entity.Folder

@Dao
interface FolderRepository {

    @Query("select * from folder_tab where status = :status")
    fun findAllByStatus(status: String): List<Folder>

    @Query("select * from folder_tab where id = :id and status = :status limit 1")
    fun findFirstByIdAndStatus(id: Long, status: String): Folder

    @Delete
    fun remove(folder: Folder)

    @Insert(onConflict = REPLACE)
    fun insert(folder: Folder): Long

    @Update
    fun update(folder: Folder)

}