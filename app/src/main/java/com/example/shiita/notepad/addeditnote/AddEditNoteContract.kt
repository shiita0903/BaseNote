package com.example.shiita.notepad.addeditnote

import android.text.Spannable
import com.example.shiita.notepad.BasePresenter
import com.example.shiita.notepad.BaseView
import com.example.shiita.notepad.data.URLSpanData
import com.example.shiita.notepad.util.MyURLSpan

interface AddEditNoteContract {

    interface View : BaseView<Presenter> {

        fun showEmptyNoteError()

        fun showNotesList()

        fun setTitle(title: String)

        fun setContent(content: String, urlSpanList: List<URLSpanData>)

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