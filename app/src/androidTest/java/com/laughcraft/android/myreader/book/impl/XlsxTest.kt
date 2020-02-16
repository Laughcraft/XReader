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

class XlsxTest{
    companion object{
        lateinit var xlsx: Xlsx

        @BeforeClass
        @JvmStatic
        fun init(){
            val fileStream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open("conbud_year.xlsx")

            System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl")
            System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
            System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl")

            val bookFile = File.createTempFile("book", ".xlsx")

            FileUtils.copyInputStreamToFile(fileStream, bookFile)

            xlsx = Xlsx(bookFile.parent, bookFile.name)
            runBlocking {
                xlsx.open().join()
            }
        }
    }

    @Test
    fun xlsx_Length_Is_98662(){
        assertEquals(98662, xlsx.length())
    }

    @Test
    fun xlsx_Sheet_Count_Equals_3(){
        assertEquals(3, xlsx.sheetCount)
    }

    @Test
    fun xlsx_Sheet_Names_List_Equals_3(){
        assertEquals(3, xlsx.getSheetNames().size)
    }

    @Test
    fun xlsx_First_Sheet_Name_Is_Year(){
        assertEquals("год", xlsx.getSheetName(0))
    }

    @Test
    fun xlsx_First_Sheet_Merged_Cells_Size_Is_4(){
        assertEquals(4, xlsx.getMergedCells(0).size)
    }
}