package jp.shiita.basenote.textshare

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import dagger.android.DaggerDialogFragment
import jp.shiita.basenote.R
import jp.shiita.basenote.data.Note
import jp.shiita.basenote.data.NotesRepository
import javax.inject.Inject

class ShareMenuDialogFragment @Inject constructor() : DaggerDialogFragment() {
    @Inject lateinit var notesRepository: NotesRepository

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = resources.getStringArray(R.array.text_share_item)
        val text = arguments!!.getString(ARGUMENT_SHARE_TEXT)
        var finish = true
        var startApp = false
        return AlertDialog.Builder(activity!!)
                .setTitle("「$text」")
                .setItems(items) { _, which ->
                    when (which) {
                        0 -> saveNote(text)     // 新規作成
                        1 -> {                  // 既存のノートに追加
                            finish = false
                            // TODO: finishを待ってactivityから操作するようにする
                            (activity as TextShareActivity).showSelectNoteDialogFragment(text, startApp)
                        }
                        2 -> {                  // 作成後にアプリを開く
                            startApp = true
                            saveNote(text)
                        }
                        3 -> {                  // 追加後にアプリを開く
                            startApp = true
                            finish = false
                            (activity as TextShareActivity).showSelectNoteDialogFragment(text, startApp)
                        }
                        4 -> {}                 // キャンセル
                    }
                    if (finish)
                        (activity as TextShareActivity).finishTextShareActivity(startApp)
                }
                .create()
    }

    override fun onCancel(dialog: DialogInterface?) {
        (activity as TextShareActivity).finishTextShareActivity(startApp = false)
        super.onCancel(dialog)
    }

    private fun saveNote(text: String) = notesRepository.saveNote(Note("", text))

    companion object {
        val ARGUMENT_SHARE_TEXT = "SHARE_TEXT"
        val TAG = ShareMenuDialogFragment::class.java.simpleName
    }
}