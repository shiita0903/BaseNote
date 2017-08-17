package com.example.shiita.notepad.notes

import com.example.shiita.notepad.BasePresenter
import com.example.shiita.notepad.BaseView
import com.example.shiita.notepad.data.Note

interface NotesContract {
    interface View : BaseView<Presenter> {

        fun setLoadingIndicator(active: Boolean)

        fun showNotes(notes: List<Note>)

        fun showAddNote()

        fun showDeleteAllNotes()

        fun showEditNoteUi(noteId: String)

        fun showLoadingNotesError()

        fun showNoNotesError()

        fun showNoNotes()

        fun showSuccessfullySavedMessage()

        var isActive: Boolean
    }

    interface Presenter : BasePresenter {

        fun result(requestCode: Int, resultCode: Int)

        fun loadNotes(forceUpdate: Boolean)

        fun deleteAllNotes()

        fun addNewNote()

        fun editNote(requestedNote: Note)
    }
}