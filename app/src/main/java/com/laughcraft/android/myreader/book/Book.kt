package com.laughcraft.android.myreader.book

import java.io.File

abstract class Book(path: String): File(path) {
    abstract var onError: ((Throwable) -> Unit)?
    abstract var title: String

    abstract fun getPagesCount(): Int

    abstract fun open()
    abstract fun close()
}