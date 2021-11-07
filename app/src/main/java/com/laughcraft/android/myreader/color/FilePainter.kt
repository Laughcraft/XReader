package com.laughcraft.android.myreader.color

import android.content.Context
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.const.Extensions
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilePainter @Inject constructor(applicationContext: Context): XPainter(applicationContext) {

    fun getColorForFile(file: File): Int {
        return if (file.isDirectory) getColor(R.color.folder) else getColorForFile(file.extension)
    }

    fun getColorForFile(extension: String): Int{
        return when (extension.lowercase()) {
            "doc" -> getColor(R.color.doc)
            "docx" -> getColor(R.color.docx)
            "xls" -> getColor(R.color.xls)
            "xlsx" -> getColor(R.color.xlsx)
            "djvu" -> getColor(R.color.djvu)
            "pdf" -> getColor(R.color.pdf)
            "epub" -> getColor(R.color.epub)
            "fb2" -> getColor(R.color.fb2)
            "html" -> getColor(R.color.html)
            "txt" -> getColor(R.color.txt)
            in Extensions.images -> getColor(R.color.image)
            else -> getColor(R.color.other)
        }
    }
}