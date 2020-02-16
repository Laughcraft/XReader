/*
 * Copyright (c) 2019.
 * Created by Vladislav Zraevskij
 *
 * This file is part of XReader.
 *
 *     XReader is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     XReader is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with XReader.  If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>.
 *
 */

package com.laughcraft.android.myreader.ui.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.android.material.appbar.AppBarLayout
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.contract.ViewTextBook
import com.laughcraft.android.myreader.presenter.TextPresenter
import com.laughcraft.android.myreader.ui.view.HorizontalWebView
import kotlinx.android.synthetic.main.activity_text_book.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

private const val TAG = "TextActivity"
private const val CHAPTER_KEY = "chapter"
private const val FONT_SIZE_KEY = "font_size"

class TextActivity : BookActivity(), ViewTextBook {
    @InjectPresenter lateinit var presenter: TextPresenter
    
    @ProvidePresenter
    fun provideTextBookPresenter(): TextPresenter {
        val uri = intent.data
        if (uri != null) {
            return TextPresenter(getFileFromUri(uri.path!!), 0)
        }
        
        val folder = intent.getStringExtra(FOLDER_KEY)!!
        val filename = intent.getStringExtra(FILENAME_KEY)!!
        
        val file = File(folder, filename)
        
        return TextPresenter(file, intent.getIntExtra(CHAPTER_KEY, 0),
                             intent.getIntExtra(FONT_SIZE_KEY, 14), intent.getIntExtra(PAGE_KEY, 0))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_book)
        setSupportActionBar(text_toolbar)
        
        text_toolbar.overflowIcon = ContextCompat.getDrawable(this@TextActivity,
                                                              R.drawable.ic_more_vert_24px)
        
        gestureDetector = GestureDetectorCompat(this, BookGestureListener(text_book_app_bar))
        
        activity_text_book_first_webview.gestureDetectorCompat = GestureDetectorCompat(this,
                                                                                       TextBookGestureListener(
                                                                                               text_book_app_bar,
                                                                                               activity_text_book_first_webview))
        
    }
    
    override fun onError(error: Throwable) {
        CoroutineScope(Main).launch {
            showErrorDialog(error)
        }
    }
    
    override fun onRestore() {
        CoroutineScope(Main).launch {
            text_progressBar.visibility = View.GONE
        }
    }
    
    override fun onClose() {
        presenter.closeBook()
    }
    
    override fun showChapter(baseUrl: String, chapter: String, mimeType: String, encoding: String, page: Int, fontSize: Int) {
        CoroutineScope(Main).launch {
            activity_text_book_first_webview.page = page
            activity_text_book_first_webview.fontSize = fontSize
            activity_text_book_first_webview.loadDataWithBaseURL(baseUrl, chapter, mimeType,
                                                                 encoding, null)
            
            activity_text_book_first_webview.rightOverScrollCallback = {
                if (presenter.hasNext()) {
                    presenter.loadNextChapter()
                }
            }
            
            activity_text_book_first_webview.leftOverScrollCallback = {
                if (presenter.hasPrevious()) {
                    presenter.loadPreviousChapter()
                }
            }
        }
    }
    
    override fun setTitle(title: String?) {
        CoroutineScope(Main).launch {
            activity_text_book_toolbar_title_textview.text = title
        }
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        onClose()
    }
    
    override fun goToPage(page: Int) {
        activity_text_book_first_webview.goToPage(page)
    }
    
    companion object {
        @JvmStatic
        fun getIntent(context: Context, folder: String, filename: String, chapter: Int, pageIndex: Int, fontSize: Int) = Intent(
                context, TextActivity::class.java).apply {
            putExtra(FOLDER_KEY, folder)
            putExtra(FILENAME_KEY, filename)
            putExtra(CHAPTER_KEY, chapter)
            putExtra(PAGE_KEY, pageIndex)
            putExtra(FONT_SIZE_KEY, fontSize)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_text_book, menu)
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        presenter.page = activity_text_book_first_webview.computeCurrentPage()
        when (item.itemId) {
            R.id.menu_text_book_add_bookmark -> showBookmarkDialog(presenter.bookFile.absolutePath,
                                                                   presenter.chapterIndex,
                                                                   activity_text_book_first_webview.computeCurrentPage(),
                                                                   activity_text_book_first_webview.fontSize)
            R.id.menu_text_book_convert -> convert(presenter.bookFile)
            R.id.menu_text_book_night_mode -> toggleNightMode()
            R.id.menu_text_book_text_size -> showFontSizeDialog()
            R.id.menu_text_book_table_of_contents -> showTableOfContentsDialog()
            R.id.menu_text_book_share -> share(presenter.bookFile)
            R.id.menu_text_book_forbid_rotations -> {
                toggleOrientationLock({
                                          CoroutineScope(Main).launch {
                                              delay(300)
                                              item.title = resources.getString(
                                                      R.string.menu_allow_rotations)
                                          }
                                      }, {
                                          CoroutineScope(Main).launch {
                                              delay(300)
                                              item.title = resources.getString(
                                                      R.string.menu_forbid_rotations)
                                          }
                                      })
            }
            R.id.menu_text_book_go_to -> openNavigationDialog(
                    activity_text_book_first_webview.getPageCount(),
                    activity_text_book_first_webview.computeCurrentPage())
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun toggleNightMode() {
        activity_text_book_first_webview.toggleNightMode()
    }
    
    private fun showFontSizeDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(View.inflate(this, R.layout.dialog_font_size, null))
        val seekbar = dialog.findViewById(R.id.dialog_font_size_seekbar) as SeekBar
        val fontTextView = dialog.findViewById(R.id.dialog_font_size_textview) as TextView
        
        fontTextView.text = resources.getString(R.string.selected_font_size_parameterized,
                                                presenter.fontSize)
        seekbar.progress = presenter.fontSize - 8
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(view: SeekBar?, progress: Int, p2: Boolean) {
                //webView_textBook.settings.defaultFontSize = progress+8
                activity_text_book_first_webview.fontSize = progress + 8
                fontTextView.text = resources.getString(R.string.selected_font_size_parameterized,
                                                        progress + 8)
                presenter.fontSize = progress + 8
            }
            
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        dialog.create()
        dialog.show()
    }
    
    private fun showTableOfContentsDialog() {
        val toc = presenter.tableOfContents
        if (toc.isNullOrEmpty()) {
            Toast.makeText(this, R.string.table_of_contents_is_null, Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(this).setTitle(R.string.table_of_contents).setItems(
                toc.toTypedArray()) { _, position ->
            presenter.loadChapter(position)
        }.create().show()
    }
    
    class TextBookGestureListener(private val appbar: AppBarLayout, private val webView: HorizontalWebView) : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (event1.y < 300 && event2.y - event1.y > 100 && abs(velocityY) > 100) {
                appbar.setExpanded(true, true)
                return true
            }
            if (event2.y < 300 && event1.y - event2.y > 100 && abs(velocityY) > 100) {
                appbar.setExpanded(false, true)
                return true
            }
            if (event2.x - event1.x > 50 && abs(velocityX) > 100) {
                webView.turnPageLeft()
                appbar.setExpanded(false, true)
                return true
            }
            if (event1.x - event2.x > 50 && abs(velocityX) > 100) {
                webView.turnPageRight()
                appbar.setExpanded(false, true)
                return true
            }
            return true
        }
    }
    
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP) {
            activity_text_book_first_webview.snapToClosestPage()
        }
        return super.dispatchTouchEvent(ev)
    }
    
    override fun onPause() {
        super.onPause()
        presenter.page = activity_text_book_first_webview.computeCurrentPage()
    }
}
