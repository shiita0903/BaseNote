package com.example.shiita.notepad.textshare

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import com.example.shiita.notepad.R
import com.example.shiita.notepad.data.NotesDataSource


class SelectNoteDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val text = arguments.getString(ARGUMENT_APPEND_TEXT)
        var startApp = arguments.getBoolean(ARGUMENT_START_APP)
        val items = NotesDataSource.getNotes()
        val checkedItems = mutableListOf<Int>()
        return AlertDialog.Builder(activity)
                .setTitle(getString(R.string.select_note))
                .setMultiChoiceItems(items.map { it.titleForList }.toTypedArray(), null) { _, which, isChecked ->
                    if (isChecked)
                        checkedItems.add(which)
                    else
                        checkedItems.remove(which)
                }
                .setPositiveButton("OK") { _, _ ->
                    if (checkedItems.isEmpty())     // 何も追加しない場合にはアプリは開かない
                        startApp = false
                    checkedItems.forEach { updateNote(items[it].id, text) }
                    dialog.cancel()
                    (activity as TextShareActivity).finishTextShareActivity(startApp)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    startApp = false
                    dialog.cancel()
                    (activity as TextShareActivity).finishTextShareActivity(startApp)
                }
                .create()
    }

    private fun updateNote(id: String, text: String) {
        val note = NotesDataSource.getNote(id)
        if (note != null)
            NotesDataSource.updateNote(note.apply { content += (System.getProperty("line.separator") + text) })
    }

    companion object {
        val ARGUMENT_APPEND_TEXT = "APPEND_TEXT"
        val ARGUMENT_START_APP = "START_APP"
        val TAG = SelectNoteDialogFragment::class.java.simpleName
    }
}