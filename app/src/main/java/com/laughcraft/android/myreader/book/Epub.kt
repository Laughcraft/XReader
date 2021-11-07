package com.laughcraft.android.myreader.book

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import nl.siegmann.epublib.domain.SpineReference
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.jsoup.Jsoup
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.ArrayList
import java.util.zip.ZipFile

class Epub(file: File, private val tempDir: File): TextBook(file.absolutePath){
    override var onError: ((Throwable) -> Unit)? = null
    override var title: String = ""

    var cover: String? = null

    override var tableOfContents: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())
    override val chapter: MutableLiveData<String> = MutableLiveData("")

    private lateinit var epubBook: nl.siegmann.epublib.domain.Book

    private lateinit var tocPairs: List<Pair<File, String>>

    override var mimeType: String = "text/html"
    override var baseUrl: String = super.baseUrl

    override fun getPagesCount(): Int = -1

    override fun open() {
        if (!tempDir.exists()) tempDir.mkdirs()
        try {
            epubBook = EpubReader().readEpub(FileInputStream(this@Epub))
        } catch (e: IOException) { onError?.invoke(e) }

        val extensions = arrayOf("xhtml", "html")
        val files = ArrayList(FileUtils.listFiles(unzipFile(this@Epub, tempDir), extensions, true))

        if (!files.isNullOrEmpty()) baseUrl = Uri.fromFile(files[0]).toString()

        val references = mutableListOf<TOCReference>()
        findAllTocReferences(epubBook.tableOfContents.tocReferences, references)

        tocPairs = createTOC(epubBook, references, files)

        tableOfContents.value = getTocFromPairs(tocPairs)
        title = epubBook.title
    }

    override fun close() {
        try { tempDir.deleteRecursively() } catch (e: Throwable){ onError?.invoke(e) }
    }

    private fun createTOC(epub: nl.siegmann.epublib.domain.Book, references: List<TOCReference>, htmls: List<File>): List<Pair<File, String>> {
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

    override fun loadChapter(chapterIndex: Int) {
        chapter.postValue(extractHtml(tocPairs[chapterIndex].first))
    }

    private fun getSpineReferences(epub: nl.siegmann.epublib.domain.Book): List<SpineReference>? {
        if (epub.spine.isEmpty) epub.generateSpineFromTableOfContents()

        return epub.spine.spineReferences
    }

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
        try { zipFile = ZipFile(file) } catch (e: IOException) { onError?.invoke(e) }

        val entries = zipFile!!.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            try {
                zipFile.getInputStream(entry).use { stream ->
                    FileUtils.copyInputStreamToFile(stream, File(destinationFolder, entry.name))
                }
            } catch (e: IOException) { onError?.invoke(e) }

        }
        return destinationFolder
    }

    private fun extractHtml(file: File): String {
        return try {
            baseUrl = Uri.fromFile(file).toString()
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
            onError?.invoke(e)
            "Error!"
        }
    }
}