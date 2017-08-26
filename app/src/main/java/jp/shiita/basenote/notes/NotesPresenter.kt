package jp.shiita.basenote.notes

import android.app.Activity
import jp.shiita.basenote.addeditnote.AddEditNoteActivity
import jp.shiita.basenote.data.Note
import jp.shiita.basenote.data.NotesDataSource

class NotesPresenter(private val notesView: NotesContract.View) : NotesContract.Presenter {

    private var firstLoad = true

    init {
        notesView.presenter = this
    }

    override fun start() {
        loadNotes(false)
    }

    override fun result(requestCode: Int, resultCode: Int) {
        // If a note was successfully added, show snack bar
        if (AddEditNoteActivity.REQUEST_ADD_NOTE == requestCode
                && Activity.RESULT_OK == resultCode) {
            notesView.showSuccessfullySavedMessage()
        }
    }

    override fun loadNotes(forceUpdate: Boolean) {
        // Simplification for sample: a network reload will be forced on first load.
        loadNotes(forceUpdate || firstLoad, true)
        firstLoad = false
    }

    override fun deleteNote(note: Note) {
        NotesDataSource.deleteNote(note.id)
        notesView.showDeleteNote(note.titleForList)
    }

    override fun deleteAllNotes() {
        if (NotesDataSource.getNotes().isEmpty()) {
            notesView.showNoNotesError()
            return
        }
        NotesDataSource.deleteAllNotes()
        if (!notesView.isActive) {
            return
        }
        notesView.showNoNotes()
        notesView.showDeleteAllNotes()
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the [NotesDataSource]
     * *
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private fun loadNotes(forceUpdate: Boolean, showLoadingUI: Boolean) {
        if (showLoadingUI) {
            notesView.setLoadingIndicator(true)
        }
        if (forceUpdate) {
            // キャッシュを利用していないので、特に何もしない
        }

        val notes = NotesDataSource.getNotes()

        if (!notesView.isActive) {
            return
        }
        if (showLoadingUI) {
            notesView.setLoadingIndicator(false)
        }
        processNotes(notes)
    }

    private fun processNotes(notes: List<Note>) {
        if (notes.isEmpty()) {
            notesView.showNoNotes()
        } else {
            notesView.showNotes(notes.toMutableList())
        }
    }

    override fun addNewNote() = notesView.showAddNote()

    override fun editNote(requestedNote: Note) = notesView.showEditNoteUi(requestedNote.id)
}
