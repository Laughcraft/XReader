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

package com.laughcraft.android.myreader.file

import android.util.Log
import java.io.File
import java.math.BigInteger
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class FilenameComparator @Inject constructor(): Comparator<File> {
    private val pattern = Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")
    
    override fun compare(o1: File?, o2: File?): Int {
        
        when {
            o1 == null && o2 == null -> return 0
            o1 == null && o2 != null -> return -1
            o1 != null && o2 == null -> return 1
        }
    
        val first = o1!!.name.lowercase()
        val second = o2!!.name.lowercase()
    
        if (first.startsWith(".") || second.startsWith(".")) {
            return first.compareTo(second)
        }
    
        // Splitting both input strings by the above patterns
        val split1 = pattern.split(first)
        val split2 = pattern.split(second)
        for (i in 0 until min(split1.size, split2.size)) {
            val c1 = split1[i][0]
            val c2 = split2[i][0]
            var cmp = 0
        
            // If both segments start with a digit, sort them numerically using
            // BigInteger to stay safe
        
            if (c1 in '0'..'9' && c2.code >= 0 && c2 <= '9') {
                try {
                    cmp = BigInteger(split1[i]).compareTo(BigInteger(split2[i]))
                } catch (e: NumberFormatException) {
                    Log.e(this::class.simpleName, "Wrong filename", e)
                }
            }
        
        
            // If we haven't sorted numerically before, or if numeric sorting yielded
            // equality (e.g 007 and 7) then sort lexicographically
            if (cmp == 0) cmp = split1[i].compareTo(split2[i])
        
            // Abort once some prefix has unequal ordering
            if (cmp != 0) return cmp
        }
    
        // If we reach this, then both strings have equally ordered prefixes, but
        // maybe one string is longer than the other (i.e. has more segments)
        return split1.size - split2.size
    }
}
