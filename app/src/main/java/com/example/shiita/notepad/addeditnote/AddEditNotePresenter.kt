package com.example.shiita.notepad.addeditnote

import com.example.shiita.notepad.data.Note
import com.example.shiita.notepad.data.NotesDataSource
import com.example.shiita.notepad.data.URLSpanData
import io.realm.RealmList

class AddEditNotePresenter(
        private val noteId: String?,
        val addEditNoteView: AddEditNoteContract.View,
        override var isDataMissing: Boolean
) : AddEditNoteContract.Presenter {

    init {
        addEditNoteView.presenter = this
    }

    override fun start() {
        if (noteId != null && isDataMissing) {
            populateNote()
        }
    }

    override fun saveNote(title: String, content: String, urlSpanList: List<URLSpanData>) {
        if (noteId == null) {
            createNote(title, content, urlSpanList)
        } else {
            updateNote(title, content, urlSpanList)
        }
    }

    override fun populateNote() {
        if (noteId == null) {
            throw RuntimeException("populateNote() was called but note is new.")
        }
        val note = NotesDataSource.getNote(noteId)
        if (addEditNoteView.isActive) {
            if (note != null) {
                addEditNoteView.run {
                    setTitle(note.title)
                    setContent(note.content, note.urlSpanList)
                }
                isDataMissing = false
            }
        }
    }

    override fun generateSearchUrl(searchWord: String, searchId: Int): String = when (searchId) {
        0 -> "http://www.google.co.jp/m/search?hl=ja&q="
        1 -> "https://ja.wikipedia.org/wiki/"
        2 -> "http://ejje.weblio.jp/content/"
        else -> error("SEARCH_IDが間違っています")
    } + searchWord

    private fun createNote(title: String, content: String, urlSpanList: List<URLSpanData>) {
        val newNote = Note(title, content, RealmList(*urlSpanList.toTypedArray()))
        if (newNote.isEmpty) {
            addEditNoteView.showEmptyNoteError()
        } else {
            NotesDataSource.saveNote(newNote)
            addEditNoteView.showNotesList()
        }
    }

    private fun updateNote(title: String, content: String, urlSpanList: List<URLSpanData>) {
        if (noteId == null) {
            throw RuntimeException("updateNote() was called but note is new.")
        }
        NotesDataSource.updateNote(Note(title, content, RealmList(*urlSpanList.toTypedArray()), noteId))
        addEditNoteView.showNotesList() // After an edit, go back to the list.
    }
}