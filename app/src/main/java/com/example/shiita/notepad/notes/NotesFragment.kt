package com.example.shiita.notepad.notes

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.*
import android.widget.*
import com.example.shiita.notepad.R
import com.example.shiita.notepad.addeditnote.AddEditNoteActivity
import com.example.shiita.notepad.addeditnote.AddEditNoteFragment
import com.example.shiita.notepad.data.Note
import com.example.shiita.notepad.util.snackbarLong
import java.util.*

class NotesFragment : Fragment(), NotesContract.View {

    override var presenter: NotesContract.Presenter? = null
    private lateinit var noNotesView: View
    private lateinit var noNoteMainView: TextView
    private lateinit var noNoteAddView: TextView
    private lateinit var notesView: LinearLayout
    override var isActive: Boolean = false
        get() = isAdded

    /**
     * Listener for clicks on notes in the ListView.
     */
    internal var itemListener: NoteItemListener = object : NoteItemListener {
        override fun onNoteClick(clickedNote: Note) {
            presenter?.editNote(clickedNote)
        }
    }

    private val listAdapter = NotesAdapter(ArrayList<Note>(0), itemListener)

    override fun onResume() {
        super.onResume()
        presenter?.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter?.result(requestCode, resultCode)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.notes_frag, container, false)

        // Set up notes view
        with(root) {
            val listView = (findViewById(R.id.notes_list) as ListView).apply {
                adapter = listAdapter
            }

            // Set up progress indicator
            (root.findViewById(R.id.refresh_layout) as ScrollChildSwipeRefreshLayout).run {
                setColorSchemeColors(
                        android.support.v4.content.ContextCompat.getColor(activity, R.color.colorPrimary),
                        android.support.v4.content.ContextCompat.getColor(activity, R.color.colorAccent),
                        android.support.v4.content.ContextCompat.getColor(activity, R.color.colorPrimaryDark)
                )
                // Set the scrolling view in the custom SwipeRefreshLayout.
                scrollUpChild = listView
                setOnRefreshListener { presenter?.loadNotes(false) }
            }

            notesView = findViewById(R.id.notesLL) as LinearLayout

            // Set up  no notes view
            noNotesView = findViewById(R.id.noNotes)
            noNoteMainView = findViewById(R.id.noNotesMain) as TextView
            noNoteAddView = (findViewById(R.id.noNotesAdd) as TextView).also {
                it.setOnClickListener { showAddNote() }
            }
        }

        // Set up floating action button
        (activity.findViewById(R.id.fab_add_note) as FloatingActionButton).apply {
            setImageResource(R.drawable.ic_add)
            setOnClickListener { presenter?.addNewNote() }
        }
        setHasOptionsMenu(true)

        return root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_refresh -> presenter?.loadNotes(true)
            R.id.menu_clear -> presenter?.deleteAllNotes()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.notes_fragment_menu, menu)
    }

    override fun setLoadingIndicator(active: Boolean) {
        val root = view ?: return
        with(root.findViewById(R.id.refresh_layout) as SwipeRefreshLayout) {
            // Make sure setRefreshing() is called after the layout is done with everything else.
            post { isRefreshing = active }
        }
    }

    override fun showNotes(notes: List<Note>) {
        listAdapter.notes = notes
        notesView.visibility = View.VISIBLE
        noNotesView.visibility = View.GONE
    }

    override fun showNoNotes() = showNoNotesViews(resources.getString(R.string.no_notes_all), true)

    override fun showSuccessfullySavedMessage() = showMessage(getString(R.string.successfully_saved_note_message))

    private fun showNoNotesViews(mainText: String, showAddView: Boolean) {
        notesView.visibility = View.GONE
        noNotesView.visibility = View.VISIBLE
        noNoteMainView.text = mainText
        noNoteAddView.visibility = if (showAddView) View.VISIBLE else View.GONE
    }

    override fun showAddNote() {
        val intent = Intent(context, AddEditNoteActivity::class.java)
        startActivityForResult(intent, AddEditNoteActivity.REQUEST_ADD_NOTE)
    }

    override fun showDeleteAllNotes() = showMessage(getString(R.string.all_notes_deleted))

    override fun showEditNoteUi(noteId: String) {
        val intent = Intent(context, AddEditNoteActivity::class.java).apply {
            putExtra(AddEditNoteFragment.ARGUMENT_EDIT_NOTE_ID, noteId)
        }
        startActivity(intent)
    }

    override fun showLoadingNotesError() = showMessage(getString(R.string.loading_notes_error))

    override fun showNoNotesError() = showMessage(getString(R.string.no_notes_error))

    private fun showMessage(message: String) = view?.snackbarLong(message) ?: Unit

    private class NotesAdapter(notes: List<Note>, private val itemListener: NoteItemListener) : BaseAdapter() {

        var notes: List<Note> = notes
            set(notes) {
                field = notes
                notifyDataSetChanged()
            }

        override fun getCount() = notes.size

        override fun getItem(i: Int) = notes[i]

        override fun getItemId(i: Int) = i.toLong()

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            val rowView = view ?: LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.note_item, viewGroup, false)

            val note = getItem(i)

            (rowView.findViewById(R.id.title) as TextView).run {
                text = note.titleForList.trimStart()
            }

            rowView.setOnClickListener { itemListener.onNoteClick(note) }
            return rowView
        }
    }

    interface NoteItemListener {
        fun onNoteClick(clickedNote: Note)
    }

    companion object {
        fun newInstance() = NotesFragment()
    }
}