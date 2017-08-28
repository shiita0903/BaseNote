package jp.shiita.basenote.addeditnote

import android.text.Spannable
import jp.shiita.basenote.BasePresenter
import jp.shiita.basenote.BaseView
import jp.shiita.basenote.data.URLSpanData
import jp.shiita.basenote.util.MyURLSpan

interface AddEditNoteContract {

    interface View : BaseView<Presenter> {

        fun showEmptyNoteError()

        fun showNotesList()

        fun setTitle(title: String)

        fun setContent(content: String, urlSpanList: List<URLSpanData>)

        fun switchEditMode()

        var isActive: Boolean
    }

    interface Presenter : BasePresenter {

        fun saveNote(title: String, content: String, urlSpanList: List<URLSpanData>)

        fun populateNote()

        fun generateSearchURL(searchWord: String, searchId: Int): String

        fun getURLSpanDataList(spannable: Spannable): List<URLSpanData>

        fun addMyURLSpanToContent(spannable: Spannable, urlSpan: MyURLSpan, start: Int, end: Int)

        var isDataMissing: Boolean
    }
}