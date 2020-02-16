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
import com.laughcraft.android.myreader.book.abstr.TextBook
import com.laughcraft.android.myreader.contract.ViewTextBook
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@InjectViewState
class TextPresenter(val bookFile: File, var chapterIndex: Int, var fontSize: Int = 16, var page: Int = 0) : MvpPresenter<ViewTextBook>() {
    
    private lateinit var textBook: TextBook
    
    private var currentChapter: String? = null
    
    @Inject lateinit var books: Books
    
    val tableOfContents: List<String>? get() = textBook.tableOfContents
    
    val handler = CoroutineExceptionHandler { _, exception ->
    }
    
    init {
        XReaderApplication.bookComponent.inject(this)
        try {
            CoroutineScope(Dispatchers.IO).launch(handler) {
                textBook = books.getBook(bookFile) as TextBook
                textBook.onError = { error -> viewState.onError(error) }
                textBook.open().join()
                loadChapter(chapterIndex)
            }
        } catch (throwable: Throwable) {
            viewState.onError(throwable)
        }
    }
    
    fun loadChapter(chapterIndex: Int) {
        CoroutineScope(Dispatchers.IO).launch(handler) {
            if (this@TextPresenter.chapterIndex != chapterIndex) {
                currentChapter = textBook.getChapter(chapterIndex)
                page = 0
            } else {
                if (currentChapter == null) currentChapter = textBook.getChapter(chapterIndex)
            }
            viewState.showChapter(textBook.baseUrl, currentChapter!!, textBook.mimeType,
                                  textBook.encoding, page, fontSize)
            viewState.setTitle(textBook.title)
            viewState.onRestore()
            this@TextPresenter.chapterIndex = chapterIndex
        }
    }
    
    fun closeBook() {
        CoroutineScope(Dispatchers.IO).launch {
            textBook.close()
        }
    }
    
    fun hasNext(): Boolean = chapterIndex < textBook.chaptersCount - 1
    
    fun hasPrevious(): Boolean = chapterIndex > 0
    
    fun loadNextChapter() = loadChapter(chapterIndex + 1)
    fun loadPreviousChapter() = loadChapter(chapterIndex - 1)
}