package com.laughcraft.android.myreader.book

import android.content.res.Resources
import android.graphics.Bitmap
import com.github.axet.djvulibre.DjvuLibre
import com.laughcraft.android.myreader.book.Book
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class Djvu(file: File): Book(file.absolutePath) {
    private lateinit var djvuLibre: DjvuLibre
    private lateinit var fileInputStream: FileInputStream

    override var onError: ((Throwable) -> Unit)? = null
    override var title: String = this.name

    var currentPage = 0

    override fun getPagesCount(): Int = djvuLibre.pagesCount

    override fun open() {
        fileInputStream = FileInputStream(this@Djvu)
        djvuLibre = DjvuLibre(fileInputStream.fd)

        DjvuLibre.META_TITLE?.let{
            djvuLibre.getMeta(it)
        }
    }

    fun getPage(page: Int): Bitmap {
        currentPage = page
        return processPage(page)
    }

    private fun processPage(index: Int): Bitmap {
        val dm = Resources.getSystem().displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        djvuLibre.renderPage(bitmap, index, 0, 0, width, height, 0, 0, width, height)
        return bitmap
    }

    override fun close() {
        try {
            fileInputStream.close()
            djvuLibre.close()
        } catch (e: IOException) {
            onError?.invoke(e)
            e.printStackTrace()
        }
    }
}