package com.laughcraft.android.myreader.viewmodel

import android.os.StatFs
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import com.laughcraft.android.myreader.const.Extensions
import com.laughcraft.android.myreader.db.dao.FavoritesDao
import com.laughcraft.android.myreader.db.dao.RecentsDao
import com.laughcraft.android.myreader.di.module.MainModule
import com.laughcraft.android.myreader.file.abstr.Explorer
import com.laughcraft.android.myreader.file.abstr.Searcher
import com.laughcraft.android.myreader.file.abstr.Sorter
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ExplorerViewModel @Inject constructor(private val explorer: Explorer,
                                            private val searcher: Searcher,
                                            private val sorter: Sorter,
                                            @Named(MainModule.DOWNLOADS_DIR_REQUEST) private val downloadsDir: File,
                                            private val exceptionHandler: CoroutineExceptionHandler,
                                            favoritesDao: FavoritesDao,
                                            recentsDao: RecentsDao): FileViewModel(exceptionHandler, favoritesDao, recentsDao) {
    val explorerFiles: MutableLiveData<List<File>> = MutableLiveData(arrayListOf())

    val books: MutableLiveData<List<File>> = MutableLiveData(arrayListOf())
    val documents: MutableLiveData<List<File>> = MutableLiveData(arrayListOf())
    val images: MutableLiveData<List<File>> = MutableLiveData(arrayListOf())
    val audio: MutableLiveData<List<File>> = MutableLiveData(arrayListOf())
    val video: MutableLiveData<List<File>> = MutableLiveData(arrayListOf())
    val archives: MutableLiveData<List<File>> = MutableLiveData(arrayListOf())

    val downloads: MutableLiveData<List<File>> = MutableLiveData(arrayListOf())
    val newFiles: MutableLiveData<List<File>> = MutableLiveData(null)
    val searchResults: MutableLiveData<List<File>> = MutableLiveData(arrayListOf())

    var newFilesBuffer: SortedMap<Long, File> = sortedMapOf({ t, t2 -> (t/1000 - t2/1000).toInt() })

    private var searchJob: Job? = null
    var searchQuery = ""

    var booksSpace = 0f
    var documentsSpace = 0f
    var audioSpace = 0f
    var videoSpace = 0f
    var imagesSpace = 0f
    var archivesSpace = 0f
    var emptySpace = 0f
    var otherSpace = 0f

    var inited = false
    private var job: Job? = null

    fun collectFiles(onSuccess: ()-> Unit){
        when {
            inited -> {
                onSuccess.invoke()
                return
            }
            job?.isActive == true -> return
        }

        job = CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            val time = System.currentTimeMillis()

            val files = searcher.collectAllFiles(explorer.homeDirectory)

            newFilesBuffer.putAll(files.take(10).associateBy { it.lastModified() })

            val audio = mutableListOf<File>()
            val video = mutableListOf<File>()
            val images = mutableListOf<File>()
            val documents = mutableListOf<File>()
            val books = mutableListOf<File>()
            val archives = mutableListOf<File>()

            var dsp = 0L
            var audioSp = 0L
            var vsp = 0L
            var isp = 0L
            var bsp = 0L
            var archSp = 0L

            if (files.isEmpty()) return@launch

            files.forEach { file ->
                checkNewFile(file)
                when (file.extension.lowercase()) {
                    in Extensions.documents -> {
                        dsp += file.length()
                        documents.add(file)
                    }
                    in Extensions.audio -> {
                        audioSp += file.length()
                        audio.add(file)
                    }
                    in Extensions.video -> {
                        vsp += file.length()
                        video.add(file)
                    }
                    in Extensions.images -> {
                        isp += file.length()
                        images.add(file)
                    }
                    in Extensions.books -> {
                        bsp += file.length()
                        books.add(file)
                    }
                    in Extensions.archives -> {
                        archSp += file.length()
                        archives.add(file)
                    }
                }
            }

            this@ExplorerViewModel.books.postValue(sorter.sortBy(Sorter.SortingType.ByName, books))
            this@ExplorerViewModel.audio.postValue(sorter.sortBy(Sorter.SortingType.ByName, audio))
            this@ExplorerViewModel.video.postValue(sorter.sortBy(Sorter.SortingType.ByName, video))
            this@ExplorerViewModel.images.postValue(sorter.sortBy(Sorter.SortingType.ByName, images))
            this@ExplorerViewModel.archives.postValue(sorter.sortBy(Sorter.SortingType.ByName, archives))
            this@ExplorerViewModel.documents.postValue(sorter.sortBy(Sorter.SortingType.ByName, documents))

            newFiles.postValue(newFilesBuffer.values.toList().sortedBy { it.lastModified() })

            val fs = StatFs(explorer.homeDirectory.absolutePath)
            val totalSpace = (fs.totalBytes / 1024).toDouble()

            emptySpace = (fs.availableBytes / 1024 / totalSpace).toFloat() * 100

            archivesSpace = (archSp / 1024 / totalSpace).toFloat() * 100
            booksSpace = (bsp / 1024 /totalSpace).toFloat() * 100
            imagesSpace = (isp / 1024 /totalSpace).toFloat() * 100
            audioSpace = (audioSp / 1024 /totalSpace).toFloat() * 100
            videoSpace = (vsp / 1024 /totalSpace).toFloat() * 100
            documentsSpace = (dsp / 1024 /totalSpace).toFloat() * 100

            otherSpace = 100 - emptySpace - archivesSpace - booksSpace - imagesSpace - audioSpace - videoSpace - documentsSpace

            Log.i("XReader", "Shares: Empty Space: $emptySpace. Video: $videoSpace. Images: $imagesSpace")

            val dlds = explorer.openDirectory(downloadsDir)
            downloads.postValue(sorter.sortBy(Sorter.SortingType.ByName, dlds))

            val time2 = System.currentTimeMillis() - time
            Log.i("XReader", "Init Time: ${time2/1000} sec")
            inited = true
            onSuccess.invoke()
        }
    }

    fun openDirectory(path: String = explorer.homeDirectory.absolutePath){
        explorerFiles.postValue(sorter.sortBy(Sorter.SortingType.ByName, explorer.openDirectory(path)))
    }

    fun back(): File? {
        val dir = explorer.currentDirectory.parentFile
        explorerFiles.postValue(sorter.sortBy(Sorter.SortingType.ByName, explorer.back()))
        return dir
    }

    fun deleteFiles(files: Collection<File>){
        files.forEach { try { it.delete() } catch (se: SecurityException){
                Log.e("XReader", "Cannot delete file ${it.absolutePath}", se)
            }
        }
    }

    fun search(query: String, directory: File = explorer.homeDirectory){
        searchJob?.cancel()
        searchJob = CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            searchQuery = query
            searchResults.postValue(searcher.setName(query).search(directory))
        }
    }

    private fun checkNewFile(file: File){
        if (newFilesBuffer.size < 10){
            newFilesBuffer[file.lastModified()] = file
        } else {
            newFilesBuffer[file.lastModified()] = file
            newFilesBuffer.remove(newFilesBuffer.firstKey())
        }
    }

}