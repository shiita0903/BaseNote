package jp.shiita.basenote.notes

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

    override fun loadNotes(forceUpdate: Boolean) {
        // Simplification for sample: a network reload will be forced on first load.
        loadNotes(forceUpdate || firstLoad, true)
        firstLoad = false
    }

    override fun updateNote(note: Note) {
        NotesDataSource.updateNote(note)
    }

    override fun deleteNote(note: Note) {
        NotesDataSource.deleteNote(note.id)
        notesView.showDeleteNote(note.titleForList)
    }

    override fun deleteAllNotes(tag: Int) {
        val empty = if (tag == 0) NotesDataSource.getNotes().isEmpty()
                    else          NotesDataSource.getNotes().any { it.tag == tag }.not()
        if (empty) {
            notesView.showNoNotesError()
            return
        }
        if (tag == 0) NotesDataSource.deleteAllNotes()
        else          NotesDataSource.deleteAllNotes(tag)
        if (!notesView.isActive) {
            return
        }
        if (tag == 0) notesView.showNoNotes()
        else          notesView.showNotes(mutableListOf())
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

    // activityから呼ぶ
    fun filterNotes(tag: Int) {
        notesView.filterNotes(tag)
    }
}
