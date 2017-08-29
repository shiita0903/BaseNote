package jp.shiita.basenote.addeditnote

import android.text.Spannable
import io.realm.RealmList
import jp.shiita.basenote.data.Note
import jp.shiita.basenote.data.NotesDataSource
import jp.shiita.basenote.data.URLSpanData
import jp.shiita.basenote.util.MyURLSpan

class AddEditNotePresenter(
        private var noteId: String?,
        private val addEditNoteView: AddEditNoteContract.View,
        override var isDataMissing: Boolean
) : AddEditNoteContract.Presenter {

    init {
        addEditNoteView.presenter = this
    }

    override fun start() {
        if (noteId != null && isDataMissing) {
            populateNote()
            addEditNoteView.switchEditMode(save = false)
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
        val note = NotesDataSource.getNote(noteId!!)
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

    override fun generateSearchURL(searchWord: String, searchId: Int): String = when (searchId) {
        0 -> "http://www.google.co.jp/m/search?hl=ja&q="
        1 -> "https://ja.wikipedia.org/wiki/"
        2 -> "http://ejje.weblio.jp/content/"
        else -> error("SEARCH_IDが間違っています")
    } + searchWord

    override fun getURLSpanDataList(spannable: Spannable): List<URLSpanData> {
        return spannable
                .getSpans(0, spannable.length, MyURLSpan::class.java)
                ?.map { span -> URLSpanData(span.url, spannable.getSpanStart(span), spannable.getSpanEnd(span)) }
                ?: emptyList()
    }

    override fun addMyURLSpanToContent(spannable: Spannable, urlSpan: MyURLSpan, start: Int, end: Int) {
        // 新しいspanに被っているspanだけをフィルタして削除
        spannable.getSpans(0, spannable.length, MyURLSpan::class.java)
                // ２つのspanの間が一文字空いていればフィルターされない
                .filterNot { start > spannable.getSpanEnd(it) || end < spannable.getSpanStart(it) }
                .forEach { spannable.removeSpan(it) }
        spannable.setSpan(urlSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun createNote(title: String, content: String, urlSpanList: List<URLSpanData>) {
        val newNote = Note(title, content, RealmList(*urlSpanList.toTypedArray()))
        if (!newNote.isEmpty) {
            noteId = newNote.id     // idを更新しないと、別idで複数保存可能になってしまう
            NotesDataSource.saveNote(newNote)
            addEditNoteView.showSaveNote()
        }
    }

    private fun updateNote(title: String, content: String, urlSpanList: List<URLSpanData>) {
        if (noteId == null) {
            throw RuntimeException("updateNote() was called but note is new.")
        }
        val newNote = Note(title, content, RealmList(*urlSpanList.toTypedArray()), id = noteId!!)
        if (newNote.isEmpty)
            NotesDataSource.deleteNote(newNote.id)
        else {
            NotesDataSource.updateNote(newNote)
            addEditNoteView.showSaveNote()
        }
    }
}