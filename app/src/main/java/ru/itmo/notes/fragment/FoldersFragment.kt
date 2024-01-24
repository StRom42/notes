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
import ru.itmo.notes.entity.Folder
import ru.itmo.notes.entity.NoteNode
import ru.itmo.notes.enums.NodeStatus
import ru.itmo.notes.enums.NodeType
import ru.itmo.notes.view.NotesAdapter


class FoldersFragment(
    db: DatabaseConfig? = null
): Fragment() {

    private lateinit var db: DatabaseConfig
    private lateinit var recyclerView: RecyclerView
    private lateinit var foldersAdapter: NotesAdapter

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

        foldersAdapter = NotesAdapter(mutableListOf()) { node -> onFolderClick(node) }
        recyclerView.adapter = foldersAdapter

        recyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        lifecycleScope.launch(Dispatchers.IO) {
            val dirs = db.folderRepository().findAllByStatus(NodeStatus.ACTIVE.name)
            foldersAdapter.setData(dirs)
        }


        view.findViewById<Button>(R.id.delete_dir_btn).setOnClickListener { onDeleteClick() }
        view.findViewById<Button>(R.id.add_dir_btn).setOnClickListener { onCreateClick() }
        view.findViewById<Button>(R.id.edit_dir_btn).setOnClickListener { onUpdateClick() }
        view.findViewById<Button>(R.id.restore_dir_btn).setOnClickListener { onRestoreClick() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    private fun onFolderClick(noteNode: NoteNode, ) {
        if (noteNode.type() == NodeType.FOLDER) {
            activity?.let {
                replaceFragment(it.supportFragmentManager, NotesFragment(db, noteNode.id()), false)
            }
        }
    }

    private fun onDeleteClick() {
        foldersAdapter.getSelected()?.let { deleteFolder(it.id()) }
            ?: kotlin.run { context?.let { alert(it, "No item selected") } }
    }

    private fun deleteFolder(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val folder = db.folderRepository().findFirstByIdAndStatus(id, NodeStatus.ACTIVE.name)
                folder.status = NodeStatus.DELETED.name
                db.folderRepository().update(folder)
            } catch (ex: Exception) {
                context?.let { alert(it, "Folder not found") }
            }
            MainScope().launch {
                foldersAdapter.removeNoteNode(id)
            }
        }
        foldersAdapter.deselectAll()
    }

    private fun onRestoreClick() {
        val idInput = EditText(context).apply { hint = "ID" }
        AlertDialog.Builder(context)
            .setTitle("restore folder")
            .setMessage("What is id of folder that you want to restore?")
            .setView(idInput)
            .setPositiveButton("Restore") { dialog, whichButton ->
                idInput.text.toString().toLongOrNull()?.let {
                    restoreFolder(it)
                } ?: kotlin.run {
                    this.context?.let { alert(it, "Incorrect long id") }
                }
            }
            .setNegativeButton("Cancel") { dialog, whichButton -> }
            .show()
    }

    private fun restoreFolder(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val folder = db.folderRepository().findFirstByIdAndStatus(id, NodeStatus.DELETED.name)
                folder.status = NodeStatus.ACTIVE.name
                db.folderRepository().update(folder)
                MainScope().launch {
                    foldersAdapter.addNode(folder)
                }
            } catch (ex: Exception) {
                context?.let { alert(it, "Dir not found") }
            }
        }
    }

    private fun onCreateClick() {
        val titleInput = EditText(context).apply { hint = "Title" }
        AlertDialog.Builder(context)
            .setTitle("create folder")
            .setMessage("What title?")
            .setView(titleInput)
            .setPositiveButton("Create") { dialog, whichButton ->
                val str = titleInput.text.toString()
                createFolder(str)
            }
            .setNegativeButton("Cancel") { dialog, whichButton -> }
            .show()
    }

    private fun createFolder(title: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val id = db.folderRepository().insert(Folder(title = title))
                val folder = db.folderRepository().findFirstByIdAndStatus(id, NodeStatus.ACTIVE.name)
                MainScope().launch {
                    foldersAdapter.addNode(folder)
                }
            } catch (ex: Exception) {
                context?.let { alert(it, "Cant create folder") }
            }
        }
    }

    private fun onUpdateClick() {
        val selected = foldersAdapter.getSelected()
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
            .setTitle("update folder")
            .setMessage("What is new title?")
            .setView(updateInput)
            .setPositiveButton("Update") { dialog, whichButton ->
                  selected?.let { updateFolder(it.id(), titleInput.text.toString()) }
            }
            .setNegativeButton("Cancel") { dialog, whichButton -> }
            .show()
    }

    private fun updateFolder(id: Long, title: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val folder = db.folderRepository().findFirstByIdAndStatus(id, NodeStatus.ACTIVE.name)
                folder.title = title
                db.folderRepository().update(folder)
                MainScope().launch {
                    foldersAdapter.replaceNode(id, folder)
                }
            } catch (ex: Exception) {
                context?.let { alert(it, "Folder not found") }
            }
        }
        foldersAdapter.deselectAll()
    }


}