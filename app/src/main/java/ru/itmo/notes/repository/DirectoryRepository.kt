package ru.itmo.notes.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import ru.itmo.notes.entity.Directory
import ru.itmo.notes.enums.NodeStatus

@Dao
interface DirectoryRepository {

    @Query("select * from directory_tab where status = :status")
    fun findAllByStatus(status: String): List<Directory>

    @Query("select * from directory_tab where id = :id and status = :status limit 1")
    fun findFirstByIdAndStatus(id: Long, status: String): Directory

    @Delete
    fun remove(directory: Directory)

    @Insert(onConflict = REPLACE)
    fun insert(directory: Directory): Long

    @Update
    fun update(directory: Directory)

}