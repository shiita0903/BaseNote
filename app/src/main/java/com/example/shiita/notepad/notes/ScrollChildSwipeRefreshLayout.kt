package com.example.shiita.notepad.notes

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.view.View

// TODO: ライブラリに置き換える
class ScrollChildSwipeRefreshLayout @JvmOverloads constructor(context: Context,
                                                              attrs: AttributeSet? = null)
    : SwipeRefreshLayout(context, attrs) {

    var scrollUpChild: View? = null

    override fun canChildScrollUp(): Boolean {
        if (scrollUpChild != null) {
            return ViewCompat.canScrollVertically(scrollUpChild, -1)
        }
        return super.canChildScrollUp()
    }
}