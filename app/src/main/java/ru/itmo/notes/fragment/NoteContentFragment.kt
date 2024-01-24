package ru.itmo.notes.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.itmo.notes.R
import ru.itmo.notes.common.alert
import ru.itmo.notes.common.replaceFragment
import ru.itmo.notes.config.DatabaseConfig
import ru.itmo.notes.entity.Note
import ru.itmo.notes.entity.NoteNode
import ru.itmo.notes.enums.NodeStatus
import ru.itmo.notes.enums.NoteNodeType
import ru.itmo.notes.view.NotesAdapter

class NoteContentFragment(
    db: DatabaseConfig? = null,
    noteId: Long? = null
): Fragment() {

    private lateinit var db: DatabaseConfig
    private var noteId: Long = -1
    private lateinit var contentEdit: EditText
    private lateinit var saveButton: Button
    private lateinit var note: Note

    init {
        if (db != null) {
            this.db = db
        }

        if (noteId != null) {
            this.noteId = noteId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this::db.isInitialized) {
            db = context?.let {
                Room.databaseBuilder(
                    it,
                    DatabaseConfig::class.java, "notes.db"
                ).build()
            }!!
        }
        if (noteId == -1L) {
            noteId = savedInstanceState?.getLong("dir_id") ?: -1
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.note_content_fragment, container, false)
        contentEdit = view.findViewById(R.id.content_edit)
        saveButton = view.findViewById(R.id.save_note_btn)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("note_id", noteId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                note = db.noteRepository().findFirstByIdAndStatus(noteId, NodeStatus.ACTIVE.name)

                view.findViewById<TextView>(R.id.title_edit).text = note.title
                contentEdit.setText(note.content)
            } catch (ex: Exception) {
                context?.let { alert(it, "Note not found") }
            }
        }

        view.findViewById<Button>(R.id.save_note_btn).setOnClickListener { onSaveClick() }
    }

    private fun onSaveClick() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                note.content = contentEdit.text.toString()
                db.noteRepository().update(note)
                context?.let { alert(it, "Saved!") }
            } catch (ex: Exception) {
                context?.let { alert(it, "Cant update content") }
            }
        }
    }

}