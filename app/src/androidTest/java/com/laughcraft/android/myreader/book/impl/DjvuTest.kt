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
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class DjvuTest{
    companion object{
        lateinit var djvu: Djvu

        @BeforeClass
        @JvmStatic
        fun init(){
            val stream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open("Churchill.djvu")

            val bookFile = File.createTempFile("book", ".pdf")

            FileUtils.copyInputStreamToFile(stream, bookFile)
            val temp = FileUtils.getTempDirectory()

            djvu = Djvu(bookFile.parent, bookFile.name)
            runBlocking {
                djvu.open().join()
            }
        }
    }

    @Test
    fun page_Count_Equals_To_320(){
        Log.i("TAGGGG", "Pages: ${djvu.pageCount}")
        Log.i("TAGGGG", "Title: ${djvu.getTitle()}")
        assertEquals(320, djvu.pageCount)
    }

    @Test
    fun first_Page_Size_Equals_To_3836160(){
        var page: Bitmap? = null
        runBlocking {
            page = djvu.getPage(0)
            Log.i("TAGGG", "" + page?.byteCount)
        }
        assertEquals(page?.byteCount, 3836160)
    }
}