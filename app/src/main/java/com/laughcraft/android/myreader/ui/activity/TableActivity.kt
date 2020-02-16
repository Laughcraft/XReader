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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bin.david.form.data.CellRange
import com.bin.david.form.data.format.draw.TextDrawFormat
import com.bin.david.form.data.table.ArrayTableData
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.contract.ViewTable
import com.laughcraft.android.myreader.presenter.TablePresenter
import kotlinx.android.synthetic.main.activity_table.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.poi.ss.util.CellRangeAddress
import java.io.File

class TableActivity : BookActivity(), ViewTable {
    
    @InjectPresenter lateinit var presenter: TablePresenter
    
    @ProvidePresenter
    fun provideTablePresenter(): TablePresenter {
        val path = intent.getStringExtra(FOLDER_KEY)
        val name = intent.getStringExtra(FILENAME_KEY)
        val page = intent.getIntExtra(PAGE_KEY, 0)
        return TablePresenter(File(path, name), page)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)
        setSupportActionBar(table_toolbar)
        
        table_view.setZoom(true)
        
        table_toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_more_vert_24px)
        gestureDetector = GestureDetectorCompat(this, BookGestureListener(table_app_bar))
        presenter.onCreate()
    }
    
    override fun onError(error: Throwable) {
        showErrorDialog(error)
    }
    
    override fun showTable(tableName: String, table: Array<Array<String>>, mergedCells: Array<CellRangeAddress>) {
        CoroutineScope(Dispatchers.Main).launch {
            val names = Array(table.size) { "" }
            activity_table_toolbar_title_textview.text = tableName
            
            val tableData = ArrayTableData.create(tableName, names, table, TextDrawFormat<String>())
            val ranges = mutableListOf<CellRange>()
            mergedCells.forEach {
                ranges.add(CellRange(it.firstRow, it.lastRow, it.firstColumn, it.lastColumn))
            }
            tableData.userCellRange = ranges
            
            table_view.tableData = tableData
            
            
            table_progressBar.visibility = View.INVISIBLE
            table_app_bar.setExpanded(false, true)
        }
    }
    
    override fun goToPage(page: Int) {
        table_progressBar.visibility = View.VISIBLE
        presenter.loadSheet(page)
    }
    
    companion object {
        @JvmStatic
        fun getIntent(context: Context, folder: String, filename: String, pageIndex: Int = 0) = Intent(
                context, TableActivity::class.java).apply {
            putExtra(FOLDER_KEY, folder)
            putExtra(FILENAME_KEY, filename)
            putExtra(PAGE_KEY, pageIndex)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_table_book, menu)
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun openNavigationDialog(pagesCount: Int, startPage: Int) {
        AlertDialog.Builder(this).setTitle(R.string.dialog_sheets_title).setItems(
                presenter.getSheetNames()) { _, position ->
            table_progressBar.visibility = View.VISIBLE
            presenter.loadSheet(position)
        }.create().show()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_table_book_add_bookmark -> showBookmarkDialog(presenter.file.absolutePath, 0,
                                                                    presenter.sheet, 0)
            R.id.menu_table_book_convert -> convert(presenter.file)
            R.id.menu_table_book_share -> share(presenter.file)
            R.id.menu_table_book_go_to -> openNavigationDialog(presenter.sheetCount,
                                                               presenter.sheet)
            R.id.menu_table_book_forbid_rotations -> {
                toggleOrientationLock({
                                          CoroutineScope(Dispatchers.Main).launch {
                                              delay(300)
                                              item.title = resources.getString(
                                                      R.string.menu_allow_rotations)
                                          }
                                      }, {
                                          CoroutineScope(Dispatchers.Main).launch {
                                              delay(300)
                                              item.title = resources.getString(
                                                      R.string.menu_forbid_rotations)
                                          }
                                      })
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
