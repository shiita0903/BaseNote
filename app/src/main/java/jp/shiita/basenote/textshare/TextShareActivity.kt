package jp.shiita.basenote.textshare

import android.content.Intent
import android.os.Bundle
import android.view.View
import dagger.android.support.DaggerAppCompatActivity
import jp.shiita.basenote.notes.NotesActivity
import javax.inject.Inject

class TextShareActivity : DaggerAppCompatActivity() {
    @Inject lateinit var shareMenuFragment: ShareMenuDialogFragment
    @Inject lateinit var selectNoteFragment: SelectNoteDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        val action = intent.action
        val type = intent.type
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (action == Intent.ACTION_SEND && type == "text/plain")
            showShareMenuDialogFragment(text)
    }

    fun showShareMenuDialogFragment(text: String) {
        shareMenuFragment.arguments = Bundle().apply {
            putString(ShareMenuDialogFragment.ARGUMENT_SHARE_TEXT, text)
        }
        shareMenuFragment.show(fragmentManager, ShareMenuDialogFragment.TAG)
    }

    fun showSelectNoteDialogFragment(text: String, startApp: Boolean) {
        selectNoteFragment.arguments = Bundle().apply {
            putString(SelectNoteDialogFragment.ARGUMENT_APPEND_TEXT, text)
            putBoolean(SelectNoteDialogFragment.ARGUMENT_START_APP, startApp)
        }
        selectNoteFragment.show(fragmentManager, SelectNoteDialogFragment.TAG)
    }

    /**
     * Fragmentから直接呼び出せるように。テキスト共有のみでしか用いないので簡略化
     */
    fun finishTextShareActivity(startApp: Boolean) {
        if (startApp)
            startActivity(Intent(this, NotesActivity::class.java))
        finish()
    }

    fun transparent() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
    }
}
