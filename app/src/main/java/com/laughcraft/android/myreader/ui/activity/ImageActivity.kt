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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.book.abstr.ImageBook
import com.laughcraft.android.myreader.contract.ViewImageBook
import com.laughcraft.android.myreader.presenter.ImagePresenter
import com.laughcraft.android.myreader.ui.adapter.ImageBookAdapter
import com.laughcraft.android.myreader.ui.view.SnapHelperOneByOne
import kotlinx.android.synthetic.main.activity_image_book.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "ImageActivity"

class ImageActivity : BookActivity(), ViewImageBook {
    @InjectPresenter lateinit var imagePresenter: ImagePresenter
    
    @ProvidePresenter
    fun provideImageBookPresenter(): ImagePresenter {
        val uri = intent.data
        if (uri != null) {
            return ImagePresenter(getFileFromUri(uri.path!!), 0)
        }
        
        val fileDirectory = intent.getStringExtra(FOLDER_KEY)!!
        val filename = intent.getStringExtra(FILENAME_KEY)!!
        val page = intent.getIntExtra(PAGE_KEY, 0)
        
        return ImagePresenter(File(fileDirectory, filename), page)
    }
    
    private lateinit var imageFile: File
    private lateinit var layoutManager: LinearLayoutManager
    private var pageCount = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_book)
        
        layoutManager = LinearLayoutManager(this@ImageActivity, LinearLayoutManager.HORIZONTAL,
                                            false)
        image_book_recyclerview.layoutManager = layoutManager
        
        gestureDetector = GestureDetectorCompat(this, BookGestureListener(image_book_app_bar))
        setSupportActionBar(image_toolbar)
        image_toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_more_vert_24px)
        image_book_app_bar.setExpanded(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            launch(Dispatchers.Main) {
                SnapHelperOneByOne().attachToRecyclerView(image_book_recyclerview)
                image_book_recyclerview.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                    override fun onChildViewDetachedFromWindow(view: View) {}
                    
                    override fun onChildViewAttachedToWindow(view: View) {
                        image_book_app_bar.setExpanded(false, true)
                    }
                })
            }
        }
        imageFile = imagePresenter.file
        if (savedInstanceState != null) imagePresenter.restore()
    }
    
    override fun onError(error: Throwable) {
        showErrorDialog(error)
    }
    
    override fun setTitle(title: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            activity_image_book_toolbar_title_textview.text = title
        }
    }
    
    override fun showImageBook(djvuBook: ImageBook, startPage: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            image_book_recyclerview.adapter = ImageBookAdapter(djvuBook)
            image_book_recyclerview.scrollToPosition(startPage)
            image_progressBar.visibility = View.GONE
        }
    }
    
    override fun goToPage(page: Int) {
        image_book_recyclerview.scrollToPosition(page)
    }
    
    override fun onPause() {
        super.onPause()
        imagePresenter.page = layoutManager.findLastCompletelyVisibleItemPosition()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_image_book, menu)
        
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_image_book_add_bookmark -> showBookmarkDialog(imageFile.absolutePath,
                                                                    0,
                                                                    layoutManager.findFirstCompletelyVisibleItemPosition(),
                                                                    0)
            R.id.menu_image_book_convert -> convert(imageFile)
            R.id.menu_image_book_share -> share(imageFile)
            R.id.menu_image_book_forbid_rotations -> toggleOrientationLock({}, {})
            R.id.menu_image_book_go_to -> openNavigationDialog(pageCount)
        }
        return super.onOptionsItemSelected(item)
    }
    
    companion object {
        @JvmStatic
        fun getIntent(context: Context, folder: String, filename: String, pageIndex: Int = 0) = Intent(
                context, ImageActivity::class.java).apply {
            putExtra(FOLDER_KEY, folder)
            putExtra(FILENAME_KEY, filename)
            putExtra(PAGE_KEY, pageIndex)
        }
    }
}
