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

import java.text.DecimalFormat

object FilenameUtils {
    
    fun humanReadableByteCount(bytes: Long): String {
        
        val hrSize: String?
        
        val k = bytes / 1024.0
        val m = bytes / 1024.0 / 1024.0
        val g = bytes / 1024.0 / 1024.0 / 1024.0
        val t = bytes / 1024.0 / 1024.0 / 1024.0 / 1024.0
        
        val dec = DecimalFormat("0.00")
        
        hrSize = when {
            t > 1 -> dec.format(t).plus(" Tb")
            g > 1 -> dec.format(g).plus(" Gb")
            m > 1 -> dec.format(m).plus(" Mb")
            k > 1 -> dec.format(k).plus(" Kb")
            else -> "$bytes bytes"
        }
        
        return hrSize
    }
}
