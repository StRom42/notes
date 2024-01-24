package ru.itmo.notes.entity

import ru.itmo.notes.enums.NodeType

interface NoteNode {

    fun id(): Long

    fun title(): String

    fun type(): NodeType

}