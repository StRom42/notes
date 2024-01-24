package ru.itmo.notes.view

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.itmo.notes.R

class NoteViewHolder(noteView: View): RecyclerView.ViewHolder(noteView) {

    val titleView: TextView

    init {
        titleView = noteView.findViewById(R.id.titleView)
    }
}