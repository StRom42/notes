package ru.itmo.notes.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.itmo.notes.entity.Folder
import ru.itmo.notes.entity.Note
import ru.itmo.notes.repository.FolderRepository
import ru.itmo.notes.repository.NoteRepository

@Database(
    entities = [Folder::class, Note::class],
    version = 1
)
abstract class DatabaseConfig : RoomDatabase(){
    abstract fun folderRepository(): FolderRepository
    abstract fun noteRepository(): NoteRepository

    companion object {

        @Volatile
        private var instance: DatabaseConfig? = null

        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            DatabaseConfig::class.java, "notes.db").build()
    }
}