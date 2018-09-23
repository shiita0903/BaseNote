package jp.shiita.basenote.addeditnote

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import dagger.android.support.DaggerAppCompatActivity
import jp.shiita.basenote.R
import jp.shiita.basenote.data.NotesRepository
import jp.shiita.basenote.databinding.ActAddEditNoteBinding
import jp.shiita.basenote.util.observe
import jp.shiita.basenote.util.replaceFragment
import javax.inject.Inject

class AddEditNoteActivity : DaggerAppCompatActivity() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var addEditNoteFragment: AddEditNoteFragment
    @Inject lateinit var selectTagDialogFragment: SelectTagDialogFragment
    @Inject lateinit var notesRepository: NotesRepository
    private val viewModel: AddEditNoteViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(AddEditNoteViewModel::class.java) }
    private val binding: ActAddEditNoteBinding
            by lazy { DataBindingUtil.setContentView<ActAddEditNoteBinding>(this, R.layout.act_add_edit_note) }
    private val noteId: String? by lazy { intent.getStringExtra(AddEditNoteFragment.ARGUMENT_ADD_EDIT_NOTE_ID) }
    private val noteTag: Int by lazy { intent.getIntExtra(AddEditNoteFragment.ARGUMENT_ADD_EDIT_NOTE_TAG, 0)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        setupActionBar()
        observe()

        if (savedInstanceState == null) {
            addEditNoteFragment.arguments = Bundle().apply {
                putString(AddEditNoteFragment.ARGUMENT_ADD_EDIT_NOTE_ID, noteId)
                putInt(AddEditNoteFragment.ARGUMENT_ADD_EDIT_NOTE_TAG, noteTag)
            }
            supportFragmentManager.replaceFragment(R.id.container, addEditNoteFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    fun showSelectTagDialogFragment(targetFragment: Fragment, requestCode: Int) {
        selectTagDialogFragment.apply {
            setTargetFragment(targetFragment, requestCode)
            arguments = Bundle().apply {
                putInt(SelectTagDialogFragment.ARGUMENT_TAG, viewModel.tag.value ?: 0)
            }
        }.show(supportFragmentManager, SelectTagDialogFragment.TAG)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.addEditNoteToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.read_note)
        }
    }

    private fun observe() {
        viewModel.webMode.observe(this) {
            if (it) switchTopFab()
            else switchBottomFab()
        }
    }

    private fun switchTopFab() {
        startScaleAnim(visible = binding.addEditNoteFabTop, gone = binding.addEditNoteFabBottom)
    }

    private fun switchBottomFab() {
        startScaleAnim(visible = binding.addEditNoteFabBottom, gone = binding.addEditNoteFabTop)
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
}
