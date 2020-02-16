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

package com.laughcraft.android.myreader.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoriteFilesDao {
    @get:Query("SELECT * FROM favorite_files") val allFiles: List<Bookmark>
    
    @Query("SELECT * FROM favorite_files WHERE `index` = :id")
    fun getFileById(id: Int): Bookmark
    
    @Query("SELECT * FROM favorite_files WHERE path = :path")
    fun getFileBookmarks(path: String): List<Bookmark>
    
    @Insert
    fun insertFile(bookmark: Bookmark)
    
    @Delete
    fun deleteFile(bookmark: Bookmark)
}
