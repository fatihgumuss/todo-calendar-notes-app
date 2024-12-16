package com.example.thenotesapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.thenotesapp.model.Note
import com.example.thenotesapp.model.ToDoItem

@Database(entities = [Note::class, ToDoItem::class], version = 7)
abstract class NoteDatabase: RoomDatabase() {

    abstract fun getNoteDao(): NoteDao
    abstract fun getToDoDao(): ToDoDao

    companion object{
        @Volatile
        private var instance: NoteDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?:
        synchronized(LOCK){
            instance ?:
            createDatabase(context).also{
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                NoteDatabase::class.java,
                "note_db"
            ).fallbackToDestructiveMigration()
                .build()
    }
}