package com.laughcraft.android.myreader.book

import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class Txt(file: File): TextBook(file.absolutePath) {
    override var onError: ((Throwable) -> Unit)? = null
    override var title: String = nameWithoutExtension
    override fun getPagesCount(): Int = 1

    override val chapter: MutableLiveData<String> = MutableLiveData("")
    override val tableOfContents: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())

    override var mimeType: String = "text/plain"

    override fun open() {
        loadChapter(0)
    }

    override fun loadChapter(chapterIndex: Int) {
        Log.i("XReader.TextFragment", "Loading text of $name...")
        try {
            val f = FileInputStream(this@Txt)
            val b = BufferedReader(InputStreamReader(f))
            val text = b.readText()
            Log.i("XReader.TextFragment", "Text of $name is $text")
            chapter.postValue(text)
            b.close()
        } catch (t: Throwable) {
            onError?.invoke(t)
        }
    }

    override fun close() {}
}