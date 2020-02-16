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

import android.content.Context
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReaderApplication
import com.laughcraft.android.myreader.contract.ViewFavorites
import com.laughcraft.android.myreader.database.Bookmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@InjectViewState
class FavoritesPresenter(private val context: Context) : MvpPresenter<ViewFavorites>() {
    private lateinit var allBookmarks: List<Bookmark>
    private lateinit var filteredFiles: List<File>
    
    private fun getBookmarks(): List<Bookmark> {
        val bookmarks: MutableList<Bookmark> = mutableListOf()
        val favorites = XReaderApplication.filesDatabase.favoriteFilesDao().allFiles
        favorites.forEach { bookmark ->
            val file = File(bookmark.filePath)
            if (file.exists()) bookmarks.add(bookmark)
            else XReaderApplication.filesDatabase.favoriteFilesDao().deleteFile(bookmark)
        }
        
        return bookmarks
    }
    
    private fun filterCopies(bookmarks: List<Bookmark>): List<File> {
        val files = mutableListOf<File>()
        bookmarks.distinctBy { it.filePath }.forEach { files.add(File(it.filePath)) }
        
        return files
    }
    
    fun showBookmarks() {
        CoroutineScope(Dispatchers.IO).launch {
            allBookmarks = getBookmarks()
            filteredFiles = filterCopies(allBookmarks)
            viewState.showBookmarks(filteredFiles)
        }
    }
    
    fun openBookmarkedBook(position: Int) {
        val file = filteredFiles[position]
        val bookmarks = allBookmarks.filter { it.filePath == file.absolutePath }
        
        if (bookmarks.size == 1) viewState.openBook(bookmarks[0])
        else {
            val pageStrings: List<String> = bookmarks.map {
                "${context.resources.getString(R.string.chapter_page, it.chapter,
                                               it.page + 1)} ${it.comment}"
            }
            viewState.showBookmarkChoosingDialog(pageStrings) { pos ->
                viewState.openBook(bookmarks[pos])
            }
        }
    }
    
    fun deleteBookmarkedBook(position: Int) {
        val file = filteredFiles[position]
        val bookmarks = allBookmarks.filter { it.filePath == file.absolutePath }
        
        if (bookmarks.size == 1) viewState.showDeleteDialog { deleteBookmark(bookmarks[0]) }
        else {
            val pageStrings: List<String> = bookmarks.map {
                "${context.resources.getString(R.string.chapter_page, it.chapter,
                                               it.page + 1)} ${it.comment}"
            }
            
            viewState.showBookmarkChoosingDialog(pageStrings) { pos ->
                viewState.showDeleteDialog { deleteBookmark(bookmarks[pos]) }
            }
        }
    }
    
    private fun deleteBookmark(bookmark: Bookmark) {
        CoroutineScope(Dispatchers.IO).launch {
            XReaderApplication.filesDatabase.favoriteFilesDao().deleteFile(bookmark)
            showBookmarks()
        }
    }
}