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

package com.laughcraft.android.myreader.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.laughcraft.android.myreader.XReaderApplication
import com.laughcraft.android.myreader.book.Books
import com.laughcraft.android.myreader.book.abstr.TableBook
import com.laughcraft.android.myreader.contract.ViewTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@InjectViewState
class TablePresenter(val file: File, var sheet: Int) : MvpPresenter<ViewTable>() {
    @Inject lateinit var books: Books
    
    private val tableBook: TableBook
    
    private var job: Job? = null
    
    var sheetCount: Int = 0
        get() = tableBook.sheetCount
        private set
    
    init {
        XReaderApplication.bookComponent.inject(this)
        tableBook = books.getBook(file) as TableBook
        try {
            CoroutineScope(Dispatchers.IO).launch {
                job = tableBook.open()
                sheetCount = tableBook.sheetCount
            }
        } catch (throwable: Throwable) {
            viewState.onError(throwable)
        }
        
    }
    
    fun onCreate() {
        load(sheet)
    }
    
    private fun load(sheet: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            job?.join()
            try {
                viewState.showTable(tableBook.getSheetName(sheet), tableBook.getSheet(sheet),
                                    tableBook.getMergedCells(sheet))
            } catch (throwable: Throwable) {
                viewState.onError(throwable)
            }
        }
    }
    
    fun loadSheet(sheet: Int) {
        load(sheet)
    }
    
    fun getSheetNames(): Array<String> {
        return tableBook.getSheetNames()
    }
}