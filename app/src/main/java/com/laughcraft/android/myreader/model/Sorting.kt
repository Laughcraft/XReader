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

import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

object Sorting {
    fun sortByName(files: List<File>?, reversed: Boolean): List<File>? {
        if (!reversed) sort(files, kotlin.Comparator { file1, file2 ->
            FilenameComparator().compare(file1.name, file2.name)
        })
        else sort(files, Collections.reverseOrder { file1, file2 ->
            FilenameComparator().compare(file1.name, file2.name)
        })
        
        return sortByFileType(files)
    }
    
    fun sortByDate(files: List<File>?, reversed: Boolean): List<File>? {
        if (!reversed) sort(files, kotlin.Comparator { file1, file2 ->
            val date1: Long = if (file1.isFile) file1.lastModified()
            else file1.lastModified()
            val date2: Long = if (file2.isFile) file2.lastModified()
            else file2.lastModified()
            
            val differenceInTime = date1 - date2
            if (differenceInTime > 0) return@Comparator 1
            if (differenceInTime < 0) return@Comparator -1
            
            0
        })
        else sort(files, Collections.reverseOrder { file1, file2 ->
            val date1: Long = if (file1.isFile) file1.lastModified()
            else file1.lastModified()
            val date2: Long = if (file2.isFile) file2.lastModified()
            else file2.lastModified()
            
            val differenceInTime = date1 - date2
            if (differenceInTime > 0) return@reverseOrder 1
            if (differenceInTime < 0) return@reverseOrder -1
            
            0
        })
        
        return sortByFileType(files)
    }
    
    fun sortBySize(files: List<File>?, reversed: Boolean): List<File>? {
        if (!reversed) sort(files, kotlin.Comparator { file1, file2 ->
            val size1: Long = if (file1.isFile) file1.length()
            else file1.length()
            
            val size2: Long = if (file2.isFile) file2.length()
            else file2.length()
            
            val differenceInSizes = size1 - size2
            if (differenceInSizes > 0) return@Comparator 1
            if (differenceInSizes < 0) return@Comparator -1
            
            0
        })
        else sort(files, Collections.reverseOrder { file1, file2 ->
            val size1: Long = if (file1.isFile) file1.length()
            else file1.length()
            
            val size2: Long = if (file2.isFile) file2.length()
            else file2.length()
            
            val differenceInSizes = size1 - size2
            if (differenceInSizes > 0) return@reverseOrder 1
            if (differenceInSizes < 0) return@reverseOrder -1
            
            0
        })
        return sortByFileType(files)
    }
    
    fun sortByExtension(files: List<File>?, reversed: Boolean): List<File>? {
        if (!reversed) sort(files, kotlin.Comparator { file1, file2 ->
            if (file1.isDirectory && file2.isDirectory) {
                return@Comparator file1.name.compareTo(file2.name, ignoreCase = true)
            } else if (file1.isDirectory && !file2.isDirectory) {
                return@Comparator 1
            } else if (!file1.isDirectory && file2.isDirectory) {
                return@Comparator -1
            } else {
                return@Comparator FilenameUtils.getExtension(file1.name).compareTo(
                        FilenameUtils.getExtension(file2.name), ignoreCase = true)
            }
        })
        else sort(files, Collections.reverseOrder { file1, file2 ->
            if (file1.isDirectory && file2.isDirectory) {
                return@reverseOrder file1.name.compareTo(file2.name, ignoreCase = true)
            } else if (file1.isDirectory && !file2.isDirectory) {
                return@reverseOrder 1
            } else if (!file1.isDirectory && file2.isDirectory) {
                return@reverseOrder -1
            } else {
                return@reverseOrder FilenameUtils.getExtension(file1.name).compareTo(
                        FilenameUtils.getExtension(file2.name), ignoreCase = true)
            }
        })
        return sortByFileType(files)
    }
    
    private fun sortByFileType(files: List<File>?): List<File>? {
        sort(files, kotlin.Comparator { file1, file2 ->
            if (file1.isDirectory && !file2.isDirectory) return@Comparator -1
            if (file2.isDirectory && !file1.isDirectory) return@Comparator 1
            0
        })
        return files
    }
    
    private fun sort(files: List<File>?, comparator: Comparator<File>): List<File>? {
        if (files == null || files.size <= 1) return files
        
        Collections.sort(files, comparator)
        return files
    }
}
