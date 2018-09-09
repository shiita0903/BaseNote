package jp.shiita.basenote.addeditnote

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import dagger.android.support.DaggerAppCompatActivity
import jp.shiita.basenote.R
import jp.shiita.basenote.data.NotesRepository
import jp.shiita.basenote.util.replaceFragment
import javax.inject.Inject

class AddEditNoteActivity : DaggerAppCompatActivity() {
    private lateinit var fabTop: FloatingActionButton
    private lateinit var fabBottom: FloatingActionButton
    private lateinit var addEditNotePresenter: AddEditNotePresenter
    @Inject lateinit var fragment: AddEditNoteFragment
    @Inject lateinit var notesRepository: NotesRepository

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

        var shouldLoadDataFromRepo = true

        if (savedInstanceState == null) {
            fragment.arguments = Bundle().apply {
                putString(AddEditNoteFragment.ARGUMENT_EDIT_NOTE_ID, noteId)
            }
            supportFragmentManager.replaceFragment(R.id.container, fragment)
        }
        else {
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY)
        }

        // Create the presenter
        addEditNotePresenter = AddEditNotePresenter(
                noteId,
                noteTag,
                fragment,
                shouldLoadDataFromRepo,
                notesRepository)
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

    fun switchTopFab() {
        startScaleAnim(visible = fabTop, gone = fabBottom)
    }

    fun switchBottomFab() {
        startScaleAnim(visible = fabBottom, gone = fabTop)
    }

    fun showTopFab() {
        getScaleAnim(fabTop, 500, toSmall = false).apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    fabTop.apply {
                        visibility = View.VISIBLE
                        scaleX = 0.1f
                        scaleY = 0.1f
                    }
                }
            })
            start()
        }
    }

    fun hideTopFab() {
        getScaleAnim(fabTop, 500, toSmall = true).run {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    fabTop.visibility = View.GONE
                }
            })
            start()
        }
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

    companion object {
        val SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY"
        val REQUEST_ADD_NOTE = 1
    }
}
