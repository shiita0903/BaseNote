package jp.shiita.basenote.addeditnote

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import jp.shiita.basenote.R
import jp.shiita.basenote.util.addFragmentToActivity

class AddEditNoteActivity : AppCompatActivity() {
    private lateinit var addEditNotePresenter: AddEditNotePresenter
    lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addeditnote_act)

        // Set up the toolbar.
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        fab = findViewById(R.id.fab_edit_note_done) as FloatingActionButton

        val noteId = intent.getStringExtra(AddEditNoteFragment.ARGUMENT_EDIT_NOTE_ID)
        setToolbarTitle(noteId)
        val addEditNoteFragment = getFragment(noteId)

        var shouldLoadDataFromRepo = true

        // Prevent the presenter from loading data from the repository if this is a config change.
        if (savedInstanceState != null) {
            // Data might not have loaded when the config change happen, so we saved the state.
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY)
        }

        // Create the presenter
        addEditNotePresenter = AddEditNotePresenter(
                noteId,
                addEditNoteFragment,
                shouldLoadDataFromRepo)
    }

    private fun setToolbarTitle(noteId: String?) {
        supportActionBar?.let {
            if (noteId == null) {
                it.setTitle(R.string.add_note)
            } else {
                it.setTitle(R.string.edit_note)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save the state so that next time we know if we need to refresh data.
        outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, addEditNotePresenter.isDataMissing)
        super.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getFragment(noteId: String?): AddEditNoteFragment  {
        return supportFragmentManager.findFragmentById(R.id.contentFrame)
                as AddEditNoteFragment? ?:
                AddEditNoteFragment.newInstance().also {
                    if (intent.hasExtra(AddEditNoteFragment.ARGUMENT_EDIT_NOTE_ID)) {
                        it.arguments = Bundle().apply {
                            putString(AddEditNoteFragment.ARGUMENT_EDIT_NOTE_ID, noteId)
                        }
                    }
                    addFragmentToActivity(supportFragmentManager, it, R.id.contentFrame)
                }
    }

    companion object {
        val SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY"
        val REQUEST_ADD_NOTE = 1
    }
}
