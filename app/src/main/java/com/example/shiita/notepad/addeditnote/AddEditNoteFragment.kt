package com.example.shiita.notepad.addeditnote

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.example.shiita.notepad.R
import com.example.shiita.notepad.data.URLSpanData
import com.example.shiita.notepad.util.MyURLSpan


class AddEditNoteFragment : Fragment(), AddEditNoteContract.View {

    override var presenter: AddEditNoteContract.Presenter? = null

    private lateinit var title: TextView

    private lateinit var content: TextView

    private lateinit var webView: WebView
    private var webViewX = 0

    private lateinit var webFrameLayout: FrameLayout

    override var isActive: Boolean = false
        get() = isAdded

    override fun onResume() {
        super.onResume()
        presenter?.start()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(activity.findViewById(R.id.fab_edit_note_done) as FloatingActionButton) {
            setImageResource(R.drawable.ic_done)
            setOnClickListener {
                presenter?.saveNote(title.text.toString(), content.text.toString(), getUrlSpanList())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // スワイプ検知に利用
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val distance = event1.x - event2.x
                val acceptSideSize = 30
                val swipeMinDistance = 50
                val swipeMinVelocity = 300

                // スワイプの移動距離・速度・開始地点の条件がそろえば、WebViewの操作をする
                if (distance > swipeMinDistance && Math.abs(velocityX) > swipeMinVelocity && event1.x > webViewX - acceptSideSize) {
                    webView.goForward()
                } else if (-distance > swipeMinDistance && Math.abs(velocityX) > swipeMinVelocity && event1.x < acceptSideSize) {
                    webView.goBack()
                }

                return false
            }
        })
        val root = inflater.inflate(R.layout.addeditnote_frag, container, false)
        // Viewの初期設定
        with(root) {
            title = findViewById(R.id.add_edit_note_title) as TextView
            content = findViewById(R.id.add_edit_note_content) as TextView
            webFrameLayout = findViewById(R.id.web_frame_layout) as FrameLayout
            webView = (findViewById(R.id.web_view) as WebView).apply {
                scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
                setWebViewClient(object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                    }
                })
                settings.apply {
                    javaScriptEnabled = true
                    builtInZoomControls = true
                }
                // TouchListenerのeventをそのままGestureDetectorに渡してスワイプの検知
                setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
                // WebViewがレイアウトに設置されてから、サイズを測る
                viewTreeObserver.addOnGlobalLayoutListener { webViewX = webView.width }
            }
            (findViewById(R.id.close_web_view_button) as ImageButton).setOnClickListener { stopWebMode() }

            val a = if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                Html.fromHtml("<a href=\"" + "http://petitviolet.hatenablog.com/entry/20131106/1383732344" + "\">リンクテキスト</a>", Html.FROM_HTML_MODE_LEGACY)
            else
                Html.fromHtml("<a href=\"" + "http://petitviolet.hatenablog.com/entry/20131106/1383732344" + "\">リンクテキスト</a>")
            content.append(a)
            val b = (content.text as Spannable)
            content.append(" ")
            content.append(b)
            val c = a
            content.append(" ")
            content.append(c)
            val spannable = content.text as Spannable
            val span = spannable.getSpans(0, content.length(), URLSpan::class.java)[0]
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            Log.d("Spannable", span.url + ":" + start + ":" + end)
            spannable.removeSpan(span)
            val myURLSpan = MyURLSpan(span.url).apply {
                onUrlClickListener = { url ->
                    webView.loadUrl(url)
                    webFrameLayout.visibility = View.VISIBLE
                    Log.d("onClick", "Click!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                }
            }
            spannable.setSpan(myURLSpan, start, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            Log.d("After Spannable", span.url + ":" + spannable.getSpanStart(myURLSpan) + ":" + spannable.getSpanEnd(myURLSpan))
        }

        content.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                var start = 0
                var end = content.text.length

                if (content.isFocused) {
                    start = content.selectionStart
                    end = content.selectionEnd
                }

                val word = content.text.subSequence(start, end).toString()
                val id = mapOf(R.id.search_google to 0, R.id.search_wikipedia to 1, R.id.search_weblio to 2)
                when (item?.itemId) {
                    R.id.search_google,
                    R.id.search_wikipedia,
                    R.id.search_weblio -> {
                        // web検索時にはfabを消してから表示する
                        val urlStr = presenter?.generateSearchUrl(word, id[item.itemId]!!)
                        webView.loadUrl(urlStr)
                        startWebMode()
                        return true
                    }

                }
                return false
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menu?.add(Menu.NONE, R.id.search_google, Menu.FIRST, getString(R.string.menu_search_google))
                menu?.add(Menu.NONE, R.id.search_wikipedia, Menu.FIRST, getString(R.string.menu_search_wikipedia))
                menu?.add(Menu.NONE, R.id.search_weblio, Menu.FIRST, getString(R.string.menu_search_weblio))
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                content.clearFocus()
            }
        }
        setHasOptionsMenu(true)
        return root
    }

    override fun showEmptyNoteError() {
        Snackbar.make(title, getString(R.string.empty_note_message), Snackbar.LENGTH_LONG).show()
    }

    override fun showNotesList() {
        with(activity) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun setTitle(title: String) {
        this.title.text = title
    }

    override fun setContent(content: String, urlSpanList: List<URLSpanData>) {
        // URLSpanの設定
        val sb = SpannableStringBuilder()
        sb.append(content)
        urlSpanList.forEach { urlSpan ->
            val span = MyURLSpan(urlSpan.url).apply {
                onUrlClickListener = { url ->
                    startWebMode()      // TODO: ここから呼び出すとキーボードが閉じない
                    webView.loadUrl(url)
                    webFrameLayout.visibility = View.VISIBLE
                }
            }
            sb.setSpan(span, urlSpan.start, urlSpan.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // タップ時の処理をONにする
        this.content.movementMethod = LinkMovementMethod.getInstance()
        this.content.text = sb
    }

    // Webページを表示する処理。fabとアクションバーを非表示に
    private fun startWebMode() {
        webFrameLayout.visibility = View.VISIBLE
        (activity as AddEditNoteActivity).apply {
            fab.visibility = View.GONE
            supportActionBar?.hide()
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
        }
        //ソフトキーボードを閉じる
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(content.windowToken, 0)
    }

    // WebViewを閉じる処理。fabとアクションバー再表示する
    private fun stopWebMode() {
        (activity as AddEditNoteActivity).apply {
            fab.visibility = View.VISIBLE
            supportActionBar?.show()
        }
        webFrameLayout.visibility = View.GONE
    }

    private fun getUrlSpanList(): List<URLSpanData> {
        val spannable = (content.text as Spannable)
        return spannable
                .getSpans(0, content.length(), MyURLSpan::class.java)
                ?.map { span -> URLSpanData(span.url, spannable.getSpanStart(span), spannable.getSpanEnd(span)) }
                ?: emptyList()
    }

    companion object {
        val ARGUMENT_EDIT_NOTE_ID = "EDIT_NOTE_ID"
        fun newInstance() = AddEditNoteFragment()
    }
}