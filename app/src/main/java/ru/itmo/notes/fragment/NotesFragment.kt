package ru.itmo.notes.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
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

class NotesFragment(
    db: DatabaseConfig? = null,
    dirId: Long? = null
): Fragment() {

    private lateinit var db: DatabaseConfig
    private var dirId: Long = -1
    private lateinit var recyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter

    init {
        if (db != null) {
            this.db = db
        }

        if (dirId != null) {
            this.dirId = dirId
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
        if (dirId == -1L) {
            dirId = savedInstanceState?.getLong("dir_id") ?: -1
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.notes_fragment, container, false)
        recyclerView = view.findViewById(R.id.notes_list)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("dir_id", dirId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notesAdapter = NotesAdapter(mutableListOf()) { node -> onNoteClick(node) }

        recyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        if (dirId != -1L) {
            lifecycleScope.launch(Dispatchers.IO) {
                context?.let {
                    val dirs = db.noteRepository().findAllByDirIdAndStatus(dirId, NodeStatus.ACTIVE.name)
                    recyclerView.adapter = notesAdapter
                    notesAdapter.setData(dirs)
                }
            }
        }


        view.findViewById<Button>(R.id.delete_note_btn).setOnClickListener { onDeleteClick() }
        view.findViewById<Button>(R.id.add_note_btn).setOnClickListener { onCreateClick() }
        view.findViewById<Button>(R.id.edit_note_btn).setOnClickListener { onUpdateClick() }
        view.findViewById<Button>(R.id.restore_note_btn).setOnClickListener { onRestoreClick() }
    }

    private fun onNoteClick(noteNode: NoteNode) {
        if (noteNode.type() == NoteNodeType.NOTE) {
            activity?.let {
                replaceFragment(it.supportFragmentManager, NoteContentFragment(db, noteNode.id()), true)
            }
        }
    }

    private fun onDeleteClick() {
        notesAdapter.getSelected()?.let { deleteNote(it.id()) }
            ?: kotlin.run { context?.let { alert(it, "No item selected") } }
    }

    private fun deleteNote(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dir = db.noteRepository().findFirstByIdAndStatus(id, NodeStatus.ACTIVE.name)
                dir.status = NodeStatus.DELETED.name
                db.noteRepository().update(dir)
            } catch (ex: Exception) {
                context?.let { alert(it, "Dir not found") }
            }
            MainScope().launch {
                notesAdapter.removeNoteNode(id)
            }
        }
        notesAdapter.deselectAll()
    }

    private fun onRestoreClick() {
        val idInput = EditText(context).apply { hint = "ID" }
        AlertDialog.Builder(context)
            .setTitle("restore dir")
            .setMessage("What is the id of dir that you want to restore?")
            .setView(idInput)
            .setPositiveButton("Restore") { dialog, whichButton ->
                idInput.text.toString().toLongOrNull()?.let {
                    restoreDir(it)
                } ?: kotlin.run {
                    this.context?.let { alert(it, "Incorrect long id") }
                }
            }
            .setNegativeButton("Cancel") { dialog, whichButton -> }
            .show()
    }

    private fun restoreDir(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dir = db.noteRepository().findFirstByIdAndStatus(id, NodeStatus.DELETED.name)
                dir.status = NodeStatus.ACTIVE.name
                db.noteRepository().update(dir)
                MainScope().launch {
                    notesAdapter.addNoteNode(dir)
                }
            } catch (ex: Exception) {
                context?.let { alert(it, "Dir not found") }
            }
        }
    }

    private fun onCreateClick() {
        val titleInput = EditText(context).apply { hint = "Title" }
        AlertDialog.Builder(context)
            .setTitle("create dir")
            .setMessage("What title?")
            .setView(titleInput)
            .setPositiveButton("Create") { dialog, whichButton ->
                val str = titleInput.text.toString()
                createDir(str)
            }
            .setNegativeButton("Cancel") { dialog, whichButton -> }
            .show()
    }

    private fun createDir(title: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val id = db.noteRepository().insert(Note(title = title, dirId = dirId))
                val note = db.noteRepository().findFirstByIdAndStatus(id, NodeStatus.ACTIVE.name)
                MainScope().launch {
                    notesAdapter.addNoteNode(note)
                }
            } catch (ex: Exception) {
                context?.let { alert(it, "Cant create note") }
            }
        }
    }

    private fun onUpdateClick() {
        val selected = notesAdapter.getSelected()
        if (selected == null) {
            context?.let { alert(it, "No item selected") }
            return
        }

        val titleInput = EditText(context).apply {
            hint = "Title"
            selected?.let { setText(it.title()) }
        }
        AlertDialog.Builder(context)
            .setTitle("update dir")
            .setMessage("What id and new title?")
            .setView(titleInput)
            .setPositiveButton("Update") { dialog, whichButton ->

                selected?.let { updateDir(it.id(), titleInput.text.toString()) }
            }
            .setNegativeButton("Cancel") { dialog, whichButton -> }
            .show()
    }

    private fun updateDir(id: Long, title: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dir = db.noteRepository().findFirstByIdAndStatus(id, NodeStatus.ACTIVE.name)
                dir.title = title
                db.noteRepository().update(dir)
                MainScope().launch {
                    notesAdapter.replaceNoteNode(id, dir)
                }
            } catch (ex: Exception) {
                context?.let { alert(it, "Dir not found") }
            }
        }
        notesAdapter.deselectAll()
    }

}