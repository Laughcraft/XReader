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

package com.laughcraft.android.myreader.model

import androidx.collection.ArrayMap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class Search(private val mDirectory: File) {
    private val conditions: MutableMap<String, (file: File) -> Boolean> = ArrayMap(1)
    private val NAME = "name"
    
    fun addName(name: String): Search {
        conditions.remove(NAME)
        conditions[NAME] = { file: File -> file.name.toLowerCase().contains(name.toLowerCase()) }
        return this
    }
    
    fun search(onComplete: (files: List<File>) -> Unit) {
        val searchResults = mutableListOf<File>()
        CoroutineScope(Dispatchers.IO).launch {
            searchInDirectory(mDirectory, conditions, searchResults)
            onComplete.invoke(searchResults)
        }
    }
    
    private fun searchInDirectory(directory: File,
                                  conditions: MutableMap<String, (file: File) -> Boolean>,
                                  searchResults: MutableList<File>) {
        val files = directory.listFiles()
        if (files.isNullOrEmpty()) return
        
        for (entry in files) {
            if (checkFile(entry, conditions)) {
                searchResults.add(entry)
            }
            
            if (entry.isDirectory) {
                searchInDirectory(entry, conditions, searchResults)
            }
        }
    }
    
    @Throws(Exception::class)
    private fun checkFile(file: File,
                          conditions: MutableMap<String, (file: File) -> Boolean>): Boolean {
        conditions.values.forEach {
            if (!it.invoke(file)) return false
        }
        return true
    }
}