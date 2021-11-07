package com.laughcraft.android.myreader.book

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.io.File

class Html(file: File): TextBook(file.absolutePath) {
    override var onError: ((Throwable) -> Unit)? = null
    override var title: String = name

    override fun getPagesCount(): Int = 1

    override var mimeType: String = "text/plain"

    override val chapter: MutableLiveData<String> = MutableLiveData()
    override val tableOfContents: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())

    override fun loadChapter(chapterIndex: Int) {
        chapter.postValue(Jsoup.parse(this@Html, null).outerHtml())
    }

    override fun open() {  loadChapter(0) }
    override fun close() {}
}