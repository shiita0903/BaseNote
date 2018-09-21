package jp.shiita.basenote.addeditnote

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Point
import android.graphics.PorterDuff
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
import jp.shiita.basenote.util.snackbar
import javax.inject.Inject


class AddEditNoteFragment @Inject constructor() : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AddEditNoteViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(AddEditNoteViewModel::class.java) }
    private lateinit var binding: FragAddEditNoteBinding
    private val noteId: String? by lazy { arguments?.getString(ARGUMENT_ADD_EDIT_NOTE_ID) }
    private val noteTag: Int by lazy { arguments?.getInt(ARGUMENT_ADD_EDIT_NOTE_TAG) ?: 0 }
    private val displayHeight: Int by lazy { Point().also { activity?.windowManager?.defaultDisplay?.getRealSize(it) }.y }
    private var webViewWidth = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_add_edit_note, container, false)
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

        binding.webView.run {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    viewModel.canGoBack.value = binding.webView.canGoBack()
                    viewModel.canGoForward.value = binding.webView.canGoForward()
                    super.onPageFinished(view, url)
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
                        mode?.menuInflater?.inflate(R.menu.search_web_menu, menu)
                    }
                    else {
                        // カット、全て選択の削除
                        menu?.removeItem(android.R.id.cut)
                        menu?.removeItem(android.R.id.selectAll)
                        mode?.menuInflater?.inflate(R.menu.action_mode_menu, menu)
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
                val y = maxOf(0f, minOf(displayHeight.toFloat(), event.rawY))
                binding.guideline.setGuidelinePercent(y / displayHeight)
                true
            }
        }
    }

    private fun observe() {
        // メニューを読み込み直して、タグの色を反映
        viewModel.tag.observe(this) { activity?.invalidateOptionsMenu() }
        viewModel.urlEvent.observe(this) { binding.webView.loadUrl(it) }
        viewModel.noteEmptyEvent.observe(this) { binding.root.snackbar(R.string.empty_note_message) }
        viewModel.noteSavedEvent.observe(this) { binding.root.snackbar(getString(R.string.save_note_message, it)) }
        viewModel.noteUpdatedEvent.observe(this) { binding.root.snackbar(getString(R.string.update_note_message, it)) }
        viewModel.noteDeleteEvent.observe(this) { activity?.finish() }  // TODO: 戻ったactivityでメッセージ
        viewModel.goBackEvent.observe(this)    { binding.webView.goBack() }
        viewModel.goForwardEvent.observe(this) { binding.webView.goForward() }
        viewModel.popupEvent.observe(this) {
            PopupMenu(context, binding.menuWebViewButton).apply {
                inflate(R.menu.web_view_menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.web_view_menu_update_link -> viewModel.updateCurrentClickableURLSpan(binding.webView.url)
                        R.id.web_view_menu_remove_link -> viewModel.removeClickableURLSpan(binding.webView.url)
                    }
                    true
                }
                show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_select_tag -> showSelectTagDialog()
            R.id.menu_delete -> viewModel.deleteNote()
            R.id.home -> activity?.finish()
            else -> return false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.addeditnote_fragment_menu, menu)
        val tag = viewModel.tag.value ?: 0
        // TODO: もっときれいに書けるはず
        if (tag != 0)
            menu?.getItem(0)?.icon?.setColorFilter(
                    resources.obtainTypedArray(R.array.tag_color).getColor(tag, 0),
                    PorterDuff.Mode.SRC_IN)
        else
            menu?.getItem(0)?.icon?.colorFilter = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            SELECT_TAG_REQUEST_CODE -> {
                val tag = data?.getIntExtra(SelectTagDialogFragment.ARGUMENT_TAG, 0) ?: return
                viewModel.updateTag(tag)
            }
        }
    }

    private fun showSelectTagDialog() {
        SelectTagDialogFragment().apply {
            setTargetFragment(this@AddEditNoteFragment, SELECT_TAG_REQUEST_CODE)
            arguments = Bundle().apply {
                putInt(SelectTagDialogFragment.ARGUMENT_TAG, viewModel.tag.value ?: 0)
            }
        }.show(fragmentManager, SelectTagDialogFragment.TAG)
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
            if (itemId == R.id.menu_search_web) {
                PopupMenu(context, activity?.findViewById(R.id.menu_search_web)).apply {
                    inflate(R.menu.search_web_menu)
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
        const val SELECT_TAG_REQUEST_CODE = 0
    }
}