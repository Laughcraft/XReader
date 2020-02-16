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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class EpubTest {
    companion object{
        lateinit var epub: Epub

        @BeforeClass
        @JvmStatic
        fun init(){
            val stream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open("Vern.epub")

            val bookFile = File.createTempFile("book", ".epub")

            FileUtils.copyInputStreamToFile(stream, bookFile)
            val temp = FileUtils.getTempDirectory()

            epub = Epub(bookFile.parent, bookFile.name, temp)
            runBlocking {
                epub.open().join()
            }
        }
    }

    @Test
    fun chapters_Size_Equals_To_67(){
        assertEquals(67, epub.chaptersCount)
    }

    @Test
    fun table_Of_Contents_Size_Equals_To_67(){
        assertEquals(67, epub.tableOfContents.size)
    }

    @Test
    fun title_Equals_To_Mysterious_Island(){
        assertEquals("Таинственный остров", epub.title)
    }

    @Test
    fun mime_Type_Equals_To_Text_Html(){
        assertEquals("text/html", epub.mimeType)
    }

    @Test
    fun encoding_Equals_To_UTF8(){
        assertEquals("UTF-8", epub.encoding)
    }

    @Test
    fun check_first_Chapter(){
        var chapter = ""
        runBlocking {
            chapter = epub.getChapter(0)
        }

        assertEquals(603, chapter.length)
    }


}