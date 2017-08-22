package com.example.shiita.notepad.addeditnote

import com.example.shiita.notepad.BasePresenter
import com.example.shiita.notepad.BaseView
import com.example.shiita.notepad.data.URLSpanData

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

        fun generateSearchUrl(searchWord: String, searchId: Int): String

        var isDataMissing: Boolean
    }
}