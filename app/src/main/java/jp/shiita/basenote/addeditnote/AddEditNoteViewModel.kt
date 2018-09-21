package jp.shiita.basenote.addeditnote

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.text.Spannable
import android.text.SpannableStringBuilder
import io.realm.RealmList
import jp.shiita.basenote.data.Note
import jp.shiita.basenote.data.NotesRepository
import jp.shiita.basenote.data.URLSpanData
import jp.shiita.basenote.util.ClickableURLSpan
import jp.shiita.basenote.util.SingleLiveEvent
import jp.shiita.basenote.util.SingleUnitLiveEvent
import jp.shiita.basenote.util.switch
import javax.inject.Inject

class AddEditNoteViewModel @Inject constructor(
        private val repository: NotesRepository
) : ViewModel() {
    // TODO: mutableをよく考える
    val title          = MutableLiveData<String>()
    val content        = MutableLiveData<Spannable>()
    val tag            = MutableLiveData<Int>()
    val editMode       = MutableLiveData<Boolean>().apply { value = false }
    val webMode        = MutableLiveData<Boolean>().apply { value = false }
    val canGoBack      = MutableLiveData<Boolean>().apply { value = false }
    val canGoForward   = MutableLiveData<Boolean>().apply { value = false }

    val urlEvent         = SingleLiveEvent<String>()
    val noteEmptyEvent   = SingleUnitLiveEvent()
    val noteSavedEvent   = SingleLiveEvent<String>()
    val noteUpdatedEvent = SingleLiveEvent<String>()
    val noteDeleteEvent  = SingleLiveEvent<String>()
    val goBackEvent      = SingleUnitLiveEvent()
    val goForwardEvent   = SingleUnitLiveEvent()
    val popupEvent       = SingleUnitLiveEvent()

    private var noteId: String? = null
    private val isNewNote
        get() = noteId == null
    private var isDataLoaded = false
    private var spanStart = 0
    private var spanEnd = 0

    fun start(noteId: String?, noteTag: Int) {
        this.noteId = noteId
        tag.value = noteTag
        if (isNewNote) editMode.postValue(true)
        if (isNewNote || isDataLoaded) return

        noteId?.let {
            // TODO: repositoryもLiveDataにする
            populateNote(repository.getNote(it))
        }
    }

    fun saveNote() {
        val note = getCurrentNote()
        if (note.isEmpty) {
            noteEmptyEvent.call()
            return
        }

        if (isNewNote) {
            noteId = note.id
            repository.saveNote(note)
            noteSavedEvent.postValue(title.value)
        }
        else {
            repository.updateNote(note)
            noteUpdatedEvent.postValue(title.value)
        }
    }

    fun deleteNote() {
        if (!isNewNote) repository.deleteNote(noteId!!)
        noteDeleteEvent.postValue(title.value)
    }

    fun updateTag(noteTag: Int) {
        tag.postValue(noteTag)
        if (!isNewNote) {
            // 保存せずに戻ってもタグは変更されるようにする
            repository.getNote(noteId!!)?.let { note ->
                note.tag = noteTag
                repository.updateNote(note)
            }
        }
    }

    fun startSearch(start: Int, end: Int, type: SearchType) {
        val word = content.value!!.subSequence(start, end).toString()
        val url = when (type) {
            SearchType.GOOGLE    -> "http://www.google.co.jp/m/search?hl=ja&q="
            SearchType.WIKIPEDIA -> "https://ja.wikipedia.org/wiki/"
            SearchType.WEBLIO    -> "http://ejje.weblio.jp/content/"
        } + word
        setClickableURLSpan(URLSpanData(url, start, end))
        webMode.postValue(true)
        urlEvent.postValue(url)
    }

    fun removeClickableURLSpan(url: String) {
        val spannable = content.value!!
        val spans = spannable.getSpans(0, spannable.length, ClickableURLSpan::class.java)

        spans.filter { url == it.url &&
                spanStart == spannable.getSpanStart(it) &&
                spanEnd   == spannable.getSpanEnd(it) }
                .forEach { spannable.removeSpan(it) }
        content.postValue(spannable)
    }

    fun updateCurrentClickableURLSpan(url: String) {
        setClickableURLSpan(URLSpanData(url, spanStart, spanEnd))
        val note = getCurrentNote()
        if (!isNewNote && !note.isEmpty) {
            repository.updateNote(note)
        }
    }

    fun goForward() {
        if (canGoForward.value == true) goForwardEvent.call()
    }

    fun goBack() {
        if (canGoBack.value == true) goBackEvent.call()
    }

    fun switchEditMode() {
        editMode.switch()
    }

    fun stopWebMode() {
        webMode.postValue(false)
    }

    fun popupWebMenu() {
        popupEvent.call()
    }

    private fun populateNote(note: Note?) {
        if (note == null) return

        title.postValue(note.title)
        content.postValue(getSpannable(note.content, note.urlSpanList))
        tag.postValue(note.tag)
        isDataLoaded = true
    }

    private fun getCurrentNote(): Note {
        val spanArray = getURLSpanDataList().toTypedArray()
        return Note(
                title.value ?: "",
                content.value?.toString() ?: "",
                RealmList(*spanArray),
                tag.value ?: 0).apply { if (!isNewNote) id = noteId!! }
    }

    private fun getSpannable(text: String, spanDataList: List<URLSpanData>): Spannable =
            SpannableStringBuilder().apply {
                append(text)
                spanDataList.forEach { spanData ->
                    val span = ClickableURLSpan(spanData.url, ::onClick)
                    setSpan(span, spanData.start, spanData.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

    private fun setClickableURLSpan(spanData: URLSpanData) {
        val spannable = content.value!!
        val spans = spannable.getSpans(0, spannable.length, ClickableURLSpan::class.java).toMutableList()
        var start = spanData.start
        var end   = spanData.end

        // 新しいspanに被っているspanだけをフィルタして削除
        val targetSpans = spans.filterNot { start >= spannable.getSpanEnd(it) || end <= spannable.getSpanStart(it) }
        targetSpans.forEach {
            spannable.removeSpan(it)
            spans.remove(it)
        }

        // spanが隣接する場合には空白を挿入
        val insertStart = spans.any { start == spannable.getSpanEnd(it) }
        val insertEnd = spans.any { end == spannable.getSpanStart(it) }

        val ssb = SpannableStringBuilder(spannable).apply {
            if (insertStart) {
                insert(start, " ")
                start++
                end++
            }
            if (insertEnd) insert(end  , " ")
            setSpan(ClickableURLSpan(spanData.url, ::onClick), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        content.postValue(ssb)
    }

    private fun getURLSpanDataList(): List<URLSpanData> {
        val spannable = content.value ?: return emptyList()
        return spannable.getSpans(0, spannable.length, ClickableURLSpan::class.java)
                .map { span -> URLSpanData(span.url,
                        spannable.getSpanStart(span),
                        spannable.getSpanEnd(span)) }
    }

    private fun onClick(url: String) {
        if (editMode.value != false) return    // 編集中は無効化

        webMode.postValue(true)
        urlEvent.postValue(url)
        getURLSpanDataList().firstOrNull()?.let {
            spanStart = it.start
            spanEnd   = it.end
        }
    }

    enum class SearchType { GOOGLE, WIKIPEDIA, WEBLIO }
}