package com.laughcraft.android.myreader.file.abstr

import java.io.File

interface Sorter {
    enum class SortingType {
        ByName, ByDate, BySize, ByExtension
    }
    
    fun sortBy(type: SortingType, files: List<File>, reversed: Boolean = false): List<File>
}