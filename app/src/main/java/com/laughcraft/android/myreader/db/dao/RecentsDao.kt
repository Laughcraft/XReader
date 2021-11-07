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

package com.laughcraft.android.myreader.db.dao

import androidx.room.*
import com.laughcraft.android.myreader.db.entity.RecentFileEntity
import java.io.File

@Dao
interface RecentsDao {
    
    @Query("SELECT * FROM recents")
    fun getAllRecentFiles(): List<RecentFileEntity>
    
    @Query("SELECT * FROM recents ORDER BY `index` DESC LIMIT :amount")
    fun getLastFiles(amount: Int): List<RecentFileEntity>
    
    @Query("SELECT * FROM recents WHERE `index` = :id")
    fun getFileById(id: Int): RecentFileEntity
    
    @Query("SELECT * FROM recents WHERE path = :path")
    fun getFilesByPath(path: String): List<RecentFileEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFile(recentFileEntity: RecentFileEntity)
    
    @Delete
    fun deleteRecentFile(recentFileEntity: RecentFileEntity)

    fun saveFile(file: File){
        insertFile(RecentFileEntity(0, file.absolutePath))
    }
}
