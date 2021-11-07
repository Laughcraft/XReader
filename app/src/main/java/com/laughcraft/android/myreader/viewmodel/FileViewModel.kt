package com.laughcraft.android.myreader.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.laughcraft.android.myreader.db.dao.FavoritesDao
import com.laughcraft.android.myreader.db.dao.RecentsDao
import com.laughcraft.android.myreader.db.entity.BookmarkEntity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class FileViewModel @Inject constructor(private val exceptionHandler: CoroutineExceptionHandler,
                                             private val favoritesDao: FavoritesDao,
                                             private val recentsDao: RecentsDao): ViewModel(){

    fun renameFile(file: File, newName: String, callback: ((updatedFile: File) -> Unit)? = null){
        if (newName != file.nameWithoutExtension){
            CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                val newFile = renameFileReally(file, newName)
                CoroutineScope(Dispatchers.Main).launch(exceptionHandler) {
                    try {
                        callback?.invoke(newFile)
                    } catch (e: Throwable){
                        Log.e("XReader", "Error in renaming callback: ", e)
                    }
                }
            }
        }
    }

    private fun renameFileReally(file: File, newName: String): File{
        val newFile = File(file.parent, "$newName.${file.extension}")
        file.renameTo(newFile)
        return newFile
    }

    fun renameFiles(files: List<File>, newName: String, callback:((updatedFiles: List<File>)-> Unit)? = null){
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            val newFiles = if (files.size == 1)
                arrayListOf(renameFileReally(files.first(), newName))
            else
                files.mapIndexed{ index, file -> renameFileReally(file, "$newName($index)") }

            CoroutineScope(Dispatchers.Main).launch(exceptionHandler) {
                try {
                    callback?.invoke(newFiles)
                } catch (e: Throwable){
                    Log.e("XReader", "Error in renaming callback: ", e)
                }
            }
        }
    }

    fun addToRecents(file: File){
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            recentsDao.saveFile(file)
            Log.i("XReader.DB", "Recent File: $file")
        }
    }

    fun addBookMark(file: File, chapter: Int = 0, page: Int = 0, comment: String = ""){
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            favoritesDao.saveBookmark(BookmarkEntity(0, file.absolutePath, chapter, page, comment))
            Log.i("XReader.DB", "Favorite File: $file")
        }
    }
}