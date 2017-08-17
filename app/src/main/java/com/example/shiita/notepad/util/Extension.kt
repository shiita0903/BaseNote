package com.example.shiita.notepad.util

import android.app.Activity
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.View

fun Activity.addFragmentToActivity(fragmentManager: FragmentManager, fragment: Fragment, frameId: Int) {
    fragmentManager.beginTransaction().run {
        add(frameId, fragment)
        commit()
    }
}

fun View.snackbarLong(message: String) = Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
