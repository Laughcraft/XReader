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
import com.laughcraft.android.myreader.book.abstr.PdfBook
import com.laughcraft.android.myreader.contract.ViewPdf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@InjectViewState
class PdfPresenter(val file: File, var page: Int) : MvpPresenter<ViewPdf>() {
    private lateinit var pdfBook: PdfBook
    private var job: Job? = null
    
    @Inject lateinit var books: Books
    
    var nightMode = false
        set(mode) {
            field = mode
            viewState.setTitle(pdfBook.name)
            viewState.showPdf(pdfBook, page, field)
        }
    
    init {
        XReaderApplication.bookComponent.inject(this)
        try {
            job = CoroutineScope(Dispatchers.IO).launch {
                pdfBook = books.getBook(file) as PdfBook
                pdfBook.open().join()
            }
        } catch (throwable: Throwable) {
            viewState.onError(throwable)
        }
    }
    
    fun restore() {
        CoroutineScope(Dispatchers.IO).launch {
            job?.join()
            viewState.setTitle(pdfBook.name)
            viewState.showPdf(pdfBook, page, nightMode)
        }
        
    }
}