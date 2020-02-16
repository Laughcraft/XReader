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

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class TxtTest{
    companion object{
        lateinit var txt: Txt

        @BeforeClass
        @JvmStatic
        fun init(){
            val stream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open("Flag.txt")

            val bookFile = File.createTempFile("book", ".txt")

            FileUtils.copyInputStreamToFile(stream, bookFile)

            txt = Txt(bookFile.parent, bookFile.name)
            runBlocking {
                txt.open().join()
            }

        }
    }
    @Test
    fun checkContent(){
        var chapter = ""
        runBlocking {
            chapter = txt.getChapter(0)
        }
        assertTrue(chapter.startsWith("Флаг"))
    }
}