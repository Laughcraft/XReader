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
import org.junit.BeforeClass
import org.junit.Test

class FilenameComparatorTest {
    companion object{
        lateinit var comparator: FilenameComparator

        @BeforeClass
        @JvmStatic
        fun init(){
            comparator = FilenameComparator()
        }
    }

    val a = "AbgfA.jpg"
    val b = "Zg54y.jpg"
    val c = "aa6ya.jpg"
    val d = "1gtrw.jpg"
    val e = "934tf.jpg"
    val f = "ch_4.jpg"
    val g = "__04g.jpg"
    val h = "  4gr.jpg"
    val i = "     .jpg"

    @Test
    fun compareAB() {
        assertTrue(comparator.compare(a, b) < 0)
    }
    @Test
    fun compareAC() {
        assertTrue(comparator.compare(a, c) > 0)
    }
    @Test
    fun compareAD() {
        assertTrue(comparator.compare(a, d) > 0)
    }
    @Test
    fun compareBE() {
        assertTrue(comparator.compare(b, e) > 0)
    }
    @Test
    fun compareAF() {
        assertTrue(comparator.compare(a, f) < 0)
    }
    @Test
    fun compareAG() {
        assertTrue(comparator.compare(a, g) > 0)
    }
    @Test
    fun compareAH() {
        assertTrue(comparator.compare(a, h) > 0)
    }
    @Test
    fun compareAI() {
        assertTrue(comparator.compare(a, i) > 0)
    }
    @Test
    fun compareBF() {
        assertTrue(comparator.compare(b, f) > 0)
    }
    @Test
    fun compareHI() {
        assertTrue(comparator.compare(h, i) < 0)
    }
    @Test
    fun compareEG() {
        assertTrue(comparator.compare(e, g) < 0)
    }
}