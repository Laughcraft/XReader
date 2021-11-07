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

import com.laughcraft.android.myreader.di.HomePath
import com.laughcraft.android.myreader.file.abstr.Explorer
import java.io.File
import java.text.DecimalFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OmegaExplorer @Inject constructor(@HomePath private val homeDirectoryPath: String): Explorer {
    override val homeDirectory: File = File(homeDirectoryPath)
    
    override var currentDirectory: File = homeDirectory
        private set
    var currentDirectoryFiles: List<File> = updateDirectoryFiles(homeDirectory)
        private set
    
    override fun openDirectory(destinationFolderPath: String): List<File> {
        return openDirectory(File(destinationFolderPath))
    }
    
    override fun openDirectory(destinationFolder: File): List<File> {
        return updateDirectoryFiles(destinationFolder)
    }
    
    override fun back(): List<File> {
        return updateDirectoryFiles(currentDirectory.parentFile ?: homeDirectory)
    }
    
    override fun isItHome(): Boolean = currentDirectory.absolutePath == homeDirectoryPath
    
    override fun goHome(): List<File> {
        return updateDirectoryFiles(homeDirectory)
    }
    
    private fun updateDirectoryFiles(directory: File): List<File>{
        this.currentDirectory = directory
        currentDirectoryFiles = directory.listFiles()?.toList() ?: arrayListOf()
        return currentDirectoryFiles
    }
    
    fun humanReadableByteCount(bytes: Long): String {
        
        val hrSize: String?
        
        val k:Double = bytes / 1024.0
        val m:Double = bytes / 1024.0 / 1024
        val g:Double = bytes / 1024.0 / 1024 / 1024
        val t:Double = bytes / 1024.0 / 1024 / 1024 / 1024
        
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
