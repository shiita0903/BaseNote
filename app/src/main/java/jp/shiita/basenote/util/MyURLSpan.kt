package jp.shiita.basenote.util

import android.text.style.URLSpan
import android.view.View

/**
 * URLSpanクリック時の処理を変更したいため、継承してonClickをオーバライドした。
 */
class MyURLSpan(url: String) : URLSpan(url) {

    /**
     * urlを引数に取るリスナ。
     */
    interface OnURLClickListener {
        fun onURLClick(url: String)
    }

    private var listener: OnURLClickListener? = null

    fun setOnURLClickListener(listener: OnURLClickListener) {
        this.listener = listener
    }

    override fun onClick(widget: View?) = listener?.onURLClick(url) ?: Unit
}
