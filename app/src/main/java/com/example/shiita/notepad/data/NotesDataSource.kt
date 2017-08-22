package com.example.shiita.notepad.data

import io.realm.Realm
import io.realm.RealmList

object NotesDataSource {

    fun getNotes(): List<Note> {
        var notes = emptyList<Note>()
        Realm.getDefaultInstance().use { realm ->
            // 新しくNoteオブジェクトを作成
            notes = realm.where(Note::class.java).findAll().map { Note(it.title, it.content, it.urlSpanList, it.id) }
        }
        return notes
    }

    fun getNote(noteId: String): Note? {
        var note: Note? = null
        Realm.getDefaultInstance().use { realm ->
            val result = realm.where(Note::class.java)
                    .equalTo(Note::id.name, noteId)
                    .findFirst()
            if (result != null) {
                // 入れ子になっているURLSpanDataも作成し直す必要がある
                val spanList = result.urlSpanList.map { URLSpanData(it.url, it.start, it.end) }
                note = Note(result.title, result.content, RealmList(*spanList.toTypedArray()), result.id)
            }
        }
        return note
    }

    fun saveNote(note: Note) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { realm ->
                val realmNote = realm.createObject(Note::class.java, note.id)
                realmNote.title = note.title
                realmNote.content = note.content
                note.urlSpanList.forEach {
                    realm.copyToRealm(it)
                    realmNote.urlSpanList.add(it)
                }
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