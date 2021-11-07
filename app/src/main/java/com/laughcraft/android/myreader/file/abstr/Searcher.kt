package com.laughcraft.android.myreader.file.abstr

import java.io.File

interface Searcher {
    fun search(directory: File): List<File>
    fun collectAllFiles(directory: File): List<File>
    
    fun setName(name: String): Searcher
    fun setMinSize(size: Long): Searcher
    fun setMaxSize(size: Long): Searcher
    fun setExtensions(vararg extensions: String): Searcher
    fun clearQuery(): Searcher
}