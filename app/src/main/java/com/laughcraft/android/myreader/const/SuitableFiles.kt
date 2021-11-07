package com.laughcraft.android.myreader.const

import java.util.*

object SuitableFiles {
    enum class FileType { Images, Pdfs, Tables, Texts, Djvu, Directory, Other }

    private val images = arrayOf("png", "jpg", "jpeg", "bmp", "PNG", "JPG", "JPEG", "BMP")
    private val pdfs = arrayOf("pdf", "docx")
    private val tables = arrayOf("xlsx")
    private val texts = arrayOf("txt", "fb2", "html", "xhtml", "epub")
    private val djvu = arrayOf("djvu")

    val suitableExtensions = EnumMap<FileType, Array<String>>(FileType::class.java).apply {
        put(FileType.Images, images)
        put(FileType.Pdfs, pdfs)
        put(FileType.Tables, tables)
        put(FileType.Texts, texts)
        put(FileType.Djvu, djvu)
    }
}