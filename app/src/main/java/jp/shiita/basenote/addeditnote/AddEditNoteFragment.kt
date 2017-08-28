package jp.shiita.basenote.addeditnote

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import jp.shiita.basenote.R
import jp.shiita.basenote.data.URLSpanData
import jp.shiita.basenote.util.MyURLSpan
import jp.shiita.basenote.util.snackbarLong

class AddEditNoteFragment : Fragment(), AddEditNoteContract.View, MyURLSpan.OnURLClickListener {

    override var presenter: AddEditNoteContract.Presenter? = null

    private lateinit var title: TextView

    private lateinit var content: TextView

    private lateinit var webView: WebView
    private var webViewX = 0

    private lateinit var closeWebViewButton: ImageButton

    private lateinit var webFrameLayout: FrameLayout

    private var editMode = true
    private var webMode = false

    override var isActive: Boolean = false
        get() = isAdded

    override fun onResume() {
        super.onResume()
        presenter?.start()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(activity.findViewById(R.id.fab_edit_note_done_top) as FloatingActionButton) {
            setOnClickListener {
                val spanList = presenter?.getURLSpanDataList(content.text as Spannable) ?: emptyList()
                presenter?.saveNote(title.text.toString(), content.text.toString(), spanList)
            }
        }
        with(activity.findViewById(R.id.fab_edit_note_done_bottom) as FloatingActionButton) {
            setOnClickListener {
                val spanList = presenter?.getURLSpanDataList(content.text as Spannable) ?: emptyList()
                presenter?.saveNote(title.text.toString(), content.text.toString(), spanList)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 画面起動時にソフトウェアキーボードを出さない
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
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
                }
                // TouchListenerのeventをそのままGestureDetectorに渡してスワイプの検知
                setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
                // WebViewがレイアウトに設置されてから、サイズを測る
                viewTreeObserver.addOnGlobalLayoutListener { webViewX = webView.width }
            }
            closeWebViewButton = (findViewById(R.id.close_web_view_button) as ImageButton).apply {
                setOnClickListener { stopWebMode() }
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
                    in id.keys -> {
                        startWebMode()
                        val urlStr = presenter?.generateSearchURL(word, id[item?.itemId]!!)
                        if (urlStr != null) {
                            // MyURLSpanの設定
                            val span = MyURLSpan(urlStr).apply {
                                setOnURLClickListener(this@AddEditNoteFragment)
                            }
                            presenter?.addMyURLSpanToContent(content.text as Spannable, span, start, end)
                            webView.loadUrl(urlStr)
                        }
                        return true
                    }

                }
                return false
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menu?.run {
                    add(Menu.NONE, R.id.search_google, Menu.FIRST, getString(R.string.menu_search_google))
                    add(Menu.NONE, R.id.search_wikipedia, Menu.FIRST, getString(R.string.menu_search_wikipedia))
                    add(Menu.NONE, R.id.search_weblio, Menu.FIRST, getString(R.string.menu_search_weblio))
                }
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = true

            override fun onDestroyActionMode(mode: ActionMode?) {
                content.clearFocus()
            }
        }

        setHasOptionsMenu(true)
        return root
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        // editModeによって表示するメニューを動的に変える
        menu?.findItem(R.id.menu_edit)?.isVisible = !editMode
        menu?.findItem(R.id.menu_edit_finish)?.isVisible = editMode
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_edit, R.id.menu_edit_finish -> switchEditMode()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) = inflater.inflate(R.menu.addeditnote_fragment_menu, menu)

    override fun showEmptyNoteError() = title.snackbarLong(getString(R.string.empty_note_message))

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
        // URLSpanの作成
        val sb = SpannableStringBuilder()
        sb.append(content)
        urlSpanList.forEach { urlSpan ->
            val span = MyURLSpan(urlSpan.url).apply {
                // TODO: ここから呼び出すとキーボードが閉じない
                setOnURLClickListener(this@AddEditNoteFragment)
            }
            sb.setSpan(span, urlSpan.start, urlSpan.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // タップ時の処理をONにする
        this.content.movementMethod = LinkMovementMethod.getInstance()
        this.content.text = sb
    }

    override fun switchEditMode() {
        editMode = !editMode

        // フォーカスが与えられるかどうかで、編集の可否を制御
        title.isFocusable = editMode
        title.isFocusableInTouchMode = editMode
        content.isFocusable = editMode
        content.isFocusableInTouchMode = editMode

        val act = (activity as AddEditNoteActivity)
        when {
            editMode && webMode -> act.showTopFab()
            editMode && !webMode -> act.showBottomFab()
            !editMode -> act.hideFab()
        }

        // メニューの書き換え
        activity.invalidateOptionsMenu()

        hideSoftInput()
    }

    override fun onURLClick(url: String) {
        if (editMode) return    // 編集中は無効化
        startWebMode()
        webView.loadUrl(url)
        webFrameLayout.visibility = View.VISIBLE
        closeWebViewButton.visibility = View.VISIBLE
    }

    // Webページを表示する処理。fabとアクションバーを非表示に
    private fun startWebMode() {
        hideSoftInput()
        webFrameLayout.visibility = View.VISIBLE
        closeWebViewButton.visibility = View.VISIBLE
        (activity as AddEditNoteActivity).apply {
            if (editMode)
                showTopFab()
            supportActionBar?.hide()
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
        }
        webMode = true
    }

    // WebViewを閉じる処理。fabとアクションバー再表示する
    private fun stopWebMode() {
        (activity as AddEditNoteActivity).apply {
            if (editMode)
                showBottomFab()
            supportActionBar?.show()
        }
        webFrameLayout.visibility = View.GONE
        closeWebViewButton.visibility = View.GONE
        webMode = false
    }

    //ソフトキーボードを閉じる
    private fun hideSoftInput() {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(content.windowToken, 0)
    }

    companion object {
        val ARGUMENT_EDIT_NOTE_ID = "EDIT_NOTE_ID"
        fun newInstance() = AddEditNoteFragment()
    }
}