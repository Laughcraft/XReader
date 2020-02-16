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

package com.laughcraft.android.myreader.book

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat.startActivity
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReaderApplication
import com.laughcraft.android.myreader.book.abstr.Book
import com.laughcraft.android.myreader.book.impl.*
import com.laughcraft.android.myreader.database.Bookmark
import com.laughcraft.android.myreader.database.RecentFile
import com.laughcraft.android.myreader.model.Natives
import com.laughcraft.android.myreader.ui.activity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File

class Books(private val context: Context) {
    
    @SuppressLint("DefaultLocale")
    fun getBook(file: File): Book? {
        val extension = FilenameUtils.getExtension(file.name)
        
        val ext = Extensions.checkExtension(extension) ?: throw IllegalArgumentException(
                "Wrong Extension")
        
        val parent = file.parent!!
        val name = file.name
        
        context.filesDir.listFiles()?.forEach { FileUtils.deleteQuietly(it) }
        
        val tempDirectory = File(context.filesDir, name + System.currentTimeMillis())
        tempDirectory.mkdir()
        
        return when (ext.toLowerCase()) {
            "txt" -> Txt(parent, name)
            "docx" -> {
                System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory",
                                   "com.fasterxml.aalto.stax.InputFactoryImpl")
                System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory",
                                   "com.fasterxml.aalto.stax.OutputFactoryImpl")
                System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory",
                                   "com.fasterxml.aalto.stax.EventFactoryImpl")
                
                val fontStream = context.resources.assets.open("times.ttf")
                val fontFile = File(tempDirectory, "font_" + System.currentTimeMillis() + ".ttf")
                fontFile.createNewFile()
                FileUtils.copyInputStreamToFile(fontStream, fontFile)
                
                return Docx(parent, name, tempDirectory, fontFile.absolutePath)
            }
            "fb2" -> Fb2(parent, name, tempDirectory)
            "epub" -> Epub(parent, name, tempDirectory)
            "html" -> Html(parent, name)
            "xhtml" -> Html(parent, name)
            
            "xlsx" -> {
                System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory",
                                   "com.fasterxml.aalto.stax.InputFactoryImpl")
                System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory",
                                   "com.fasterxml.aalto.stax.OutputFactoryImpl")
                System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory",
                                   "com.fasterxml.aalto.stax.EventFactoryImpl")
                
                Xlsx(parent, name)
            }
            
            "pdf" -> Pdf(parent, name)
            "djvu" -> {
                Natives.loadLibraries(context, "djvu", "djvulibrejni")
                Djvu(parent, name)
            }
            
            "jpg" -> Image(parent, name)
            "jpeg" -> Image(parent, name)
            "png" -> Image(parent, name)
            "bmp" -> Image(parent, name)
            
            else -> null
        }
    }
    
    fun openBook(file: File, activityContext: Context) {
        openBook(activityContext, file, 0, 0, 14)
    }
    
    fun openBook(bookmark: Bookmark, activityContext: Context) {
        val file = File(bookmark.filePath)
        val chapter = bookmark.chapter
        val page = bookmark.page
        val fontSize = bookmark.fontSize
        
        openBook(activityContext, file, chapter, page, fontSize)
    }
    
    private fun openBook(activityContext: Context, file: File, chapter: Int, page: Int, fontSize: Int) {
        val fileExtension = FilenameUtils.getExtension(file.name).toLowerCase()
        
        if (Extensions.TextExtensions.find(fileExtension) != null) {
            addNewRecentFileToDatabase(file)
            startActivity(activityContext,
                          TextActivity.getIntent(activityContext, file.parent!!, file.name, chapter,
                                                 page, fontSize), null)
            return
        }
        
        if (Extensions.TableExtensions.find(fileExtension) != null) {
            addNewRecentFileToDatabase(file)
            startActivity(activityContext,
                          TableActivity.getIntent(activityContext, file.parent!!, file.name, page),
                          null)
            return
        }
        
        if (Extensions.DjvuExtensions.find(fileExtension) != null) {
            addNewRecentFileToDatabase(file)
            startActivity(activityContext,
                          DjvuActivity.getIntent(activityContext, file.parent!!, file.name, page),
                          null)
            return
        }
        
        if (Extensions.ImageExtensions.find(fileExtension) != null) {
            addNewRecentFileToDatabase(file)
            startActivity(activityContext,
                          ImageActivity.getIntent(activityContext, file.parent!!, file.name, page),
                          null)
            return
        }
        
        if (Extensions.PdfExtensions.find(fileExtension) != null) {
            addNewRecentFileToDatabase(file)
            startActivity(activityContext,
                          PdfActivity.getIntent(activityContext, file.parent!!, file.name, page),
                          null)
            return
        }
    }
    
    fun getColor(file: File): Int {
        return getColor(FilenameUtils.getExtension(file.name))
    }
    
    fun getColor(extension: String): Int {
        return if (Extensions.checkExtension(extension) != null) Extensions.getColor(extension)
        else R.color.other
    }
    
    private fun addNewRecentFileToDatabase(file: File) {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = XReaderApplication.filesDatabase.recentFilesDao()
            dao.allFiles.forEach {
                if (it.filePath == file.absolutePath) {
                    dao.deleteFile(it)
                }
            }
            
            dao.insertFile(RecentFile(file.absolutePath))
        }
    }
}