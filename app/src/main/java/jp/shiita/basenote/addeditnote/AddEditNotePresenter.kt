package jp.shiita.basenote.addeditnote

import android.text.Spannable
import android.text.SpannableStringBuilder
import io.realm.RealmList
import jp.shiita.basenote.data.Note
import jp.shiita.basenote.data.NotesRepository
import jp.shiita.basenote.data.URLSpanData
import jp.shiita.basenote.util.MyURLSpan
import javax.inject.Inject

class AddEditNotePresenter @Inject constructor(
        private var noteId: String?,
        defaultTag: Int,
        private val addEditNoteView: AddEditNoteContract.View,
        override var isDataMissing: Boolean,
        private val notesRepository: NotesRepository
) : AddEditNoteContract.Presenter {

    init {
        addEditNoteView.presenter = this
        addEditNoteView.setNoteTag(defaultTag)
    }

    override fun start() {
        if (noteId != null && isDataMissing) {
            populateNote()
            addEditNoteView.switchEditMode(save = false)
        }
    }

    override fun saveNote(title: String, content: String, urlSpanList: List<URLSpanData>, tag: Int) {
        if (noteId == null) {
            createNote(title, content, urlSpanList, tag)
        } else {
            updateNote(title, content, urlSpanList, tag)
        }
    }

    override fun updateTag(tag: Int) {
        if (noteId != null) {
            val note = notesRepository.getNote(noteId!!)?.apply { this.tag = tag }
            if (note != null)
                notesRepository.updateNote(note)
        }
    }

    override fun deleteNote() {
        if (noteId != null) {
            notesRepository.deleteNote(noteId!!)
        }
        addEditNoteView.finishActivity()
    }

    override fun populateNote() {
        if (noteId == null) {
            throw RuntimeException("populateNote() was called but note is new.")
        }
        val note = notesRepository.getNote(noteId!!)
        if (addEditNoteView.isActive) {
            if (note != null) {
                addEditNoteView.run {
                    setTitle(note.title)
                    setContent(note.content, note.urlSpanList)
                    setNoteTag(note.tag)
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

    override fun findURLSpanData(spannable: Spannable, url: String): URLSpanData? {
        val list = getURLSpanDataList(spannable)
        // 同じURLが複数あった時にはテキストの最初のURLのみが更新されるが、まあいいことにする
        return list.firstOrNull { it.url == url }
    }

    override fun addURLSpan(spannable: Spannable, urlSpan: MyURLSpan, start: Int, end: Int): Spannable {
        val spans = spannable.getSpans(0, spannable.length, MyURLSpan::class.java)

        // 新しいspanに被っているspanだけをフィルタして削除
        spans.filterNot { start >= spannable.getSpanEnd(it) || end <= spannable.getSpanStart(it) }
                .forEach { spannable.removeSpan(it) }

        // spanが隣接する場合には空白を挿入
        val insertStart = spans.any{ start == spannable.getSpanEnd(it) }
        val insertEnd = spans.any{ end == spannable.getSpanStart(it) }
        var s = start
        var e = end
        val sb = SpannableStringBuilder(spannable)
        if (insertStart) {
            sb.insert(s, " ")
            s++
            e++
        }
        if (insertEnd) {
            sb.insert(e, " ")
        }
        sb.setSpan(urlSpan, s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return sb
    }

    override fun removeURLSpan(spannable: Spannable, urlSpan: MyURLSpan, start: Int, end: Int): Spannable {
        val spans = spannable.getSpans(0, spannable.length, MyURLSpan::class.java)
        spans.filter { urlSpan.url == it.url && start == spannable.getSpanStart(it) && end == spannable.getSpanEnd(it)}
                .forEach { spannable.removeSpan(it) }
        return spannable
    }

    private fun createNote(title: String, content: String, urlSpanList: List<URLSpanData>, tag: Int) {
        val newNote = Note(title, content, RealmList(*urlSpanList.toTypedArray()), tag)
        if (!newNote.isEmpty) {
            noteId = newNote.id     // idを更新しないと、別idで複数保存可能になってしまう
            notesRepository.saveNote(newNote)
            addEditNoteView.showSaveNote()
        }
    }

    private fun updateNote(title: String, content: String, urlSpanList: List<URLSpanData>, tag: Int) {
        if (noteId == null) {
            throw RuntimeException("updateNote() was called but note is new.")
        }
        val newNote = Note(title, content, RealmList(*urlSpanList.toTypedArray()), tag, id = noteId!!)
        if (newNote.isEmpty)
            notesRepository.deleteNote(newNote.id)
        else {
            notesRepository.updateNote(newNote)
            addEditNoteView.showSaveNote()
        }
    }
}