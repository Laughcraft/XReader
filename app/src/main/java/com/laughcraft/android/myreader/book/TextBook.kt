package com.laughcraft.android.myreader.book

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.laughcraft.android.myreader.book.Book

abstract class TextBook(path:String): Book(path) {
    abstract val chapter: MutableLiveData<String>
    abstract val tableOfContents: MutableLiveData<List<String>>

    open var encoding: String = "UTF-8"
    open var mimeType: String = "text/html"
    open var baseUrl: String = Uri.fromFile(this).toString()

    abstract fun loadChapter(chapterIndex: Int)
}