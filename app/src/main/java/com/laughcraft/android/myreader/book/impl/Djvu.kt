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

import android.content.res.Resources
import android.graphics.Bitmap
import com.github.axet.djvulibre.DjvuLibre
import com.laughcraft.android.myreader.book.abstr.ImageBook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.IOException

class Djvu(parent: String, child: String) : ImageBook(parent, child) {
    
    private lateinit var djvuLibre: DjvuLibre
    private lateinit var fileInputStream: FileInputStream
    
    override val pageCount: Int get() = djvuLibre.pagesCount
    
    override suspend fun getPage(index: Int): Bitmap? {
        return withContext(CoroutineScope(Default).coroutineContext) { processPage(index) }
    }
    
    @Throws(IOException::class)
    override suspend fun open(): Job {
        return CoroutineScope(IO).launch {
            fileInputStream = FileInputStream(this@Djvu)
            djvuLibre = DjvuLibre(fileInputStream.fd)
        }
    }
    
    override fun getTitle(): String? {
        val title = djvuLibre.getMeta(DjvuLibre.META_TITLE)
        return if (title.isNullOrBlank()) this.name
        else title
    }
    
    private fun processPage(index: Int): Bitmap {
        val dm = Resources.getSystem().displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        djvuLibre.renderPage(bitmap, index, 0, 0, width, height, 0, 0, width, height)
        return bitmap
    }
    
    override suspend fun close() {
        try {
            fileInputStream.close()
            djvuLibre.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

