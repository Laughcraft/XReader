package com.laughcraft.android.myreader.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.laughcraft.android.myreader.di.module.MainModule
import com.laughcraft.android.myreader.net.OnlineConverter
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ConverterViewModel @Inject constructor(private val converter: OnlineConverter,
                                             @Named(MainModule.DOWNLOADS_DIR_REQUEST)
                                             private val resultDir: File,
                                             private val exceptionHandler: CoroutineExceptionHandler): ViewModel() {

    val extension: MutableLiveData<String> = MutableLiveData()
    lateinit var sourceFile: File

    val progress: MutableLiveData<OnlineConverter.ProgressStage> = MutableLiveData(OnlineConverter.ProgressStage.BEFORE)
    var convertedFile: File? = null
    var lastStage: OnlineConverter.ProgressStage? = null

    var internetAccess: MutableLiveData<Boolean> = MutableLiveData(null)

    fun prepare(filepath: String){
        sourceFile = File(filepath)
        converter.onProgressChangedCallback = {
            lastStage = it
            progress.postValue(it)
        }
        checkInternetConnection()
    }

    fun convert(){
        val result = createResultFile()
        converter.convert(sourceFile, extension.value!!, result)
    }

    private fun createResultFile(): File {
        return File(resultDir, "${sourceFile.nameWithoutExtension}.${extension.value!!}").apply {
            createNewFile()
        }
    }

    private fun checkInternetConnection() {
        CoroutineScope(Dispatchers.Main).launch(exceptionHandler) {
            val online = withContext(CoroutineScope(Dispatchers.IO + exceptionHandler).coroutineContext) {
                try {
                    val timeoutMs = 1500
                    val socket = Socket()
                    val socketAddress = InetSocketAddress("8.8.8.8", 53)

                    socket.connect(socketAddress, timeoutMs)
                    socket.close()

                    true
                } catch (e: IOException) {
                    false
                }
            }
            internetAccess.postValue(online)
        }
    }
}