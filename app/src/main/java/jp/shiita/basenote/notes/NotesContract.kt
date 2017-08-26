package jp.shiita.basenote.notes

import jp.shiita.basenote.BasePresenter
import jp.shiita.basenote.BaseView
import jp.shiita.basenote.data.Note

interface NotesContract {
    interface View : BaseView<Presenter> {

        fun setLoadingIndicator(active: Boolean)

        fun showNotes(notes: MutableList<Note>)

        fun showAddNote()

        fun showDeleteNote(title: String)

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

        fun deleteNote(note: Note)

        fun deleteAllNotes()

        fun addNewNote()

        fun editNote(requestedNote: Note)
    }
}