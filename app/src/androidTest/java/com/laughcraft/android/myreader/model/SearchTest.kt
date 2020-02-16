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

import org.apache.commons.io.FileUtils
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class SearchTest {
    companion object {

        lateinit var explorer: Explorer
        lateinit var tempDir: File
        lateinit var tempInTemp: File

        @BeforeClass
        @JvmStatic
        fun init() {
            tempDir = File(FileUtils.getTempDirectory(), "TestDirectory")
            tempDir.mkdir()

            tempInTemp = File(tempDir, "TempInTemp")
            tempInTemp.mkdir()

            explorer = Explorer(tempDir.absolutePath)

            for (i in 1..5) {
                val file = File(tempDir, "$i.txt")
                file.createNewFile()
                file.printWriter().write(1000 * i)
            }

            for (i in 6..8) {
                val file = File(tempDir, "$i.jpg")
                file.createNewFile()
                file.printWriter().write(7777 * i)
            }

            for (i in 9..11) {
                val file = File(tempDir, "$i.png")
                file.createNewFile()
                file.printWriter().write(8080 * i)
            }

            for (i in 1..3) {
                val file = File(tempInTemp, "abc$i.txt")
                file.createNewFile()
                file.printWriter().write(1000 * i)
            }
        }

        @AfterClass
        @JvmStatic
        fun cleanDirectory() {
            FileUtils.deleteQuietly(tempDir)
        }
    }


    @Test
    fun search_Finds_Four_Files_By_Query_1() {
        var res: List<File>? = null
        explorer.searchInDirectory("1") { files -> res = files }

        Thread.sleep(1500)
        assertEquals(4, res!!.size)
    }

    @Test
    fun search_Finds_Four_Files_By_Query_a() {
        var res: List<File>? = null
        explorer.searchInDirectory("a") { files -> res = files }

        Thread.sleep(1500)
        assertEquals(3, res!!.size)
    }

    @Test
    fun search_Finds_Four_Files_By_Query_txt() {
        var res: List<File>? = null
        explorer.searchInDirectory("txt") { files -> res = files }

        Thread.sleep(1500)
        assertEquals(8, res!!.size)
    }
}