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

import org.junit.Assert.assertTrue
import org.junit.Test

class FilenameUtilsTest{
    @Test
    fun bytes_2000_Equals_To_1_95_Kb(){
        assertTrue(FilenameUtils.humanReadableByteCount(2000) == "1,95 Kb")
    }

    @Test
    fun bytes_550646543_Equals_To_525_14_Mb(){
        assertTrue(FilenameUtils.humanReadableByteCount(550646543) == "525,14 Mb")
    }

    @Test
    fun bytes_345355351111_Equals_To_321_64_Gb(){
        assertTrue(FilenameUtils.humanReadableByteCount(345355351111) == "321,64 Gb")
    }

    @Test
    fun bytes_10_Equals_To_10_Bytes(){
        assertTrue(FilenameUtils.humanReadableByteCount(10) == "10 bytes")
    }
}