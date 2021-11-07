package com.laughcraft.android.myreader.file

import com.laughcraft.android.myreader.file.abstr.Sorter
import com.laughcraft.android.myreader.file.abstr.Sorter.SortingType
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OmegaSorter @Inject constructor(private val nameComparator: FilenameComparator): Sorter {
    
    override fun sortBy(type: SortingType, files: List<File>, reversed: Boolean): List<File> {
        return putDirectoriesToTheTop(when (type) {
            SortingType.ByName -> sortByName(files, reversed)
            SortingType.ByDate -> sortByDate(files, reversed)
            SortingType.BySize -> sortBySize(files, reversed)
            SortingType.ByExtension -> sortByExtension(files, reversed)
        })
    }
    
    private fun sortByName(files: List<File>, reversed: Boolean): List<File> {
        return if (reversed) files.sortedWith(nameComparator).reversed() else files.sortedWith(nameComparator)
    }
    
    private fun sortByDate(files: List<File>, reversed: Boolean): List<File> {
        return if (reversed) files.sortedByDescending { it.lastModified() } else files.sortedBy { it.lastModified() }
    }
    
    private fun sortByExtension(files: List<File>, reversed: Boolean): List<File> {
        return if (reversed)
            files.sortedByDescending { if (it.isDirectory) "" else it.extension }
        else
            files.sortedBy { if (it.isDirectory) "" else it.extension }
    }
    
    private fun sortBySize(files: List<File>, reversed: Boolean): List<File> {
        return if (reversed) files.sortedByDescending { it.length() } else files.sortedBy { it.length() }
    }
    
    private fun putDirectoriesToTheTop(files: List<File>): List<File> {
        return files.sortedWith { file1, file2 -> when {
                file1.isDirectory && !file2.isDirectory -> -1
                file2.isDirectory && !file1.isDirectory -> 1
                else -> 0
            }
        }
    }
}