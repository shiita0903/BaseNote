package com.example.shiita.notepad.addeditnote

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import com.example.shiita.notepad.R
import android.view.MotionEvent
import android.text.method.Touch.onTouchEvent
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout


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
                presenter?.saveNote(title.text.toString(), content.text.toString())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
            webView = (findViewById(R.id.web_view) as WebView).apply {
                scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
                setWebViewClient(object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                    }
                })
                settings.javaScriptEnabled = true
                // TouchListenerのeventをそのままGestureDetectorに渡してスワイプの検知
                setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
                // WebViewがレイアウトに設置されてから、サイズを測る
                viewTreeObserver.addOnGlobalLayoutListener { webViewX = webView.width }
            }
            webFrameLayout = findViewById(R.id.web_frame_layout) as FrameLayout
            (findViewById(R.id.close_web_view_button) as Button).setOnClickListener {
                // WebViewを閉じる処理。fabを再表示する
                (activity as AddEditNoteActivity).fab.visibility = View.VISIBLE
                webFrameLayout.visibility = View.GONE
            }
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
                        webFrameLayout.visibility = View.VISIBLE
                        (activity as AddEditNoteActivity).fab.visibility = View.GONE
                        if (content.isFocused) {
                            //ソフトキーボードを閉じる
                            val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputMethodManager.hideSoftInputFromWindow(content.windowToken, 0)
                        }
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

    override fun setContent(content: String) {
        this.content.text = content
    }

    companion object {
        val ARGUMENT_EDIT_NOTE_ID = "EDIT_NOTE_ID"
        fun newInstance() = AddEditNoteFragment()
    }
}