package ru.itmo.notes.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ru.itmo.notes.enums.NodeStatus
import ru.itmo.notes.enums.NoteNodeType

@Entity(tableName = "NOTE_TAB")
data class Note (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "dir_id")
    var dirId: Long,

    @ColumnInfo(name = "status")
    var status: String = NodeStatus.ACTIVE.name,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "content")
    var content: String = "",

    @ColumnInfo(name = "image_url")
    var imageUrl: String = ""
): NoteNode {

    @Ignore
    override fun id() = id

    @Ignore
    override fun title() = title

    @Ignore
    override fun type() = NoteNodeType.NOTE

}