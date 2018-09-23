package jp.shiita.basenote.addeditnote

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import jp.shiita.basenote.R
import javax.inject.Inject

class SelectTagDialogFragment @Inject constructor() : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val tags = resources.getStringArray(R.array.tag_color_item)
        var selected = arguments!!.getInt(ARGUMENT_TAG)
        return AlertDialog.Builder(activity!!)
                .setTitle(getString(R.string.select_tag))
                .setSingleChoiceItems(tags, selected) { _, which ->
                    selected = which
                }
                .setPositiveButton("OK") { _, _ ->
                    targetFragment?.let { fragment ->
                        val data = Intent().apply {
                            putExtra(ARGUMENT_TAG, selected)
                            if (arguments!!.containsKey(ARGUMENT_POSITION))
                                putExtra(ARGUMENT_POSITION, arguments!!.getInt(ARGUMENT_POSITION))
                        }
                        fragment.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
    }

    override fun show(manager: FragmentManager?, tag: String?) {
        if (!checkShowingDialog(manager, tag)) {
            super.show(manager, tag)
        }
    }

    private fun checkShowingDialog(manager: FragmentManager?, tag: String?): Boolean {
        val fragment = manager?.findFragmentByTag(tag ?: "") ?: return false
        if (fragment is SelectTagDialogFragment) return fragment.dialog != null
        return false
    }

    companion object {
        const val ARGUMENT_TAG = "argumentTag"
        const val ARGUMENT_POSITION = "argumentPosition"
        val TAG = SelectTagDialogFragment::class.java.simpleName
    }
}