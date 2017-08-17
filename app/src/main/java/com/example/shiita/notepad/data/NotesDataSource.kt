package com.example.shiita.notepad.data

import io.realm.Realm

object NotesDataSource {

    fun getNotes(): List<Note> {
        var notes = emptyList<Note>()
        Realm.getDefaultInstance().use { realm ->
            // 新しくNoteオブジェクトを作成
            notes = realm.where(Note::class.java).findAll().map { Note(it.title, it.content, it.id) }
        }
        return notes
    }

    fun getNote(noteId: String): Note? {
        var note: Note? = null
        Realm.getDefaultInstance().use { realm ->
            val result = realm.where(Note::class.java)
                    .equalTo(Note::id.name, noteId)
                    .findFirst()
            if (result != null) note = Note(result.title, result.content, result.id)
        }
        return note
    }

    fun saveNote(note: Note) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { realm ->
                realm.copyToRealm(note)
            }
        }
    }

    fun deleteAllNotes() {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { realm ->
                realm.deleteAll()
            }
        }
    }

    fun deleteNote(noteId: String) {
        Realm.getDefaultInstance().use { realm ->
            val result = realm.where(Note::class.java).equalTo(Note::id.name, noteId).findFirst()
            realm.executeTransaction {
                result.deleteFromRealm()
            }
        }
    }

    fun updateNote(note: Note) {
        // TODO: 消す必要はないかもしれない
        deleteNote(note.id)
        saveNote(note)
    }
}