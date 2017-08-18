package com.example.shiita.notepad.addeditnote

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.shiita.notepad.R

class WebViewFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.webview_frag, container, false)
        val urlStr = when (arguments.getInt(ARGUMENT_SEARCH_ID)) {
            0 -> "http://www.google.co.jp/m/search?hl=ja&q="
            1 -> "https://ja.wikipedia.org/wiki/"
            2 -> "http://ejje.weblio.jp/content/"
            else -> error("SEARCH_IDが間違っています")
        } + arguments.getString(ARGUMENT_SEARCH_WORD)

        (root.findViewById(R.id.web_view) as WebView).apply {
            setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                }
            })
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            loadUrl(urlStr)
        }

        return root
    }

    companion object {
        val ARGUMENT_SEARCH_WORD = "SEARCH_WORD"
        val ARGUMENT_SEARCH_ID = "SEARCH_ID"    // 0:Google, 1:Wikipedia, 2:Weblio
    }
}