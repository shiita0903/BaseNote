package jp.shiita.basenote.notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import jp.shiita.basenote.R
import jp.shiita.basenote.addeditnote.AddEditNoteActivity
import jp.shiita.basenote.addeditnote.AddEditNoteFragment
import jp.shiita.basenote.data.Note
import jp.shiita.basenote.util.snackbarLong

class NotesFragment : Fragment(), NotesContract.View {

    override var presenter: NotesContract.Presenter? = null
    private lateinit var notesView: View
    private lateinit var noNotesView: View
    private lateinit var noNoteMainView: TextView
    private lateinit var noNoteAddView: TextView
    override var isActive: Boolean = false
        get() = isAdded

    // RecyclerViewのアダプタ
    private val notesAdapter by lazy {
        NotesAdapter(context, ArrayList(0), object : NotesAdapter.NoteItemListener {
            override fun onNoteItemClick(clickedNote: Note) = presenter?.editNote(clickedNote) ?: Unit
        }, object : NotesAdapter.NoteItemMenuListener {
            override fun onNoteItemMenuClick(clickedNote: Note, menuId: Int) {
                when(menuId) {
                    R.id.note_item_menu_delete ->  {
                        presenter?.deleteNote(clickedNote)
                        deleteNoteFromAdapter(clickedNote)    // アダプタのノート一覧からも削除する必要がある
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        presenter?.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
        = presenter?.result(requestCode, resultCode) ?: Unit

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.notes_frag, container, false)

        // Set up notes view
        with(root) {
            val recyclerView = (findViewById(R.id.notes_recycler_view) as RecyclerView).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = notesAdapter
                val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                addItemDecoration(dividerItemDecoration)
            }

            // Set up progress indicator
            (root.findViewById(R.id.refresh_layout) as ScrollChildSwipeRefreshLayout).run {
                setColorSchemeColors(
                        android.support.v4.content.ContextCompat.getColor(context, R.color.colorPrimary),
                        android.support.v4.content.ContextCompat.getColor(context, R.color.colorAccent),
                        android.support.v4.content.ContextCompat.getColor(context, R.color.colorPrimaryDark)
                )
                // Set the scrolling view in the custom SwipeRefreshLayout.
                scrollUpChild = recyclerView
                setOnRefreshListener { presenter?.loadNotes(false) }
            }

            notesView = findViewById(R.id.notes_view)

            // Set up  no notes view
            noNotesView = findViewById(R.id.no_notes_view)
            noNoteMainView = findViewById(R.id.no_notes_main) as TextView
            noNoteAddView = (findViewById(R.id.no_notes_add) as TextView).also {
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater)
            = inflater.inflate(R.menu.notes_fragment_menu, menu)

    override fun setLoadingIndicator(active: Boolean) {
        val root = view ?: return
        with(root.findViewById(R.id.refresh_layout) as SwipeRefreshLayout) {
            // Make sure setRefreshing() is called after the layout is done with everything else.
            post { isRefreshing = active }
        }
    }

    override fun showNotes(notes: MutableList<Note>) {
        notesAdapter.notes = notes
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

    override fun showDeleteNote(title: String) = showMessage(getString(R.string.note_item_delete_message, title))

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

    private fun deleteNoteFromAdapter(clickedNote: Note) {
        notesAdapter.removeItem(clickedNote)
    }

    private class NotesAdapter(val context: Context, notes: MutableList<Note>
                               , private val itemListener: NoteItemListener
                               , private val menuListener: NoteItemMenuListener)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var notes: MutableList<Note> = notes
            set(notes) {
                field = notes
                notifyDataSetChanged()
            }

        private val inflater = LayoutInflater.from(context)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder !is ViewHolder) return
            if (notes.size > position) {
                // itemの設定とリスナの登録
                holder.apply {
                    textView.text = notes[position].titleForList.trimStart()
                    itemView.setOnClickListener {
                        itemListener.onNoteItemClick(notes[position])
                    }
                    menu.setOnClickListener {
                        popup.showAsDropDown(menu, menu.width, -menu.height)    // ポップアップメニューの表示
                    }
                    popup.contentView.findViewById(R.id.note_item_menu_delete).setOnClickListener {
                        menuListener.onNoteItemMenuClick(notes[position], R.id.note_item_menu_delete)
                        popup.dismiss()
                    }
                }
            }
        }

        override fun getItemCount(): Int = notes.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder
            = ViewHolder(inflater.inflate(R.layout.note_item, parent, false), context)

        // ViewHolderに対するクリックリスナはonBindViewHolderで登録
        class ViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
            val textView = itemView.findViewById(R.id.note_item_title) as TextView
            val menu = itemView.findViewById(R.id.note_item_menu) as ImageButton
            val popup = PopupWindow(context).apply {
                contentView = LayoutInflater.from(context).inflate(R.layout.note_menu, null)
                isOutsideTouchable = true
                isFocusable = true
                isTouchable = true
            }
        }

        fun removeItem(clickedNote: Note) {
            val position = notes.indexOf(clickedNote)
            notes.removeAt(position)
            notifyItemRemoved(position)
        }

        // RecyclerViewのアイテムタップ時のリスナ
        interface NoteItemListener {
            fun onNoteItemClick(clickedNote: Note)
        }

        // RecyclerViewのアイテムのメニュータップ時のリスナ
        interface NoteItemMenuListener {
            fun onNoteItemMenuClick(clickedNote: Note, menuId: Int)
        }
    }

    companion object {
        fun newInstance() = NotesFragment()
    }
}