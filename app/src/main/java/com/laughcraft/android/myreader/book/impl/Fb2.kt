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
import android.util.Log
import com.kursx.parser.fb2.FictionBook
import com.laughcraft.android.myreader.book.abstr.TextBook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class Fb2(private val folder: String, private val fileName: String, private val temporaryFolder: File) : TextBook(
        folder, fileName) {
    private val TAG = "Fb2"
    
    override lateinit var title: String
    override val chaptersCount: Int get() = tableOfContents.size
    override lateinit var encoding: String
    override lateinit var tableOfContents: List<String>
    override lateinit var cover: String
    
    override var onError: (Throwable) -> Unit = {}
    
    private lateinit var chapters: MutableList<String?>
    private lateinit var fictionBook: FictionBook
    private lateinit var sections: NodeList
    override val mimeType: String get() = "text/html"
    override val baseUrl: String get() = Uri.fromFile(this).toString()
    
    private lateinit var temp: File
    
    override suspend fun open(): Job = CoroutineScope(Dispatchers.IO).launch {
        if (!temporaryFolder.exists()) temporaryFolder.mkdirs()
        
        temp = File(temporaryFolder, FilenameUtils.removeExtension(fileName) + ".html")
        FileUtils.copyFile(this@Fb2, temp)
        
        try {
            fictionBook = FictionBook(this@Fb2)
            encoding = fictionBook.encoding
            sections = getSections()
            tableOfContents = fillTableOfContents(sections)
            
            title = fictionBook.title
            
            chapters = arrayOfNulls<String>(tableOfContents.size).toMutableList()
            
        } catch (e: ParserConfigurationException) {
            Log.e(TAG, "Error from constructor", e)
        } catch (e: IOException) {
            Log.e(TAG, "Error from constructor", e)
        } catch (e: SAXException) {
            Log.e(TAG, "Error from constructor", e)
        }
    }
    
    override suspend fun close() {
        FileUtils.deleteQuietly(temporaryFolder)
    }
    
    override suspend fun getChapter(index: Int): String {
        if (chapters[index] == null) {
            chapters[index] = getFormattedChapter(fictionBook, sections, index)
        }
        return wrapInHtml(tableOfContents[index], chapters[index]!!)
        
    }
    
    private fun fillTableOfContents(sections: NodeList): List<String> {
        val tableOfContents = mutableListOf<String>()
        for (i in 0 until sections.length) {
            val sectionTitles = (sections.item(i) as org.w3c.dom.Element).getElementsByTagName(
                    "title")
            
            if (sectionTitles.length == 1) {
                val paragraph = (sectionTitles.item(0) as org.w3c.dom.Element).getElementsByTagName(
                        "p").item(0)
                tableOfContents.add(paragraph.textContent)
            } else {
                tableOfContents.add("* * *")
            }
        }
        
        return tableOfContents
    }
    
    private fun getSections(file: File = this): NodeList {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(file)
        doc.documentElement.normalize()
        
        val nodes = doc.getElementsByTagName("body")
        val body = nodes.item(0) as org.w3c.dom.Element
        
        return body.getElementsByTagName("section")
    }
    
    private fun getFormattedChapter(fictionBook: FictionBook, sections: NodeList, index: Int): String {
        
        val thisSection = sections.item(index)
        
        val chapter = StringBuilder()
        for (i in 0 until thisSection.childNodes.length) {
            var string = nodeToString(thisSection.childNodes.item(i))
            
            string = string.replace("emphasis", "em")
            
            Regex("<image l:href=\".*\"").findAll(string).forEach {
                val anchorStartIndex = it.value.indexOfFirst { a -> a == '#' } + 1
                val imgName = it.value.substring(anchorStartIndex, it.value.length - 1)
                
                val img = fictionBook.binaries[imgName]!!.binary
                val extension = FilenameUtils.getExtension(imgName)
                string = string.replace(it.value, "<img src=\"data:image/$extension;base64, $img\"")
            }
            
            string = string.replace("l:href", "href")
            
            val matcher = Regex("<a href=\"#.*/a>")
            matcher.findAll(string).forEach {
                val ind = it.value.indexOfFirst { a -> a == '#' }
                val anchor = it.value.substring(ind, it.value.length)
                val beginning = it.value.substring(0, ind)
                
                val newLink = "${beginning}pdfFile://${temp.absolutePath}$anchor"
                
                string = string.replace(it.value, newLink)
            }
            
            chapter.append(string)
        }
        
        return chapter.toString()
    }
    
    private fun nodeToString(node: Node): String {
        val sw = StringWriter()
        try {
            val t = TransformerFactory.newInstance().newTransformer()
            t.setOutputProperty(OMIT_XML_DECLARATION, "yes")
            t.transform(DOMSource(node), StreamResult(sw))
        } catch (te: TransformerException) {
            Log.e(TAG, "nodeToString Transformer Exception")
        }
        
        return sw.toString()
    }
    
    private fun getChapters(fictionBook: FictionBook, sections: NodeList): MutableList<String> {
        val chapters = mutableListOf<String>()
        for (i in tableOfContents.indices) {
            chapters.add(getFormattedChapter(fictionBook, sections, i))
        }
        return chapters
    }
    
    private fun wrapInHtml(title: String, body: String): String {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "p{" +
                "   word-break: break-word;" +
                "   text-align: justify;" +
                "   text-indent:40px;" + "}" +
                "img{\n" +
                "    max-height:100%;\n" +
                "    max-width:100%;\n" +
                "    height:auto;\n" +
                "    width:auto;\n" + "}" +
                "table { " +
                "   width:100%;\n" +
                "   table-layout: fixed;\n" +
                "   overflow-wrap: break-word;" +
                "}" + "</style>\n" +
                "</head>\n\n" +
                "<body>\n" +
                "<h1>$title</h1>\n" +
                body +
                "</body>\n" +
                "</html>"
    }
}
