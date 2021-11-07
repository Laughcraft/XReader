package com.laughcraft.android.myreader.book

import java.io.File

class Pdf(file: File): Book(file.absolutePath) {
    override var onError: ((Throwable) -> Unit)? = null
    override var title: String = nameWithoutExtension

    override fun getPagesCount(): Int = 1
    override fun open() { }
    override fun close() {}
}