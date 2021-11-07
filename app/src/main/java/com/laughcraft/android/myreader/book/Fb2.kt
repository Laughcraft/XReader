package com.laughcraft.android.myreader.book

import androidx.lifecycle.MutableLiveData
import com.kursx.parser.fb2.FictionBook
import kotlinx.coroutines.*
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
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class Fb2(file: File, private val tempDir: File): TextBook(file.absolutePath) {
    override lateinit var title: String
    val chaptersCount: Int get() = tableOfContents.value?.size ?: 0

    override val chapter: MutableLiveData<String> = MutableLiveData()
    override val tableOfContents: MutableLiveData<List<String>> = MutableLiveData()

    var cover: String = ""

    override var onError: ((Throwable) -> Unit)? = null

    private lateinit var chapters: MutableList<String?>
    private lateinit var fictionBook: FictionBook
    private lateinit var sections: NodeList
    override var mimeType: String = "text/html"
    private lateinit var temp: File

    override fun getPagesCount(): Int = -1

    override fun close() {
        tempDir.deleteRecursively()
    }

    override fun open() {
        if (!tempDir.exists()) tempDir.mkdirs()

        temp = File(tempDir, "$nameWithoutExtension.html")
        FileUtils.copyFile(this@Fb2, temp)

        try {
            fictionBook = FictionBook(this@Fb2)
            encoding = fictionBook.encoding
            sections = getSections()
            tableOfContents.value = fillTableOfContents(sections)

            title = fictionBook.title

            chapters = arrayOfNulls<String>(tableOfContents.value?.size ?: 0).toMutableList()

        } catch (e: ParserConfigurationException) {
            onError?.invoke(e)
        } catch (e: IOException) {
            onError?.invoke(e)
        } catch (e: SAXException) {
            onError?.invoke(e)
        }
    }

    override fun loadChapter(chapterIndex: Int){
        if (chapters[chapterIndex] == null) {
            chapters[chapterIndex] = getFormattedChapter(fictionBook, sections, chapterIndex)
        }
        chapter.postValue(wrapInHtml(tableOfContents.value!![chapterIndex], chapters[chapterIndex]!!))
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
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            t.transform(DOMSource(node), StreamResult(sw))
        } catch (te: TransformerException) {
            onError?.invoke(te)
        }

        return sw.toString()
    }

    private fun getChapters(fictionBook: FictionBook, sections: NodeList): MutableList<String> {
        val chapters = mutableListOf<String>()
        for (i in tableOfContents.value!!.indices) {
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