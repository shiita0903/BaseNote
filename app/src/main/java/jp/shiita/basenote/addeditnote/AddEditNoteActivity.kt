package jp.shiita.basenote.addeditnote

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import jp.shiita.basenote.R
import jp.shiita.basenote.util.addFragmentToActivity

class AddEditNoteActivity : AppCompatActivity() {
    private lateinit var addEditNotePresenter: AddEditNotePresenter
    private lateinit var fabTop: FloatingActionButton
    private lateinit var fabBottom: FloatingActionButton

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
        fabTop = findViewById(R.id.fab_edit_note_done_top) as FloatingActionButton
        fabBottom = findViewById(R.id.fab_edit_note_done_bottom) as FloatingActionButton

        val noteId = intent.getStringExtra(AddEditNoteFragment.ARGUMENT_EDIT_NOTE_ID)
        val noteTag = intent.getIntExtra(AddEditNoteFragment.ARGUMENT_EDIT_NOTE_TAG, 0)
        setToolbarTitle(editMode = true)
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
                noteTag,
                addEditNoteFragment,
                shouldLoadDataFromRepo)
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

    fun setToolbarTitle(editMode: Boolean) {
        supportActionBar?.let {
            if (editMode)
                it.setTitle(R.string.edit_note)
            else
                it.setTitle(R.string.read_note)
        }
    }

    fun showTopFab() {
        startScaleAnim(visible = fabTop, gone = fabBottom)
    }

    fun showBottomFab() {
        startScaleAnim(visible = fabBottom, gone = fabTop)
    }

    fun setFabIconResource(id: Int) {
        fabTop.setImageResource(id)
        fabBottom.setImageResource(id)
    }

    private fun startScaleAnim(visible: View, gone: View) {
        if (visible.visibility == View.VISIBLE) return

        // fabを消すアニメーション
        val smallAnim = getScaleAnim(gone, 500, toSmall = true).apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    gone.visibility = View.GONE
                }
            })
        }
        // fabを出現させるアニメーション
        val largeAnim = getScaleAnim(visible, 500, toSmall = false).apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    visible.apply {
                        visibility = View.VISIBLE
                        scaleX = 0.1f
                        scaleY = 0.1f
                    }
                }
            })
        }

        AnimatorSet().apply {
            playSequentially(smallAnim, largeAnim)  // 前のアニメーションが終了してから順に実行
            start()
        }
    }

    private fun getScaleAnim(view: View, duration: Long, toSmall: Boolean): AnimatorSet {
        val start = if (toSmall) 1f else 0.1f
        val end   = if (toSmall) 0.1f else 1f
        val anim1 = ObjectAnimator.ofFloat(view, "scaleX", start, end)
        val anim2 = ObjectAnimator.ofFloat(view, "scaleY", start, end)
        return AnimatorSet().apply {
            this.duration = duration
            interpolator = if (toSmall) AccelerateInterpolator() else DecelerateInterpolator()
            playTogether(anim1, anim2)
        }
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
