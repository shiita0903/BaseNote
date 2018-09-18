package jp.shiita.basenote.util

import android.text.style.URLSpan
import android.view.View

/**
 * URLSpanクリック時の処理を変更したいため、継承してonClickをオーバライドした。
 */
class ClickableURLSpan(url: String, private val onClick: (url: String) -> Unit) : URLSpan(url) {
    override fun onClick(widget: View?) = onClick(url)
}
