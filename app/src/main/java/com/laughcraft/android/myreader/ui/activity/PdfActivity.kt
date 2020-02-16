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
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.book.abstr.PdfBook
import com.laughcraft.android.myreader.contract.ViewPdf
import com.laughcraft.android.myreader.presenter.PdfPresenter
import kotlinx.android.synthetic.main.activity_pdf.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class PdfActivity : BookActivity(), ViewPdf {
    @InjectPresenter lateinit var pdfPresenter: PdfPresenter
    
    @ProvidePresenter
    fun providePdfPresenter(): PdfPresenter {
        val uri = intent.data
        if (uri != null) {
            return PdfPresenter(getFileFromUri(uri.path!!), 0)
        }
        
        val path = intent.getStringExtra(FOLDER_KEY)
        val name = intent.getStringExtra(FILENAME_KEY)
        val page = intent.getIntExtra(PAGE_KEY, 0)
        
        return PdfPresenter(File(path, name), page)
    }
    
    override fun setTitle(title: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            activity_pdf_toolbar_title_textview.text = title
        }
    }
    
    override fun showPdf(pdfBook: PdfBook, page: Int, nightMode: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            pdf_view.fromFile(pdfBook.pdfFile).swipeHorizontal(true).pageSnap(true).defaultPage(
                    page).onPageChange { newPage, _ ->
                pdf_book_app_bar.setExpanded(false, true)
                pdfPresenter.page = newPage
            }.autoSpacing(true).nightMode(nightMode).pageFling(true).load()
            pdf_progressBar.visibility = View.GONE
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)
        setSupportActionBar(pdf_toolbar)
        pdf_toolbar.overflowIcon = ContextCompat.getDrawable(this@PdfActivity,
                                                             R.drawable.ic_more_vert_24px)
        
        gestureDetector = GestureDetectorCompat(this, BookGestureListener(pdf_book_app_bar))
        pdf_book_app_bar.setExpanded(true, true)
        pdfPresenter.restore()
    }
    
    override fun onError(error: Throwable) {
        showErrorDialog(error)
    }
    
    override fun goToPage(page: Int) {
        pdf_view.jumpTo(page)
    }
    
    companion object {
        @JvmStatic
        fun getIntent(context: Context, folder: String, filename: String, pageIndex: Int = 0) = Intent(
                context, PdfActivity::class.java).apply {
            putExtra(FOLDER_KEY, folder)
            putExtra(FILENAME_KEY, filename)
            putExtra(PAGE_KEY, pageIndex)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_pdf_book, menu)
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_pdf_book_add_bookmark -> showBookmarkDialog(pdfPresenter.file.absolutePath, 0,
                                                                  pdf_view.currentPage, 0)
            R.id.menu_pdf_book_convert -> convert(pdfPresenter.file)
            R.id.menu_pdf_book_night_mode -> toggleNightMode()
            R.id.menu_pdf_book_share -> share(pdfPresenter.file)
            R.id.menu_pdf_book_go_to -> openNavigationDialog(pdf_view.pageCount,
                                                             pdf_view.currentPage)
            R.id.menu_pdf_book_forbid_rotations -> {
                toggleOrientationLock({ CoroutineScope(Dispatchers.Main).launch {
                                              delay(300)
                                              item.title = resources.getString(
                                                      R.string.menu_allow_rotations)
                                          }
                                      }, { CoroutineScope(Dispatchers.Main).launch {
                                              delay(300)
                                              item.title = resources.getString(
                                                      R.string.menu_forbid_rotations)
                                          }
                                      })
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun toggleNightMode() {
        nightMode = !nightMode
        pdfPresenter.nightMode = nightMode
    }
}
