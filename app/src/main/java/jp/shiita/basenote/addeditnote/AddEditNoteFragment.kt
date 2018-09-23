package jp.shiita.basenote.addeditnote

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import dagger.android.support.DaggerFragment
import jp.shiita.basenote.R
import jp.shiita.basenote.databinding.FragAddEditNoteBinding
import jp.shiita.basenote.util.observe
import jp.shiita.basenote.util.setTintCompat
import jp.shiita.basenote.util.snackbar
import javax.inject.Inject


class AddEditNoteFragment @Inject constructor() : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AddEditNoteViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(AddEditNoteViewModel::class.java) }
    private lateinit var binding: FragAddEditNoteBinding
    private val noteId: String? by lazy { arguments?.getString(ARGUMENT_ADD_EDIT_NOTE_ID) }
    private val noteTag: Int by lazy { arguments?.getInt(ARGUMENT_ADD_EDIT_NOTE_TAG) ?: 0 }
    private val statusBarHeight: Int by lazy { Rect().also { activity?.window?.decorView?.getWindowVisibleDisplayFrame(it) }.top }
    private var fragmentHeight = 0
    private var webViewWidth = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_add_edit_note, container, false)
        binding.root.run { viewTreeObserver.addOnGlobalLayoutListener { fragmentHeight = height } }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        observe()
        viewModel.start(noteId, noteTag)

        // スワイプ検知
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val distance = event1.x - event2.x
                val acceptSideSize = 30
                val swipeMinDistance = 50
                val swipeMinVelocity = 300

                // スワイプの移動距離・速度・開始地点の条件がそろえば、WebViewの操作をする
                if (distance > swipeMinDistance && Math.abs(velocityX) > swipeMinVelocity && event1.x > webViewWidth - acceptSideSize) {
                    viewModel.goForward()
                } else if (-distance > swipeMinDistance && Math.abs(velocityX) > swipeMinVelocity && event1.x < acceptSideSize) {
                    viewModel.goBack()
                }

                return false
            }
        })

        // WebView初期化
        binding.addEditNoteWebView.run {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    viewModel.setCanGoForward(canGoForward())
                    viewModel.setCanGoBack(canGoBack())
                    viewModel.updateUrl(url)
                }
            }
            setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
            viewTreeObserver.addOnGlobalLayoutListener { webViewWidth = width }
        }

        // リンククリック処理
        binding.addEditNoteContent.run {
            movementMethod = LinkMovementMethod.getInstance()
            customSelectionActionModeCallback = object : ActionMode.Callback {

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    item ?: return false
                    val start = maxOf(selectionStart, 0)
                    val end = minOf(selectionEnd, length())
                    return showSearchMenu(mode, item.itemId, start, end)
                }

                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    // floating action modeに対応しているかどうかでメニュー表示を分ける
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mode?.menuInflater?.inflate(R.menu.search, menu)
                    }
                    else {
                        // カット、全て選択の削除
                        menu?.removeItem(android.R.id.cut)
                        menu?.removeItem(android.R.id.selectAll)
                        mode?.menuInflater?.inflate(R.menu.action_mode, menu)
                    }
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = true

                override fun onDestroyActionMode(mode: ActionMode?) {}
            }
        }

        // 大きさ変更処理
        binding.addEditNoteWebViewBar.apply {
            setOnTouchListener { _, event ->
                val y = maxOf(0f, minOf(fragmentHeight.toFloat(), event.rawY - statusBarHeight))
                viewModel.setGuidelinePercent(y / fragmentHeight)
                true
            }
        }
    }

    private fun observe() {
        // メニューを読み込み直して、タグの色を反映
        viewModel.tag.observe(this) { activity?.invalidateOptionsMenu() }
        viewModel.noteEmptyEvent.observe(this) { binding.root.snackbar(R.string.empty_note_message) }
        viewModel.noteSavedEvent.observe(this) { binding.root.snackbar(getString(R.string.save_note_message, it)) }
        viewModel.noteUpdatedEvent.observe(this) { binding.root.snackbar(getString(R.string.update_note_message, it)) }
        viewModel.noteDeleteEvent.observe(this) { activity?.finish() }  // TODO: 戻ったactivityでメッセージ
        viewModel.goBackEvent.observe(this)    { binding.addEditNoteWebView.goBack() }
        viewModel.goForwardEvent.observe(this) { binding.addEditNoteWebView.goForward() }
        viewModel.popupEvent.observe(this) {
            PopupMenu(context, binding.addEditNoteWebViewMenu).apply {
                inflate(R.menu.web_view)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_web_view_update_link -> viewModel.updateCurrentSpan()
                        R.id.menu_web_view_remove_link -> viewModel.removeCurrentSpan()
                    }
                    true
                }
                show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_frag_add_edit_note_select_tag ->
                (activity as AddEditNoteActivity).showSelectTagDialogFragment(this, REQUEST_CODE_SELECT_TAG)
            R.id.menu_frag_add_edit_note_delete -> viewModel.deleteNote()
            R.id.menu_frag_add_edit_note_finish -> {
                viewModel.saveNote()
                activity?.finish()
            }
            else -> return false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        menu ?: return
        inflater.inflate(R.menu.frag_add_edit_note, menu)
        val tag = viewModel.tag.value ?: 0
        val color = resources.obtainTypedArray(R.array.tag_color).getColor(tag, 0)
        menu.findItem(R.id.menu_frag_add_edit_note_select_tag).run { icon = icon.setTintCompat(color) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_CODE_SELECT_TAG -> {
                val tag = data?.getIntExtra(SelectTagDialogFragment.ARGUMENT_TAG, 0) ?: return
                viewModel.updateTag(tag)
            }
        }
    }

    private fun showSearchMenu(mode: ActionMode?, itemId: Int, start: Int, end: Int): Boolean {
        val searchTypeMap = mapOf(
                R.id.menu_search_google    to AddEditNoteViewModel.SearchType.GOOGLE,
                R.id.menu_search_wikipedia to AddEditNoteViewModel.SearchType.WIKIPEDIA,
                R.id.menu_search_weblio    to AddEditNoteViewModel.SearchType.WEBLIO)

        // floating action modeに対応しているかどうかでメニュー表示を分ける
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (itemId) {
                in searchTypeMap -> {
                    viewModel.startSearch(start, end, searchTypeMap[itemId]!!)
                    mode?.finish()
                    return true
                }
            }
        }
        else {
            if (itemId == R.id.menu_action_mode_search_web) {
                PopupMenu(context, activity?.findViewById(R.id.menu_action_mode_search_web)).apply {
                    inflate(R.menu.search)
                    setOnMenuItemClickListener {
                        if (it.itemId in searchTypeMap) { viewModel.startSearch(start, end, searchTypeMap[it.itemId]!!) }
                        true
                    }
                    show()
                }
                mode?.finish()
                return true
            }
        }
        return false
    }

    companion object {
        private val TAG = AddEditNoteFragment::class.java.simpleName
        const val ARGUMENT_ADD_EDIT_NOTE_ID = "argumentAddEditNoteId"
        const val ARGUMENT_ADD_EDIT_NOTE_TAG = "argumentAddEditNoteTag"
        const val REQUEST_CODE_SELECT_TAG = 0
    }
}