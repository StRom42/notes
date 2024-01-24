package ru.itmo.notes.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
import ru.itmo.notes.entity.Directory
import ru.itmo.notes.entity.NoteNode
import ru.itmo.notes.enums.NodeStatus
import ru.itmo.notes.enums.NoteNodeType
import ru.itmo.notes.view.NotesAdapter


class DirsFragment(
    db: DatabaseConfig? = null
): Fragment() {

    private lateinit var db: DatabaseConfig
    private lateinit var recyclerView: RecyclerView
    private lateinit var dirsAdapter: NotesAdapter

    init {
        if (db != null) {
            this.db = db
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this::db.isInitialized) return
        db = context?.let {
            Room.databaseBuilder(
                it,
                DatabaseConfig::class.java, "notes.db"
            ).build()
        }!!
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dirs_fragment, container, false)
        recyclerView = view.findViewById(R.id.dirs_list)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dirsAdapter = NotesAdapter(mutableListOf()) { node -> onDirClick(node) }
        recyclerView.adapter = dirsAdapter

        recyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        lifecycleScope.launch(Dispatchers.IO) {
            val dirs = db.directoryRepository().findAllByStatus(NodeStatus.ACTIVE.name)
            dirsAdapter.setData(dirs)
        }


        view.findViewById<Button>(R.id.delete_dir_btn).setOnClickListener { onDeleteClick() }
        view.findViewById<Button>(R.id.add_dir_btn).setOnClickListener { onCreateClick() }
        view.findViewById<Button>(R.id.edit_dir_btn).setOnClickListener { onUpdateClick() }
        view.findViewById<Button>(R.id.restore_dir_btn).setOnClickListener { onRestoreClick() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    private fun onDirClick(noteNode: NoteNode, ) {
        if (noteNode.type() == NoteNodeType.DIRECTORY) {
            activity?.let {
                replaceFragment(it.supportFragmentManager, NotesFragment(db, noteNode.id()), false)
            }
        }
    }

    private fun onDeleteClick() {
        dirsAdapter.getSelected()?.let { deleteDir(it.id()) }
            ?: kotlin.run { context?.let { alert(it, "No item selected") } }
    }

    private fun deleteDir(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dir = db.directoryRepository().findFirstByIdAndStatus(id, NodeStatus.ACTIVE.name)
                dir.status = NodeStatus.DELETED.name
                db.directoryRepository().update(dir)
            } catch (ex: Exception) {
                context?.let { alert(it, "Dir not found") }
            }
            MainScope().launch {
                dirsAdapter.removeNoteNode(id)
            }
        }
        dirsAdapter.deselectAll()
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
                val dir = db.directoryRepository().findFirstByIdAndStatus(id, NodeStatus.DELETED.name)
                dir.status = NodeStatus.ACTIVE.name
                db.directoryRepository().update(dir)
                MainScope().launch {
                    dirsAdapter.addNoteNode(dir)
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
                val id = db.directoryRepository().insert(Directory(title = title))
                val dir = db.directoryRepository().findFirstByIdAndStatus(id, NodeStatus.ACTIVE.name)
                MainScope().launch {
                    dirsAdapter.addNoteNode(dir)
                }
            } catch (ex: Exception) {
                context?.let { alert(it, "Cant create dir") }
            }
        }
    }

    private fun onUpdateClick() {
        val selected = dirsAdapter.getSelected()
        if (selected == null) {
            context?.let { alert(it, "No item selected") }
            return
        }

        val titleInput = EditText(context).apply {
            hint = "Title"
            selected?.let { setText(it.title()) }
        }
        val updateInput = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleInput)
        }
        AlertDialog.Builder(context)
            .setTitle("update dir")
            .setMessage("What is new title?")
            .setView(updateInput)
            .setPositiveButton("Update") { dialog, whichButton ->
                  selected?.let { updateDir(it.id(), titleInput.text.toString()) }
            }
            .setNegativeButton("Cancel") { dialog, whichButton -> }
            .show()
    }

    private fun updateDir(id: Long, title: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dir = db.directoryRepository().findFirstByIdAndStatus(id, NodeStatus.ACTIVE.name)
                dir.title = title
                db.directoryRepository().update(dir)
                MainScope().launch {
                    dirsAdapter.replaceNoteNode(id, dir)
                }
            } catch (ex: Exception) {
                context?.let { alert(it, "Dir not found") }
            }
        }
        dirsAdapter.deselectAll()
    }


}