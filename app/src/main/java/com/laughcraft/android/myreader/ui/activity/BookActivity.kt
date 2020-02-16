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

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.GestureDetectorCompat
import com.arellomobile.mvp.MvpAppCompatActivity
import com.google.android.material.appbar.AppBarLayout
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReaderApplication
import com.laughcraft.android.myreader.book.Books
import com.laughcraft.android.myreader.database.Bookmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.abs

const val FOLDER_KEY = "folder_path"
const val FILENAME_KEY = "filename"
const val PAGE_KEY = "page"
const val BOOK_KEY = "book"

abstract class BookActivity : MvpAppCompatActivity() {
    protected var nightMode = false
    protected var orientationLocked = false
    protected lateinit var gestureDetector: GestureDetectorCompat
    protected var currentPage = 0
    
    @Inject lateinit var books: Books
    
    override fun onCreate(savedInstanceState: Bundle?) {
        XReaderApplication.bookComponent.inject(this)
        super.onCreate(savedInstanceState)
    }
    
    protected open fun toggleNightMode() {}
    protected abstract fun goToPage(page: Int)
    
    protected open fun openNavigationDialog(pagesCount: Int, startPage: Int = 0) {
        var targetPage = 0
        val dialog = AlertDialog.Builder(this).setTitle(R.string.dialog_go_to_title).setView(
                R.layout.dialog_go_to).setPositiveButton(R.string.dialog_button_go) { _, _ ->
            goToPage(targetPage)
        }.setNeutralButton(R.string.dismiss) { _, _ -> }.create()
        dialog.show()
        
        val seekbar: SeekBar = dialog.findViewById(R.id.dialog_go_to_seekbar)
        val textView: TextView = dialog.findViewById(R.id.dialog_go_to_textview)
        
        seekbar.progress = startPage
        seekbar.max = pagesCount - 1
        
        textView.text = resources.getString(R.string.dialog_go_to, startPage + 1)
        
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                textView.text = resources.getString(R.string.dialog_go_to, p1 + 1)
                targetPage = p1
            }
            
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }
    
    protected open fun toggleOrientationLock(onLockEnabled: () -> Unit, onLockDisabled: () -> Unit) {
        if (!orientationLocked) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            orientationLocked = true
            onLockEnabled.invoke()
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            orientationLocked = false
            onLockDisabled.invoke()
        }
    }
    
    protected open fun showBookmarkDialog(path: String, chapter: Int, page: Int, fontSize: Int) {
        var comment: String? = ""
        
        var dialog: AlertDialog
        var bookmarks: List<Bookmark>
        
        val dao = XReaderApplication.filesDatabase.favoriteFilesDao()
        CoroutineScope(Dispatchers.IO).launch {
            bookmarks = dao.getFileBookmarks(path)
            var thisBookmarkComment: String? = ""
            CoroutineScope(Dispatchers.Main).launch {
                
                dialog = if (bookmarks.any {
                        thisBookmarkComment = it.comment
                        return@any it.page == page && it.chapter == chapter
                    }) {
                    createBookmarkDialog(R.string.bookmark, R.string.delete_button,
                                         R.layout.dialog_bookmark) {
                        deleteBookmark(Bookmark(path, chapter, page, fontSize, comment))
                    }
                } else {
                    createBookmarkDialog(R.string.add_bookmark, R.string.add_bookmark_button,
                                         R.layout.dialog_bookmark) {
                        addBookmark(Bookmark(path, chapter, page, fontSize, comment))
                    }
                }
                
                dialog.show()
                
                val pageIndexTextView = dialog.findViewById(
                        R.id.dialog_bookmark_page_index_textview) as TextView
                pageIndexTextView.text = resources.getString(R.string.bookmark_page_index, page + 1)
                
                val commentEditText = dialog.findViewById(
                        R.id.dialog_bookmark_comment_editText) as EditText
                commentEditText.setText(thisBookmarkComment)
                
                commentEditText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {}
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }
                    
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        comment = p0.toString()
                    }
                })
            }
        }
    }
    
    protected open fun createBookmarkDialog(@StringRes titleId: Int,
                                            @StringRes positiveButtonTextId: Int,
                                            @LayoutRes layoutId: Int,
                                            action: () -> Unit): AlertDialog {
        return AlertDialog.Builder(this).setTitle(titleId).setView(layoutId).setPositiveButton(
                positiveButtonTextId) { _, _ -> action.invoke() }.setNeutralButton(
                R.string.dismiss) { _, _ -> }.create()
    }
    
    protected open fun addBookmark(bookmark: Bookmark) {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = XReaderApplication.filesDatabase.favoriteFilesDao()
            
            dao.insertFile(bookmark)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@BookActivity, R.string.menu_bookmark_added,
                               Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    protected open fun deleteBookmark(bookmark: Bookmark) {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = XReaderApplication.filesDatabase.favoriteFilesDao()
            
            dao.deleteFile(bookmark)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@BookActivity, R.string.bookmark_deleted,
                               Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    protected open fun getFileFromUri(uri: String): File {
        val firstCharIndex = uri.lastIndexOf(':') + 2
        val path = uri.substring(firstCharIndex)
        
        return File(path)
    }
    
    protected open fun convert(file: File) {
        startActivity(ConverterActivity.getIntent(this, file.parent!!, file.name))
    }
    
    protected open fun share(book: File) {
        var intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(book))
        intent.type = "application/pdf"
        intent = Intent.createChooser(intent,
                                      resources.getString(R.string.menu_intent_send_file_title))
        startActivity(intent)
    }
    
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
    
    protected fun showErrorDialog(error: Throwable) {
        CoroutineScope(Dispatchers.Main).launch {
            AlertDialog.Builder(this@BookActivity).setTitle(
                    R.string.could_not_open_error_dialog_title).setNeutralButton(
                    R.string.dismiss) { _, _ -> Unit }.setMessage(error.message).create().show()
        }
    }
    
    class BookGestureListener(private val appbar: AppBarLayout) : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (event1.y < 400 && event2.y - event1.y > 100 && abs(velocityY) > 100) {
                appbar.setExpanded(true, true)
                return true
            }
            if (event2.y < 400 && event1.y - event2.y > 100 && abs(velocityY) > 100) {
                appbar.setExpanded(false, true)
                return true
            }
            return true
        }
    }
}
