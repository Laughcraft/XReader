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
import android.os.Environment
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.contract.ViewExplorer
import com.laughcraft.android.myreader.model.Explorer
import com.laughcraft.android.myreader.model.Sorting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Suppress("DEPRECATION")
@InjectViewState
class ExplorerPresenter(private val appContext: Context) : MvpPresenter<ViewExplorer>() {
    var isItHomeDirectory = true
    
    private val fakeHomeDirectory: List<File> = createFakeHomeDirectory()
    private val fakeTitle = appContext.getString(R.string.explorer_title)
    
    private val HOME_DIRECTORY_PATH = Environment.getExternalStorageDirectory().absolutePath
    
    var sortingType = 1
        set(value) {
            field = if (value == field) -value
            else value
            updateAdapter(mLastSentFiles)
        }
    private val directory: Explorer = Explorer(HOME_DIRECTORY_PATH)
    
    private var mLastSentFiles: List<File>? = null
    
    fun openInnerDirectory(newDirectoryPath: String) {
        updateAdapter(directory.openNewDirectory(newDirectoryPath))
        isItHomeDirectory = false
        if (directory.directory.absolutePath == Environment.getExternalStorageDirectory().absolutePath) viewState.setTitle(
                appContext.getString(R.string.internal_storage))
        else viewState.setTitle(directory.directory.name)
    }
    
    fun openUpperDirectory() {
        if (!isItHomeDirectory && fakeHomeDirectory.any { it.absolutePath == directory.directory.absolutePath }) {
            updateFakeAdapter()
            isItHomeDirectory = true
        } else {
            updateAdapter(directory.openNewDirectory(directory.parentPath))
            
            if (directory.directory.absolutePath == Environment.getExternalStorageDirectory().absolutePath) viewState.setTitle(
                    appContext.getString(R.string.internal_storage))
            else viewState.setTitle(directory.directory.name)
        }
    }
    
    fun searchFile(query: String) {
        viewState.updateFiles(null)
        directory.searchInDirectory(query) { updateAdapter(it) }
    }
    
    fun onOpenSearchView() {
    
    }
    
    fun onCloseSearchView() {
        if (isItHomeDirectory) updateFakeAdapter()
        else updateAdapter(directory.directoryFiles)
    }
    
    private fun sort(files: List<File>?) {
        when (sortingType) {
            1 -> Sorting.sortByName(files, false)
            2 -> Sorting.sortByDate(files, false)
            3 -> Sorting.sortByExtension(files, false)
            4 -> Sorting.sortBySize(files, false)
            
            -1 -> Sorting.sortByName(files, true)
            -2 -> Sorting.sortByDate(files, true)
            -3 -> Sorting.sortByExtension(files, true)
            -4 -> Sorting.sortBySize(files, true)
        }
    }
    
    private fun updateFakeAdapter() {
        updateAdapter(fakeHomeDirectory)
        viewState.setTitle(fakeTitle)
    }
    
    private fun updateAdapter(files: List<File>?) {
        CoroutineScope(Dispatchers.Main).launch {
            sort(files)
            
            viewState.updateFiles(files)
            mLastSentFiles = files
        }
    }
    
    fun onResume() {
        if (isItHomeDirectory) {
            updateFakeAdapter()
        } else {
            updateAdapter(directory.directoryFiles)
            if (directory.directory.absolutePath == Environment.getExternalStorageDirectory().absolutePath) viewState.setTitle(
                    appContext.getString(R.string.internal_storage))
            else viewState.setTitle(directory.directory.name)
        }
    }
    
    fun onCreateView() {
        directory.openNewDirectory(HOME_DIRECTORY_PATH)
        updateAdapter(fakeHomeDirectory)
        viewState.setTitle(appContext.getString(R.string.explorer_title))
    }
    
    private fun createFakeHomeDirectory(): List<File> {
        val fakeDirFiles: MutableList<File> = mutableListOf()
        
        if (Environment.getExternalStorageDirectory().exists()) fakeDirFiles.add(
                Environment.getExternalStorageDirectory())
        
        if (Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).exists()) fakeDirFiles.add(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM))
        
        if (Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS).exists()) fakeDirFiles.add(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS))
        
        if (Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).exists()) fakeDirFiles.add(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
        
        if (Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES).exists()) fakeDirFiles.add(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES))
        
        if (Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).exists()) fakeDirFiles.add(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
        
        return fakeDirFiles
    }
}
