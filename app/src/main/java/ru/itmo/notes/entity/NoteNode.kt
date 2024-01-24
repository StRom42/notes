package ru.itmo.notes.entity

import ru.itmo.notes.enums.NoteNodeType

interface NoteNode {

    fun id(): Long

    fun title(): String

    fun type(): NoteNodeType

}