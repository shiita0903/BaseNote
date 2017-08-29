package jp.shiita.basenote.addeditnote

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import jp.shiita.basenote.R

class SelectTagDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val tags = resources.getStringArray(R.array.tag_color_item)
        var selected = arguments.getInt(ARGUMENT_TAG)
        return AlertDialog.Builder(activity)
                .setTitle(getString(R.string.select_tag))
                .setSingleChoiceItems(tags, selected) { _, which ->
                    selected = which
                }
                .setPositiveButton("OK") { _, _ ->
                    if (targetFragment != null) {
                        val data = Intent().apply {
                            putExtra(ARGUMENT_TAG, selected)
                            if (arguments.containsKey(ARGUMENT_POSITION))
                                putExtra(ARGUMENT_POSITION, arguments.getInt(ARGUMENT_POSITION))
                        }
                        targetFragment.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
    }

    companion object {
        val ARGUMENT_TAG = "TAG"
        val ARGUMENT_POSITION = "POSITION"
        val TAG = SelectTagDialogFragment::class.java.simpleName
    }
}