package com.example.shiita.notepad.util

import android.text.style.URLSpan
import android.view.View

/**
 * URLSpanクリック時の処理を変更したいため、継承してonClickをオーバライドした。
 */
class MyURLSpan(url: String) : URLSpan(url) {

    /**
     * urlを引数に取るリスナ。
     */
    var onUrlClickListener: ((String) -> Unit)? = null

    override fun onClick(widget: View?) = onUrlClickListener?.invoke(url) ?: Unit
}
