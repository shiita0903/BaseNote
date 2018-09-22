package jp.shiita.basenote.addeditnote

import android.arch.lifecycle.LiveData
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
    val title           = MutableLiveData<String>()
    val content         = MutableLiveData<Spannable>()
    val webViewUrl:       LiveData<String>    get() = _webViewUrl
    val tag:              LiveData<Int>       get() = _tag
    val editMode:         LiveData<Boolean>   get() = _editMode
    val webMode:          LiveData<Boolean>   get() = _webMode
    val canGoBack:        LiveData<Boolean>   get() = _canGoBack
    val canGoForward:     LiveData<Boolean>   get() = _canGoForward
    val guidelinePercent: LiveData<Float>     get() = _guidelinePercent

    val noteEmptyEvent:   LiveData<Unit>      get() = _noteEmptyEvent
    val noteSavedEvent:   LiveData<String>    get() = _noteSavedEvent
    val noteUpdatedEvent: LiveData<String>    get() = _noteUpdatedEvent
    val noteDeleteEvent:  LiveData<String>    get() = _noteDeleteEvent
    val goBackEvent:      LiveData<Unit>      get() = _goBackEvent
    val goForwardEvent:   LiveData<Unit>      get() = _goForwardEvent
    val popupEvent:       LiveData<Unit>      get() = _popupEvent

    private val _webViewUrl       = MutableLiveData<String>()
    private val _tag              = MutableLiveData<Int>()
    private val _editMode         = MutableLiveData<Boolean>().apply { value = false }
    private val _webMode          = MutableLiveData<Boolean>().apply { value = false }
    private val _canGoBack        = MutableLiveData<Boolean>().apply { value = false }
    private val _canGoForward     = MutableLiveData<Boolean>().apply { value = false }
    private val _guidelinePercent = MutableLiveData<Float>().apply { value = 1f }

    private val _noteEmptyEvent   = SingleUnitLiveEvent()
    private val _noteSavedEvent   = SingleLiveEvent<String>()
    private val _noteUpdatedEvent = SingleLiveEvent<String>()
    private val _noteDeleteEvent  = SingleLiveEvent<String>()
    private val _goBackEvent      = SingleUnitLiveEvent()
    private val _goForwardEvent   = SingleUnitLiveEvent()
    private val _popupEvent       = SingleUnitLiveEvent()

    private var noteId: String? = null
    private val isNewNote: Boolean get() = noteId == null
    private val currentNote: Note
        get() = Note(
                title.value ?: "",
                content.value?.toString() ?: "",
                RealmList(*getURLSpanDataList().toTypedArray()),
                tag.value ?: 0).apply { if (!isNewNote) id = noteId!! }

    private var beforePercent = 0.5f
    private var spanStart = 0
    private var spanEnd = 0

    fun start(id: String?, tag: Int) {
        noteId = id
        _tag.postValue(tag)
        if (isNewNote) {
            _editMode.postValue(true)
            return
        }

        noteId?.let {
            // TODO: repositoryもLiveDataにする
            populateNote(repository.getNote(it))
        }
    }

    fun saveNote() {
        val note = currentNote
        if (note.isEmpty) {
            _noteEmptyEvent.call()
            return
        }

        if (isNewNote) {
            noteId = note.id
            repository.saveNote(note)
            _noteSavedEvent.postValue(note.titleForList)
        }
        else {
            repository.updateNote(note)
            _noteUpdatedEvent.postValue(note.titleForList)
        }
    }

    fun deleteNote() {
        val note = currentNote
        if (!isNewNote) repository.deleteNote(note.id)
        _noteDeleteEvent.postValue(note.titleForList)
    }

    fun updateTag(noteTag: Int) {
        _tag.postValue(noteTag)
        if (!isNewNote) {
            // 保存せずに戻ってもタグは変更されるようにする
            repository.getNote(noteId!!)?.let { note ->
                note.tag = noteTag
                repository.updateNote(note)
            }
        }
    }

    fun startSearch(start: Int, end: Int, type: SearchType) {
        val word = content.value?.subSequence(start, end)?.toString() ?: return
        val url = when (type) {
            SearchType.GOOGLE    -> "http://www.google.co.jp/m/search?hl=ja&q="
            SearchType.WIKIPEDIA -> "https://ja.wikipedia.org/wiki/"
            SearchType.WEBLIO    -> "http://ejje.weblio.jp/content/"
        } + word
        setClickableURLSpan(URLSpanData(url, start, end))
        _webMode.postValue(true)
        _guidelinePercent.postValue(beforePercent)
        _webViewUrl.postValue(url)
    }

    fun updateCurrentSpan() {
        val url = webViewUrl.value ?: return
        setClickableURLSpan(URLSpanData(url, spanStart, spanEnd))
    }

    fun removeCurrentSpan() {
        val url = webViewUrl.value ?: return
        removeClickableURLSpan(url)
    }

    fun setCanGoForward(canGoForward: Boolean) {
        _canGoForward.postValue(canGoForward)
    }

    fun setCanGoBack(canGoBack: Boolean) {
        _canGoBack.postValue(canGoBack)
    }

    fun goForward() {
        if (canGoForward.value == true) _goForwardEvent.call()
    }

    fun goBack() {
        if (canGoBack.value == true) _goBackEvent.call()
    }

    fun updateUrl(url: String?) {
        _webViewUrl.postValue(url)
    }

    fun setGuidelinePercent(percent: Float) {
        beforePercent = percent
        _guidelinePercent.postValue(percent)
    }

    fun switchEditMode() {
        _editMode.switch()
    }

    fun stopWebMode() {
        _webMode.postValue(false)
        _guidelinePercent.postValue(1f)
    }

    fun popupWebMenu() {
        _popupEvent.call()
    }

    private fun populateNote(note: Note?) {
        if (note == null) return

        title.postValue(note.title)
        content.postValue(getSpannable(note.content, note.urlSpanList))
        _tag.postValue(note.tag)
    }

    private fun getSpannable(text: String, spanDataList: List<URLSpanData>): Spannable =
            SpannableStringBuilder().apply {
                append(text)
                spanDataList.forEach { spanData ->
                    val span = ClickableURLSpan(spanData.url, ::onClick)
                    setSpan(span, spanData.start, spanData.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

    private fun removeClickableURLSpan(url: String) {
        val spannable = content.value ?: return
        val spans = spannable.getSpans(0, spannable.length, ClickableURLSpan::class.java)

        spans.filter { url == it.url &&
                spanStart == spannable.getSpanStart(it) &&
                spanEnd   == spannable.getSpanEnd(it) }
                .forEach { spannable.removeSpan(it) }
        content.postValue(spannable)
    }

    private fun setClickableURLSpan(spanData: URLSpanData) {
        val spannable = content.value ?: return
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

        _webMode.postValue(true)
        _guidelinePercent.postValue(beforePercent)
        _webViewUrl.postValue(url)
        getURLSpanDataList().firstOrNull()?.let {
            spanStart = it.start
            spanEnd   = it.end
        }
    }

    enum class SearchType { GOOGLE, WIKIPEDIA, WEBLIO }
}