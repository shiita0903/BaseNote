package jp.shiita.basenote.notes

import jp.shiita.basenote.BasePresenter
import jp.shiita.basenote.BaseView
import jp.shiita.basenote.data.Note

interface NotesContract {
    interface View : BaseView<Presenter> {

        fun setLoadingIndicator(active: Boolean)

        fun filterNotes(tag: Int)

        fun showNotes(notes: MutableList<Note>)

        fun showAddNote()

        fun showDeleteNote(title: String)

        fun showDeleteAllNotes()

        fun showEditNoteUi(noteId: String)

        fun showLoadingNotesError()

        fun showNoNotesError()

        fun showNoNotes()

        var isActive: Boolean
    }

    interface Presenter : BasePresenter {

        fun loadNotes(forceUpdate: Boolean)

        fun updateNote(note: Note)

        fun deleteNote(note: Note)

        fun deleteAllNotes(tag: Int)

        fun addNewNote()

        fun editNote(requestedNote: Note)
    }
}