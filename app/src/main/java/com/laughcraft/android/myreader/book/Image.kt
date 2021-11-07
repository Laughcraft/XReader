package com.laughcraft.android.myreader.book

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.laughcraft.android.myreader.const.SuitableFiles
import com.laughcraft.android.myreader.file.FilenameComparator
import org.apache.commons.io.FileUtils
import java.io.File

class Image(file: File): Book(file.absolutePath) {
    override var onError: ((Throwable) -> Unit)? = null
    override var title: String = parentFile?.name ?: name

    var images: MutableList<File> = arrayListOf()
    var currentPage = 0
    override fun getPagesCount(): Int = images.size

    override fun open() {
        images = FileUtils.listFiles(parentFile!!, SuitableFiles.suitableExtensions[SuitableFiles.FileType.Images], false).toMutableList()
        Log.i("XReader.Images", "Opening ${images.size} files...")
        images.sortWith(FilenameComparator())
        currentPage = images.indexOfFirst { it.absolutePath == this@Image.absolutePath }
    }

    override fun close() {}

    fun getPage(index: Int): Bitmap {
        val f = images[index]
        val p = f.absolutePath
        val b = BitmapFactory.decodeFile(p)
        Log.i("XReader.Images", "Rendering ${images[index]}; Index: $index. Total: ${images.size}. Bitmap: ${b?.height}. File Exists: ${f.exists()}")

        return b ?: Bitmap.createBitmap(0,0,Bitmap.Config.RGB_565)
    }
}