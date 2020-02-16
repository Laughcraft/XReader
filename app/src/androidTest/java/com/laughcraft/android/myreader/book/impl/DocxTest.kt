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
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class DocxTest{
    companion object{
        lateinit var docx: Docx

        @BeforeClass
        @JvmStatic
        fun init(){
            val fileStream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open("Minfin.docx")
            val fontStream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open("times.ttf")

            System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl")
            System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
            System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl")

            val bookFile = File.createTempFile("book", ".docx")
            val fontFile = File.createTempFile("font", ".ttf")
            fontFile.createNewFile()

            FileUtils.copyInputStreamToFile(fileStream, bookFile)
            FileUtils.copyInputStreamToFile(fontStream, fontFile)

            docx = Docx(bookFile.parent, bookFile.name, FileUtils.getTempDirectory(), fontFile.absolutePath)
            runBlocking {
                docx.open().join()
            }
        }
    }

    @Test
    fun docx_length_equals_13150(){
        assertEquals(13150, docx.length())
    }

    @Test
    fun docx_true_file_length_equals_34655(){
        assertEquals(34655, docx.pdfFile.length())
    }
}