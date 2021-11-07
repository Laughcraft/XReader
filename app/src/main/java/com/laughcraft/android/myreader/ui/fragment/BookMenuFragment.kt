package com.laughcraft.android.myreader.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.SeekBar
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.book.Book
import com.laughcraft.android.myreader.databinding.DialogMenuBinding
import com.laughcraft.android.myreader.databinding.DialogScrollBinding
import com.laughcraft.android.myreader.db.entity.BookmarkEntity
import com.laughcraft.android.myreader.string.Translator
import java.io.File
import javax.inject.Inject
import kotlin.math.abs

open class BookMenuFragment(@LayoutRes contentLayoutId: Int): Fragment(contentLayoutId) {

    @Inject lateinit var translator: Translator

    protected open lateinit var gestureDetector: GestureDetectorCompat

    private lateinit var binding: DialogMenuBinding

    protected open var convert: (()-> Unit)? = null
    protected open var navigate: (()-> Unit)? = null
    protected open var bookmark: (()-> Unit)? = null
    protected open var night: (()-> Unit)? = null
    protected open var options: (()-> Unit)? = null
    protected open var rename: (()-> Unit)? = null
    protected open var share: (()-> Unit)? = null

    open fun showMenu(root: ViewGroup) {
        val menuView = LayoutInflater.from(root.context).inflate(R.layout.dialog_menu, root, false)

        binding = DialogMenuBinding.bind(menuView)
        binding.apply {
            clBookMenu.setOnClickListener   { root.removeView(menuView) }
            fabConvert.setOnClickListener   { convert?.invoke() }
            fabBookmark.setOnClickListener  { bookmark?.invoke() }
            fabNavigate.setOnClickListener  { navigate?.invoke() }
            fabNight.setOnClickListener     { night?.invoke() }
            fabOptions.setOnClickListener   { options?.invoke() }
            fabRename.setOnClickListener    { rename?.invoke() }
            fabShare.setOnClickListener     { share?.invoke() }
        }

        root.addView(menuView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gestureDetector = GestureDetectorCompat(requireContext(), MenuGestureListener(view as ViewGroup, 0))
        view.setOnTouchListener { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }
    }

    open inner class MenuGestureListener(private val viewGroup: ViewGroup,
                                         private val orientation: Int): GestureDetector.SimpleOnGestureListener() {

        protected val velocityThreshold = 10

        override fun onFling(event1: MotionEvent?, event2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (event1 == null || event2 == null) return false
            Log.i("XReader.Fling", "Fling!")
            //todo constants
            if (//event1.y < viewGroup.height / 3 &&
                abs(event2.y - event1.y) > 50 &&
                abs(event2.y - event1.y) > abs(event2.x - event1.x) &&
                abs(velocityY) > velocityThreshold) {
                if (orientation == 0){
                    showMenu(viewGroup)
                    return true
                }
            }
//            if (event2.y < 300 && event1.y - event2.y > 100 && abs(velocityY) > velocityThreshold) {
//                if (orientation == 1){ showMenu(viewGroup) }
//            }

            return false
        }
    }

    fun shareFile(file: File){
        val intent = Intent(Intent.ACTION_SEND).apply {
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            setDataAndType(Uri.fromFile(file), mime)
        }

        startActivity(Intent.createChooser(intent, translator.getString(R.string.choose_app)))
    }

    fun showRenameDialog(file: File, onRenamePressed: (newName: String)->Unit){
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_rename, null, false)
        val tiet = dialogView.findViewById<TextInputEditText>(R.id.tietRename)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(translator.getString(R.string.rename))
            .setView(dialogView)
            .setPositiveButton(translator.getString(R.string.save)){ _, _ ->
                onRenamePressed.invoke(tiet.text.toString())
            }
            .setNegativeButton(translator.getString(android.R.string.cancel)){ _, _ -> }
            .create()

        tiet.setText(file.nameWithoutExtension)

        dialog.show()
    }

    fun showBookmarkDialog(book: Book, chapter: Int, page: Int, onBookmarkAdded: (BookmarkEntity)-> Unit){
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_rename, null, false)
        val tiet = dialogView.findViewById<TextInputEditText>(R.id.tietRename)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(translator.getString(R.string.rename))
            .setView(dialogView)
            .setPositiveButton(translator.getString(R.string.save)){ _, _ ->
                onBookmarkAdded.invoke(BookmarkEntity(0, book.absolutePath, chapter, page, tiet.text.toString()))
            }
            .setNegativeButton(translator.getString(android.R.string.cancel)){ _, _ -> }
            .create()

        dialog.show()
    }

    fun showNavigateDialog(currentPage: Int, currentChapter: Int,
                           pageRange: IntRange, chapterRange: IntRange,
                           onSelected:(chapter: Int, page: Int) -> Unit) {
        val b = DialogScrollBinding.inflate(layoutInflater)

        if (chapterRange.first < 0 || chapterRange.last < 0 || currentChapter < 0) {
            b.sbChapter.visibility = View.GONE
            b.tvChapter.visibility = View.GONE
            b.tvChapterTitle.visibility = View.GONE
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(translator.getString(R.string.dialog_go_to_title))
            .setView(b.root)
            .setPositiveButton(translator.getString(R.string.go)){ _, _ ->
                onSelected.invoke(b.sbChapter.progress, b.sbPage.progress)
            }
            .setNegativeButton(translator.getString(android.R.string.cancel)){ _, _ -> }
            .create()

        b.sbChapter.setOnSeekBarChangeListener( object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                b.tvChapter.text = (p1 + chapterRange.first).toString()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        b.sbPage.setOnSeekBarChangeListener( object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                b.tvPage.text = (p1 + pageRange.first).toString()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        b.sbChapter.max = chapterRange.last - chapterRange.first
        b.sbPage.max = pageRange.last - pageRange.first
        b.sbChapter.progress = currentChapter
        b.sbPage.progress = currentPage

        dialog.show()
    }
}