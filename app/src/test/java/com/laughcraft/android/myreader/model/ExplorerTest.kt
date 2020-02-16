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

class ExplorerTest {
    companion object{

        lateinit var explorer: Explorer
        lateinit var tempDir: File
        lateinit var tempInTemp: File

        @BeforeClass
        @JvmStatic
        fun init(){
            tempDir = File(FileUtils.getTempDirectory(), "TestDirectory")
            tempDir.mkdir()

            tempInTemp = File(tempDir, "TempInTemp")
            tempInTemp.mkdir()

            explorer = Explorer(tempDir.absolutePath)

            for (i in 1..5){
                val file = File(tempDir, "$i.txt")
                file.createNewFile()
                file.printWriter().write(1000*i)
            }

            for (i in 6..8){
                val file = File(tempDir, "$i.jpg")
                file.createNewFile()
                file.printWriter().write(7777*i)
            }

            for (i in 9..11){
                val file = File(tempDir, "$i.png")
                file.createNewFile()
                file.printWriter().write(8080*i)
            }

            for (i in 1..3){
                val file = File(tempInTemp, "abc$i.txt")
                file.createNewFile()
                file.printWriter().write(1000*i)
            }
        }

        @AfterClass
        @JvmStatic
        fun cleanDirectory(){
            FileUtils.deleteQuietly(tempDir)
        }
    }

    @Test
    fun explorer_Returns_12_Files_From_First_Directory(){
        explorer.openNewDirectory(tempDir.absolutePath)
        assertEquals(12, explorer.directoryFiles.size)
    }

    @Test
    fun explorer_Returns_Its_Directory(){
        explorer.openNewDirectory(tempDir.absolutePath)
        assertEquals(tempDir, explorer.directory)
    }

    @Test
    fun explorer_Returns_Its_Directory_Path(){
        explorer.openNewDirectory(tempDir.absolutePath)
        assertEquals(tempDir.absolutePath, explorer.path)
    }

    @Test
    fun explorer_Returns_Its_Parent_Path(){
        explorer.openNewDirectory(tempDir.absolutePath)
        assertEquals(tempDir.parent, explorer.parentPath)
    }

    @Test
    fun explorer_Returns_3_Files_From_Second_Directory(){
        explorer.openNewDirectory(tempInTemp.absolutePath)
        assertEquals(3, explorer.directoryFiles.size)
    }
}