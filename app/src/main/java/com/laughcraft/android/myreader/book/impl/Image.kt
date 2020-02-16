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

package com.laughcraft.android.myreader.book.impl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.laughcraft.android.myreader.book.Extensions
import com.laughcraft.android.myreader.book.abstr.ImageBook
import com.laughcraft.android.myreader.model.FilenameComparator
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*

class Image(parent: String, child: String) : ImageBook(parent, child) {
    private val parentDirectory = File(parent)
    lateinit var images: MutableList<File>
    override var pageCount: Int = 0
    
    var currentPage = 0
    
    override suspend fun open(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            images = FileUtils.listFiles(parentDirectory,
                                         Extensions.ImageExtensions.extensions.toTypedArray(),
                                         false).toMutableList()
            val comparator = FilenameComparator()
            images.sortWith(Comparator { f1, f2 -> comparator.compare(f1.name, f2.name) })
            pageCount = images.size
            currentPage = images.indexOfFirst { it.absolutePath == this@Image.absolutePath }
        }
    }
    
    override suspend fun getPage(index: Int): Bitmap? {
        return withContext(CoroutineScope(Dispatchers.Default).coroutineContext) {
            BitmapFactory.decodeFile(images[index].absolutePath)
        }
    }
    
    override suspend fun close() {}
    override fun getTitle(): String? = null
}
