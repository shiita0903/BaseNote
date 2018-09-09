package jp.shiita.basenote.notes

import jp.shiita.basenote.data.Note
import jp.shiita.basenote.data.NotesRealmDataSource
import jp.shiita.basenote.data.NotesRepository
import javax.inject.Inject

class NotesPresenter @Inject constructor(
        private val notesView: NotesContract.View,
        private val notesRepository: NotesRepository
) : NotesContract.Presenter {

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
        notesRepository.updateNote(note)
    }

    override fun deleteNote(note: Note) {
        notesRepository.deleteNote(note.id)
        notesView.showDeleteNote(note.titleForList)
    }

    override fun deleteAllNotes(tag: Int) {
        val empty = if (tag == 0) notesRepository.getNotes().isEmpty()
                    else          notesRepository.getNotes().any { it.tag == tag }.not()
        if (empty) {
            notesView.showNoNotesError()
            return
        }
        if (tag == 0) notesRepository.deleteAllNotes()
        else          notesRepository.deleteAllNotes(tag)
        if (!notesView.isActive) {
            return
        }
        if (tag == 0) notesView.showNoNotes()
        else          notesView.showNotes(notesRepository.getNotes().toMutableList())
        notesView.showDeleteAllNotes()
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the [NotesRealmDataSource]
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

        val notes = notesRepository.getNotes()

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
