package jp.shiita.basenote.addeditnote

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
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

    private lateinit var webViewBar: View

    private lateinit var webFrameLayout: FrameLayout

    private var editMode = true
    private var webMode = false

    private var noteTag = 0

    override var isActive: Boolean = false
        get() = isAdded

    override fun onResume() {
        super.onResume()
        presenter?.start()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(activity.findViewById(R.id.fab_edit_note_done_top) as FloatingActionButton) {
            setOnClickListener { switchEditMode(save = true) }
        }
        with(activity.findViewById(R.id.fab_edit_note_done_bottom) as FloatingActionButton) {
            setOnClickListener { switchEditMode(save = true) }
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
            webViewBar = findViewById(R.id.add_edit_note_web_view_bar)
            findViewById(R.id.back_web_view_button).setOnClickListener { webView.goBack() }
            findViewById(R.id.forward_web_view_button).setOnClickListener { webView.goForward() }
            findViewById(R.id.close_web_view_button).setOnClickListener { stopWebMode() }
            findViewById(R.id.close_web_view_button).setOnClickListener { stopWebMode() }
        }

        // タップ時の処理をONにする
        content.movementMethod = LinkMovementMethod.getInstance()
        content.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                var start = 0
                var end = content.text.length

                if (content.isFocused) {
                    start = Math.max(content.selectionStart, start) // 最小値は0
                    end = Math.min(content.selectionEnd, end)       // 最大値はcontent.text.length
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
                            content.text = presenter?.addMyURLSpanToContent(content.text as Spannable, span, start, end)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_select_tag -> {
                SelectTagDialogFragment().apply {
                    setTargetFragment(this@AddEditNoteFragment, SELECT_TAG_REQUEST_CODE)
                    arguments = Bundle().apply {
                        putInt(SelectTagDialogFragment.ARGUMENT_TAG, noteTag)
                    }
                }.show(fragmentManager, SelectTagDialogFragment.TAG)
            }
            R.id.menu_delete -> presenter?.deleteNote()
            R.id.home -> finishActivity()
            else -> return false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.addeditnote_fragment_menu, menu)
        // タグの色変更
        if (noteTag != 0)
            menu?.getItem(0)?.icon?.setColorFilter(resources.obtainTypedArray(R.array.tag_color).getColor(noteTag, 0), PorterDuff.Mode.SRC_IN)
        else
            menu?.getItem(0)?.icon?.colorFilter = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            SELECT_TAG_REQUEST_CODE -> {
                noteTag = data?.getIntExtra(SelectTagDialogFragment.ARGUMENT_TAG, 0)!!
                presenter?.updateTag(noteTag)
                activity.invalidateOptionsMenu()    // メニューを読み込み直して、タグの色を反映
            }
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
                setOnURLClickListener(this@AddEditNoteFragment)
            }
            sb.setSpan(span, urlSpan.start, urlSpan.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        this.content.text = sb
    }

    override fun setNoteTag(tag: Int) {
        noteTag = tag
    }

    override fun showSaveNote() = content.snackbarLong(getString(R.string.save_note_message))

    override fun switchEditMode(save: Boolean) {
        editMode = !editMode

        // フォーカスが与えられるかどうかで、編集の可否を制御
        title.isFocusable = editMode
        title.isFocusableInTouchMode = editMode
        content.isFocusable = editMode
        content.isFocusableInTouchMode = editMode

        val act = (activity as AddEditNoteActivity)
        when {
            editMode -> act.setFabIconResource(R.drawable.ic_done)
            !editMode -> {
                act.setFabIconResource(R.drawable.ic_edit)
                // 編集モードから抜けた時に保存処理をする
                if (save) saveNote()
            }
        }
        act.setToolbarTitle(editMode)

        hideSoftInput()
    }

    override fun finishActivity() = activity.finish()

    override fun onURLClick(url: String) {
        if (editMode) return    // 編集中は無効化
        startWebMode()
        webView.loadUrl(url)
        webFrameLayout.visibility = View.VISIBLE
        webViewBar.visibility = View.VISIBLE
    }

    private fun saveNote() {
        val spanList = presenter?.getURLSpanDataList(content.text as Spannable) ?: emptyList()
        presenter?.saveNote(title.text.toString(), content.text.toString(), spanList, noteTag)
    }

    // Webページを表示する処理。fabとアクションバーを非表示に
    private fun startWebMode() {
        hideSoftInput()
        webFrameLayout.visibility = View.VISIBLE
        webViewBar.visibility = View.VISIBLE
        (activity as AddEditNoteActivity).apply {
            showTopFab()
            supportActionBar?.hide()
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
        }
        webMode = true
    }

    // WebViewを閉じる処理。fabとアクションバー再表示する
    private fun stopWebMode() {
        (activity as AddEditNoteActivity).apply {
            showBottomFab()
            supportActionBar?.show()
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        webFrameLayout.visibility = View.GONE
        webViewBar.visibility = View.GONE
        webMode = false
    }

    //ソフトキーボードを閉じる
    private fun hideSoftInput() {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(content.windowToken, 0)
    }

    companion object {
        val ARGUMENT_EDIT_NOTE_ID = "EDIT_NOTE_ID"
        val ARGUMENT_EDIT_NOTE_TAG = "EDIT_NOTE_TAG"
        val SELECT_TAG_REQUEST_CODE = 1
        fun newInstance() = AddEditNoteFragment()
    }
}