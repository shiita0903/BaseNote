package com.example.shiita.notepad.addeditnote

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.*
import android.widget.TextView
import com.example.shiita.notepad.R
import com.flipboard.bottomsheet.BottomSheetLayout


class AddEditNoteFragment : Fragment(), AddEditNoteContract.View {

    override var presenter: AddEditNoteContract.Presenter? = null

    private lateinit var title: TextView

    private lateinit var content: TextView

    private lateinit var bottomSheet: BottomSheetLayout

    override var isActive: Boolean = false
        get() = isAdded

    override fun onResume() {
        super.onResume()
        presenter?.start()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(activity.findViewById(R.id.fab_edit_note_done) as FloatingActionButton) {
            setImageResource(R.drawable.ic_done)
            setOnClickListener {
                presenter?.saveNote(title.text.toString(), content.text.toString())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.addeditnote_frag, container, false)
        with(root) {
            title = findViewById(R.id.add_edit_note_title) as TextView
            content = findViewById(R.id.add_edit_note_content) as TextView
            bottomSheet = findViewById(R.id.bottom_sheet) as BottomSheetLayout
        }
        content.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                var start = 0
                var end = content.text.length

                if (content.isFocused) {
                    start = content.selectionStart
                    end = content.selectionEnd
                }

                val word = content.text.subSequence(start, end).toString()
                val id = mapOf(R.id.search_google to 0, R.id.search_wikipedia to 1, R.id.search_weblio to 2)
                when (item?.itemId) {
                    R.id.search_google,
                    R.id.search_wikipedia,
                    R.id.search_weblio -> {
                        getFragment(word, id[item.itemId]!!).show(activity.supportFragmentManager, R.id.bottom_sheet.toString())
                        return true
                    }

                }
                return false
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menu?.add(Menu.NONE, R.id.search_google, Menu.FIRST, getString(R.string.menu_search_google))
                menu?.add(Menu.NONE, R.id.search_wikipedia, Menu.FIRST, getString(R.string.menu_search_wikipedia))
                menu?.add(Menu.NONE, R.id.search_weblio, Menu.FIRST, getString(R.string.menu_search_weblio))
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
            }
        }
        setHasOptionsMenu(true)
        return root
    }

    override fun showEmptyNoteError() {
        Snackbar.make(title, getString(R.string.empty_note_message), Snackbar.LENGTH_LONG).show()
    }

    override fun showNotesList() {
        with(activity) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun setTitle(title: String) {
        this.title.text = title
    }

    override fun setContent(content: String) {
        this.content.text = content
    }

    private fun getFragment(searchWord: String, searchId: Int): WebViewFragment {
        return activity.supportFragmentManager.findFragmentById(R.id.bottom_sheet)
                as WebViewFragment? ?:
                WebViewFragment().apply {
                    arguments = Bundle().apply {
                        putString(WebViewFragment.ARGUMENT_SEARCH_WORD, searchWord)
                        putInt(WebViewFragment.ARGUMENT_SEARCH_ID, searchId)
                    }
                }
    }

    companion object {
        val ARGUMENT_EDIT_NOTE_ID = "EDIT_NOTE_ID"
        fun newInstance() = AddEditNoteFragment()
    }
}