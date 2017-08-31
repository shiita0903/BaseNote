package jp.shiita.basenote.textshare

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import jp.shiita.basenote.notes.NotesActivity

class TextShareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        val action = intent.action
        val type = intent.type
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (action == Intent.ACTION_SEND && type == "text/plain") {
            ShareMenuDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ShareMenuDialogFragment.ARGUMENT_SHARE_TEXT, text)
                }
            }.show(supportFragmentManager, ShareMenuDialogFragment.TAG)
        }
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
