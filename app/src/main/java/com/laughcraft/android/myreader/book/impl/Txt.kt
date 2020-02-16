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

import android.net.Uri
import com.laughcraft.android.myreader.book.abstr.TextBook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class Txt(parent: String, child: String) : TextBook(parent, child) {
    override lateinit var title: String
    override val chaptersCount: Int get() = 1
    override lateinit var encoding: String
    override val tableOfContents: List<String>? get() = null
    override val cover: String? get() = null
    override val mimeType: String get() = "text/plain"
    override val baseUrl: String get() = Uri.fromFile(this).toString()
    
    override var onError: (Throwable) -> Unit = {}
    
    override suspend fun open() = CoroutineScope(Dispatchers.Default).launch {
        encoding = getCharsetName(this@Txt)
        title = this@Txt.name
    }
    
    override suspend fun close() {}
    override suspend fun getChapter(index: Int): String {
        val text: String
        try {
            text = Scanner(this@Txt).useDelimiter("\\Z").next()
        } catch (exception: Throwable) {
            return ""
        }
        return text
    }
    
}
