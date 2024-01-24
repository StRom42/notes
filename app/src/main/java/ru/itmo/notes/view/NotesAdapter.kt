package ru.itmo.notes.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.itmo.notes.R
import ru.itmo.notes.entity.NoteNode

class NotesAdapter(
    private val nodes: MutableList<NoteNode>,
    private val onClickListener: (NoteNode) -> Unit
) : RecyclerView.Adapter<NoteViewHolder>() {

    private var selected: NoteNode? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.note_row, parent, false)
        return NoteViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: NoteViewHolder, position: Int) {
        val noteNode = nodes[position]
        viewHolder.titleView.text = "${noteNode.id()}: ${noteNode.type().name} | ${noteNode.title()}"
        viewHolder.titleView.setOnClickListener { onClickListener(noteNode) }
        if (selected?.type() == noteNode.type() && selected?.id() == noteNode.id()) {
            viewHolder.titleView.setTextColor(Color.RED)
        } else {
            viewHolder.titleView.setTextColor(Color.BLACK)
        }
        viewHolder.titleView.setOnLongClickListener { onSelect(noteNode) }
    }

    fun onSelect(noteNode: NoteNode): Boolean {
        selected = noteNode
        notifyDataSetChanged()

        return true
    }

    fun getSelected(): NoteNode? {
        return selected
    }

    fun deselectAll() {
        selected = null
        notifyDataSetChanged()
    }

    fun setData(newNodes: List<NoteNode>) {
        nodes.clear()
        nodes.addAll(newNodes)
    }

    fun addNoteNode(node: NoteNode) {
        nodes.add(node)
        notifyDataSetChanged()
    }

    fun removeNoteNode(id: Long) {
        val index = nodes.indexOfFirst { it.id() == id }
        nodes.removeAt(index)
        notifyDataSetChanged()
    }

    fun replaceNoteNode(id: Long, noteNode: NoteNode) {
        val index = nodes.indexOfFirst { it.id() == id }
        nodes[index] = noteNode
        notifyDataSetChanged()
    }

    override fun getItemCount() = nodes.size

}