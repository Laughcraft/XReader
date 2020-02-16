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

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class Fb2Test{

    companion object{

        lateinit var fb2: Fb2

        @BeforeClass
        @JvmStatic
        fun init(){
            val stream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open("Friconomics.fb2")

            val bookFile = File.createTempFile("book", ".fb2")

            FileUtils.copyInputStreamToFile(stream, bookFile)
            val temp = FileUtils.getTempDirectory()

            fb2 = Fb2(bookFile.parent, bookFile.name, temp)
            runBlocking {
                fb2.open().join()
            }
        }
    }

    @Test
    fun chapters_Size_Equals_To_19(){
        Log.i("TAGGGG", "Chapters: ${fb2.chaptersCount}")
        Log.i("TAGGGG", "Encoding: ${fb2.encoding}")
        Log.i("TAGGGG", "Title: ${fb2.title}")
        Log.i("TAGGGG", "Mime: ${fb2.mimeType}")
        Log.i("TAGGGG", "TOC: ${fb2.tableOfContents.size}")
        assertEquals(19, fb2.chaptersCount)
    }

    @Test
    fun table_Of_Contents_Size_Equals_To_19(){
        assertEquals(19, fb2.tableOfContents.size)
    }

    @Test
    fun title_Equals_To_Friconomics(){
        assertEquals("Фрикономика. Мнение экономиста-диссидента о неожиданных связях между событиями и явлениями", fb2.title)
    }

    @Test
    fun mime_Type_Equals_To_Text_Html(){
        assertEquals("text/html", fb2.mimeType)
    }

    @Test
    fun encoding_Equals_To_utf8(){
        assertEquals("utf-8", fb2.encoding)
    }

    @Test
    fun check_first_Chapter(){
        var chapter = ""
        runBlocking {
            chapter = fb2.getChapter(0)
        }

        assertEquals(6164, chapter.length)
    }

}