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

package com.laughcraft.android.myreader.book.impl

import android.net.Uri
import com.laughcraft.android.myreader.book.abstr.TextBook
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.SpineReference
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.jsoup.Jsoup
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import java.util.zip.ZipFile

class Epub(parent: String, child: String, private val temporaryFolder: File) : TextBook(parent,
                                                                                        child) {
    override lateinit var title: String
        private set
    override var cover: String? = null
        private set
    override val encoding: String = "UTF-8"
    
    override var tableOfContents: List<String> = listOf()
    
    override var onError: (Throwable) -> Unit = {}
    
    private lateinit var epubBook: Book
    
    override var mimeType: String = "text/html"
    override lateinit var baseUrl: String
    
    private lateinit var tocPairs: List<Pair<File, String>>
    override val chaptersCount: Int get() = tocPairs.size
    
    override suspend fun open(): Job = GlobalScope.launch {
        
        if (!temporaryFolder.exists()) temporaryFolder.mkdirs()
        try {
            epubBook = EpubReader().readEpub(FileInputStream(this@Epub))
        } catch (e: IOException) {
            onBookError(e)
        }
        
        val extensions = arrayOf("xhtml", "html")
        val files = ArrayList(
                FileUtils.listFiles(unzipFile(this@Epub, temporaryFolder), extensions, true))
        
        if (!files.isNullOrEmpty()) baseUrl = Uri.fromFile(files[0]).toString()
        
        val references = mutableListOf<TOCReference>()
        findAllTocReferences(epubBook.tableOfContents.tocReferences, references)
        
        tocPairs = createTOC(epubBook, references, files)
        
        tableOfContents = getTocFromPairs(tocPairs)
        title = epubBook.title
    }
    
    private fun createTOC(epub: Book, references: List<TOCReference>, htmls: List<File>): List<Pair<File, String>> {
        val titleList = mutableListOf<Pair<File, String>>()
        
        val spineHtmls = getSpineReferences(epub)
        
        for (i in spineHtmls!!.indices) {
            val href = spineHtmls[i].resource.href
            val htmlFile = htmls.find { it.absolutePath.endsWith(href) }
            
            var tit: String
            
            val titles = getTitleStringsFromTocReferences(references, href)
            
            if (titles.isEmpty()) {
                tit = FilenameUtils.getBaseName(htmlFile!!.name)
                titleList.add(Pair(htmlFile, tit))
            } else {
                titles.forEach {
                    titleList.add(Pair(htmlFile!!, it))
                }
            }
        }
        
        return titleList
    }
    
    private fun getTocFromPairs(pairs: List<Pair<File, String>>): List<String> {
        val tocTitles = mutableListOf<String>()
        
        pairs.forEach {
            tocTitles.add(it.second)
        }
        return tocTitles
    }
    
    private fun getSpineReferences(epub: Book): List<SpineReference>? {
        if (epub.spine.isEmpty) epub.generateSpineFromTableOfContents()
        
        return epub.spine.spineReferences
    }
    
    override suspend fun getChapter(index: Int): String = extractHtml(tocPairs[index].first)
    
    private fun getTitleStringsFromTocReferences(tocReferences: List<TOCReference>, htmlFilename: String): List<String> {
        val titles: MutableList<String> = mutableListOf()
        
        tocReferences.forEach { reference ->
            if (reference.resource.href == htmlFilename) {
                titles.add(reference.title)
            }
        }
        return titles
    }
    
    private fun findAllTocReferences(source: List<TOCReference>, result: MutableList<TOCReference>) {
        source.forEach {
            result.add(it)
            if (it.children.size > 0) {
                findAllTocReferences(it.children, result)
            }
        }
    }
    
    private fun unzipFile(file: File, destinationFolder: File): File {
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(file)
        } catch (e: IOException) {
            onBookError(e)
        }
        
        val entries = zipFile!!.entries()
        
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            try {
                zipFile.getInputStream(entry).use { stream ->
                    FileUtils.copyInputStreamToFile(stream, File(destinationFolder, entry.name))
                }
            } catch (e: IOException) {
                onBookError(e)
            }
            
        }
        return destinationFolder
    }
    
    private fun extractHtml(file: File): String {
        return try {
            val doc = Jsoup.parse(file, encoding)
            doc.head().appendElement("style").text(
                        "p{" +
                                "   word-break: break-word;" +
                                "   text-align: justify;" +
                                "   text-indent:40px;" +
                                "}" +
                                "img{\n" +
                                "    max-height:100%;\n" +
                                "    max-width:100%;\n" +
                                "    height:auto;\n" +
                                "    width:auto;\n" +
                                "}" +
                                "table { " +
                                "   width:100%;\n" +
                                "   table-layout: fixed;\n" +
                                "   overflow-wrap: break-word;" +
                                "}")
            
            doc.toString()
        } catch (e: IOException) {
            onBookError(e)
            ""
        }
    }
    
    override suspend fun close() {
        FileUtils.deleteQuietly(temporaryFolder)
    }
}


