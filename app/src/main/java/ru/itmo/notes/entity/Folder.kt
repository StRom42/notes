package ru.itmo.notes.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ru.itmo.notes.enums.NodeStatus
import ru.itmo.notes.enums.NodeType

@Entity(tableName = "FOLDER_TAB")
data class Folder (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "status")
    var status: String = NodeStatus.ACTIVE.name
) : NoteNode {

    @Ignore
    override fun id() = id

    @Ignore
    override fun title() = title

    @Ignore
    override fun type() = NodeType.FOLDER

}