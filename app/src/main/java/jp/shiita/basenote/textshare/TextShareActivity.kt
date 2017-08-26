package jp.shiita.basenote.textshare

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import jp.shiita.basenote.notes.NotesActivity

class TextShareActivity : Activity() {
    // 透明背景のstyle(タイトルバーを消している)のため、Activityを継承する必要がある
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent.action
        val type = intent.type
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (action == Intent.ACTION_SEND && type == "text/plain") {
            ShareMenuDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ShareMenuDialogFragment.ARGUMENT_SHARE_TEXT, text)
                }
            }.show(fragmentManager, ShareMenuDialogFragment.TAG)
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
}
