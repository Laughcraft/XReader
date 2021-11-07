/*
 * Copyright (c) 2019.
 * Created by Vladislav Zraevskij
 *
 * This file is part of XReader.
 *
 *     XReader is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     XReader is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with XReader.  If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>.
 *
 */

package com.laughcraft.android.myreader.net

import android.net.Uri
import android.util.Log
import com.laughcraft.android.myreader.di.module.MainModule
import com.laughcraft.android.myreader.net.gson.GetProgressResponse
import com.laughcraft.android.myreader.net.gson.PostConversionRequest
import com.laughcraft.android.myreader.net.gson.PostConversionResponse
import com.laughcraft.android.myreader.net.gson.PutFileResponse
import kotlinx.coroutines.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class OnlineConverter @Inject constructor(private val api: ConversionApi,
                                          @Named(MainModule.API_KEY_REQUEST) private val apiKey: String,
                                          private val exceptionHandler: CoroutineExceptionHandler) {

    enum class ProgressStage { BEFORE, STARTED, UPLOADING, UPLOADED, CONVERTING, CONVERTED, DOWNLOADING, AFTER }

    private lateinit var conversionId: String
    private lateinit var convertedFileUrl: String
    private var attempts = 0
    
    private lateinit var srcFile: File
    private lateinit var outFile: File
    
    private lateinit var requestBody: RequestBody
    private lateinit var outFormat: String

    var onError: ((t: Throwable)-> Unit)? = null

    var onProgressChangedCallback: ((progressStage: ProgressStage)-> Unit)? = null

    private val okResponse = 200
    private val maxAttempts = 40

    private val tag = "OnlineConverter"

    fun convert(sourceFile: File, outputFormat: String, outputFile: File? = null) {

        outFormat = outputFormat
        srcFile = sourceFile

        outFile = outputFile ?: createOutputFile(sourceFile.nameWithoutExtension, outputFormat)

        if (!sourceFile.exists()) throw Exception("File does not exist")
        
        val uri = Uri.fromFile(sourceFile)
        
        requestBody = RequestBody.create(null, sourceFile)

        val request = PostConversionRequest(apiKey, "upload", uri.toString(), srcFile.name, outputFormat)
        
        startConversion(request)
        Log.i(tag, "Conversion started")
    }
    
    private fun startConversion(request: PostConversionRequest) {
        Log.i(tag, "Trying to Start Conversion")

        onProgressChangedCallback?.invoke(ProgressStage.STARTED)

        api.startConversion(request).enqueue(object : Callback<PostConversionResponse> {
            override fun onResponse(call: Call<PostConversionResponse>, response: Response<PostConversionResponse>) {
                Log.i(tag, "Start Conversion Response: POST completed with ${response.code()}")
                if (response.code() != okResponse) {
                    onError?.invoke(IllegalAccessException("Cannot upload file"))
                    return
                }
                conversionId = response.body()!!.data!!.id!!
                onProgressChangedCallback?.invoke(ProgressStage.UPLOADING)
                uploadFile()
            }
            
            override fun onFailure(call: Call<PostConversionResponse>, t: Throwable) {
                Log.i(tag, "Start Conversion Response: POST completed with FAILURE", t)
                onError?.invoke(t)
            }
        })
    }

    private fun uploadFile() {
        Log.i(tag, "Trying to Upload File")

        val call = api.uploadFile(conversionId, srcFile.name, requestBody)
        call.enqueue(object : Callback<PutFileResponse> {
            override fun onResponse(call: Call<PutFileResponse>, response: Response<PutFileResponse>) {
                Log.i(tag, "Upload File: PUT completed with ${response.code()}")
                if (response.code() != okResponse) {
                    return
                }
                onProgressChangedCallback?.invoke(ProgressStage.UPLOADED)
                CoroutineScope(Dispatchers.Main).launch(exceptionHandler) {
                    getConversionProgress(conversionId)
                }

            }

            override fun onFailure(call: Call<PutFileResponse>, t: Throwable) {
                Log.i(tag, "Upload File: Failure")

            }
        })
    }

    private fun getConversionProgress(id: String) {
        Log.i(tag, "Get Progress: Trying to get URL. Attempt #$attempts")
        attempts++
        onProgressChangedCallback?.invoke(ProgressStage.CONVERTING)
        api.getConversionProgress(id).enqueue(object : Callback<GetProgressResponse> {
            override fun onResponse(call: Call<GetProgressResponse>, response: Response<GetProgressResponse>) {
                Log.i(tag, "Get Progress Response: GET completed with ${response.code()}")
                if (response.code() != okResponse) {
                    return
                }
                if (response.body()!!.data!!.step == "finish") {
                    convertedFileUrl = response.body()!!.data!!.output!!.url!!
                    onProgressChangedCallback?.invoke(ProgressStage.CONVERTED)
                    Log.i(tag, "Get Progress: URL = $convertedFileUrl")
                    downloadConvertedFile(convertedFileUrl)
                } else {
                    if (attempts >= maxAttempts) return
                    attempts++
                    CoroutineScope(Dispatchers.Main).launch(exceptionHandler) {
                        delay(1500)
                        getConversionProgress(id)
                    }
                }
            }
            
            override fun onFailure(call: Call<GetProgressResponse>, t: Throwable) {
                Log.i(tag, "Get Conversion Progress Response: GET completed with FAILURE")
                if (t.message != null) {
                    if (t.message!!.contains("Expected BEGIN_OBJECT but was BEGIN_ARRAY")) {
                        CoroutineScope(Dispatchers.Main).launch(exceptionHandler) {
                            if (attempts >= maxAttempts) {
                                onError?.invoke(IllegalAccessException())
                                return@launch
                            }
                            delay(200)
                            getConversionProgress(id)
                        }
                    } else {
                        Log.i(tag, "", t)
                    }
                }
            }
        })
    }
    
    private fun downloadConvertedFile(url: String) {
        Log.i(tag, "Trying to get converted pdfFile")
        onProgressChangedCallback?.invoke(ProgressStage.DOWNLOADING)
        api.getFile(url).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.i(tag, "Get File: GET completed with code ${response.code()}")
                if (response.code() != okResponse) {
                    return
                }
                CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                    if (convertedFileUrl.endsWith("zip")) {
                        outFile = File("${outFile.nameWithoutExtension}.zip")
                    }

                    writeResponseBodyToDisk(response.body()!!, outFile)
                }
            }
            
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.i(tag, "Get File: Failure", t)
                onError?.invoke(t)
            }
        })
    }
    
    private fun writeResponseBodyToDisk(body: ResponseBody, outputFile: File): Boolean {
        Log.i(tag, "Trying to write it on disk")
        try {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            
            try {
                val fileReader = ByteArray(4096)
                
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                
                inputStream = body.byteStream()
                outputStream = FileOutputStream(outputFile)
                
                while (true) {
                    val read = inputStream.read(fileReader)
                    
                    if (read == -1) {
                        break
                    }
                    
                    outputStream.write(fileReader, 0, read)
                    
                    fileSizeDownloaded += read.toLong()
                }
                
                outputStream.flush()
                Log.i(tag, "pdfFile download: $fileSizeDownloaded of $fileSize")
                if (outputFile.absolutePath.endsWith("zip")) {
                    val tempFolder = File(FilenameUtils.removeExtension(outputFile.absolutePath))
                    tempFolder.mkdir()
                    unzipFile(outputFile, tempFolder)
                    FileUtils.deleteQuietly(outputFile)
                }
                return true
            } catch (e: IOException) {
                Log.e("XReader.Converter", "Error on writing", e)
                return false
            } finally {
                inputStream?.close()
                outputStream?.close()
                onProgressChangedCallback?.invoke(ProgressStage.AFTER)
            }
        } catch (e: IOException) {
            return false
        }
    }
    
    private fun unzipFile(file: File, destinationFolder: File): File {
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(file)
        } catch (e: IOException) {
            Log.e(tag, "Can't open zip pdfFile", e)
        }
        
        val entries = zipFile!!.entries()
        var counter = 1
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            try {
                val name = "$counter.${FilenameUtils.getExtension(entry.name)}"
                counter++
                zipFile.getInputStream(entry).use { stream ->
                    FileUtils.copyInputStreamToFile(stream, File(destinationFolder, name))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            
        }
        return destinationFolder
    }

    private fun createOutputFile(pathWithoutExtension: String, extension: String, depth: Int? = null): File {
        return if (depth == null) {
            val f = File("$pathWithoutExtension.$extension")
            if (f.exists()) createOutputFile(pathWithoutExtension, extension, 1)
            else f
        } else {
            val f = File("$pathWithoutExtension$depth.$extension")
            if (f.exists()) createOutputFile(pathWithoutExtension, extension, depth + 1)
            else f
        }
    }
}
