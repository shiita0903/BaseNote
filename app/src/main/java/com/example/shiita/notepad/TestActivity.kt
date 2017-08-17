package com.example.shiita.notepad

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.shiita.notepad.data.Note
import com.example.shiita.notepad.data.NotesDataSource

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        findViewById(R.id.getNotes).setOnClickListener {
            Log.d(TAG, "getNotes")
            NotesDataSource.getNotes().forEach {
                Log.d(TAG, "id = ${it.id}, title = ${it.title}, content = ${it.content}")
            }
        }
        findViewById(R.id.getNote).setOnClickListener {
            Log.d(TAG, "getNote")
            val note = NotesDataSource.getNote("48a4b95c-cf15-4170-b2ce-40de4fc272a8")
            Log.d(TAG, "id = ${note?.id}, title = ${note?.title}, content = ${note?.content}")
        }
        findViewById(R.id.saveNote).setOnClickListener {
            Log.d(TAG, "saveNote")
            NotesDataSource.saveNote(Note(title = "test", content = "test content"))
        }
        findViewById(R.id.deleteAllNotes).setOnClickListener {
            Log.d(TAG, "deleteAllNotes")
            NotesDataSource.deleteAllNotes()
        }
        findViewById(R.id.deleteNote).setOnClickListener {
            Log.d(TAG, "deleteNote")
            NotesDataSource.deleteNote("48a4b95c-cf15-4170-b2ce-40de4fc272a8")
        }
    }

    companion object {
        val TAG = TestActivity::class.java.simpleName
    }
}
