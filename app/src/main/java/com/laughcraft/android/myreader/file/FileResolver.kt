package com.laughcraft.android.myreader.file

import android.util.Log
import com.laughcraft.android.myreader.const.SuitableFiles
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileResolver @Inject constructor() {

    fun resolve(file: File): SuitableFiles.FileType {
        if (file.isDirectory) return SuitableFiles.FileType.Directory

        val extension = file.extension.lowercase()

        SuitableFiles.suitableExtensions.forEach { type ->
            Log.i("XReader", "Type: ${type.key.name}")
            if (extension in type.value) return type.key
        }

        return SuitableFiles.FileType.Other
    }
}