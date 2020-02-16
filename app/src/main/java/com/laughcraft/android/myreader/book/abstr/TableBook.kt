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

package com.laughcraft.android.myreader.book.abstr

import org.apache.poi.ss.util.CellRangeAddress
import java.io.File
import java.net.URI

abstract class TableBook : File, Book {
    abstract val sheetCount: Int
    
    override var onError: (Throwable) -> Unit = {}
    
    protected constructor(pathname: String) : super(pathname)
    protected constructor(parent: String, child: String) : super(parent, child)
    
    protected constructor(parent: File, child: String) : super(parent, child)
    
    protected constructor(uri: URI) : super(uri)
    
    abstract fun getSheet(sheet: Int): Array<Array<String>>
    abstract fun getSheetName(sheet: Int): String
    abstract fun getMergedCells(sheet: Int): Array<CellRangeAddress>
    
    abstract fun getSheetNames(): Array<String>
}