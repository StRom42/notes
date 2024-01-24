package ru.itmo.notes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import ru.itmo.notes.common.addFragment
import ru.itmo.notes.config.DatabaseConfig
import ru.itmo.notes.fragment.DirsFragment

class MainActivity : AppCompatActivity() {

    lateinit var db: DatabaseConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(
            applicationContext,
            DatabaseConfig::class.java, "notes.db"
        ).build()

        setContentView(R.layout.main_layout)

        addFragment(supportFragmentManager, DirsFragment(db), false)
    }

}