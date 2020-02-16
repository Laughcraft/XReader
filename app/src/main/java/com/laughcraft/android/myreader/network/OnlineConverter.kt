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

package com.laughcraft.android.myreader.network

import android.net.Uri
import android.util.Log
import com.laughcraft.android.myreader.XReaderApplication
import com.laughcraft.android.myreader.network.gson.GetProgressResponse
import com.laughcraft.android.myreader.network.gson.PostConversionRequest
import com.laughcraft.android.myreader.network.gson.PostConversionResponse
import com.laughcraft.android.myreader.network.gson.PutFileResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.zip.ZipFile

private const val TAG = "OnlineConverter"

class OnlineConverter {
    private val API_KEY = "feb3867dd958e2447e9ba3b75703c185"
    private val api: ConversionApi = XReaderApplication.networkHelper.jsonApi
    
    private val MAX_ATTEMPTS = 40
    
    private lateinit var conversionId: String
    private lateinit var convertedFileUrl: String
    private var attemptsCounter = 0
    
    private lateinit var srcFile: File
    private lateinit var outFile: File
    
    private lateinit var requestBody: RequestBody
    private lateinit var outFormat: String
    
    var onUploadCallback: (() -> Unit)? = null
    var onConvertCallback: (() -> Unit)? = null
    var onSuccessCallback: (() -> Unit)? = null
    
    var onFailureCallback: (() -> Unit)? = null
    
    fun convert(sourceFile: File, outputFormat: String, outputFile: File? = null) {
        
        outFormat = outputFormat
        srcFile = sourceFile
        CoroutineScope(Dispatchers.IO).launch {
            outFile = outputFile ?: createOutputFile(
                    FilenameUtils.removeExtension(sourceFile.absolutePath), outputFormat)
        }
        
        if (!sourceFile.exists()) throw Exception("File does not exist")
        
        val uri = Uri.fromFile(sourceFile)
        
        requestBody = RequestBody.create(null, sourceFile)
        
        val request = PostConversionRequest(API_KEY, "upload", uri.toString(), srcFile.name,
                                            outputFormat)
        
        startConversion(request)
        Log.i(TAG, "Conversion started")
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
    
    private fun startConversion(request: PostConversionRequest) {
        Log.i(TAG, "Trying to Start Conversion")
        api.startConversion(request).enqueue(object : Callback<PostConversionResponse> {
            override fun onResponse(call: Call<PostConversionResponse>, response: Response<PostConversionResponse>) {
                Log.i(TAG, "Start Conversion Response: POST completed with ${response.code()}")
                if (response.code() != 200) {
                    onFailureCallback?.invoke()
                    return
                }
                conversionId = response.body()!!.data!!.id!!
                Log.i(TAG,
                      "Start Conversion: ID = $conversionId, Callback is null?: ${onUploadCallback == null}")
                onUploadCallback?.invoke()
                uploadFile()
            }
            
            override fun onFailure(call: Call<PostConversionResponse>, t: Throwable) {
                Log.i(TAG, "Start Conversion Response: POST completed with FAILURE", t)
                onFailureCallback?.invoke()
            }
        })
    }
    
    private fun getConversionProgress(id: String) {
        Log.i(TAG, "Get Progress: Trying to get URL. Attempt #$attemptsCounter")
        attemptsCounter++
        api.getConversionProgress(id).enqueue(object : Callback<GetProgressResponse> {
            override fun onResponse(call: Call<GetProgressResponse>, response: Response<GetProgressResponse>) {
                Log.i(TAG, "Get Progress Response: GET completed with ${response.code()}")
                if (response.code() != 200) {
                    return
                }
                if (response.body()!!.data!!.step == "finish") {
                    convertedFileUrl = response.body()!!.data!!.output!!.url!!
                    Log.i(TAG, "Get Progress: URL = $convertedFileUrl")
                    onConvertCallback?.invoke()
                    getFile(convertedFileUrl)
                } else {
                    if (attemptsCounter >= MAX_ATTEMPTS) return
                    attemptsCounter++
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1500)
                        getConversionProgress(id)
                    }
                }
            }
            
            override fun onFailure(call: Call<GetProgressResponse>, t: Throwable) {
                Log.i(TAG, "Get Conversion Progress Response: GET completed with FAILURE")
                if (t.message != null) {
                    if (t.message!!.contains("Expected BEGIN_OBJECT but was BEGIN_ARRAY")) {
                        CoroutineScope(Dispatchers.Main).launch {
                            if (attemptsCounter >= MAX_ATTEMPTS) {
                                onFailureCallback?.invoke()
                                return@launch
                            }
                            delay(2000)
                            getConversionProgress(id)
                        }
                    } else {
                        Log.i(TAG, "", t)
                    }
                }
            }
        })
    }
    
    private fun getFile(url: String) {
        Log.i(TAG, "Trying to get converted pdfFile")
        api.getFile(url).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.i(TAG, "Get File: GET completed with code ${response.code()}")
                if (response.code() != 200) {
                    return
                }
                CoroutineScope(Dispatchers.IO).launch {
                    if (convertedFileUrl.endsWith("zip")) {
                        outFile = File("${FilenameUtils.removeExtension(outFile.absolutePath)}.zip")
                    }
                    
                    writeResponseBodyToDisk(response.body()!!, outFile)
                    CoroutineScope(Dispatchers.Main).launch { onSuccessCallback?.invoke() }
                }
                
            }
            
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.i(TAG, "Get File: Failure", t)
                onFailureCallback?.invoke()
            }
        })
    }
    
    private fun writeResponseBodyToDisk(body: ResponseBody, outputFile: File): Boolean {
        Log.i(TAG, "Trying to write it on disk")
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
                    val read = inputStream!!.read(fileReader)
                    
                    if (read == -1) {
                        break
                    }
                    
                    outputStream.write(fileReader, 0, read)
                    
                    fileSizeDownloaded += read.toLong()
                }
                
                outputStream.flush()
                Log.i(TAG, "pdfFile download: $fileSizeDownloaded of $fileSize")
                if (outputFile.absolutePath.endsWith("zip")) {
                    val tempFolder = File(FilenameUtils.removeExtension(outputFile.absolutePath))
                    tempFolder.mkdir()
                    unzipFile(outputFile, tempFolder)
                    FileUtils.deleteQuietly(outputFile)
                }
                return true
            } catch (e: IOException) {
                return false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            return false
        }
        
    }
    
    private fun uploadFile() {
        Log.i(TAG, "Trying to Upload File")
        
        val call = api.uploadFile(conversionId, srcFile.name, requestBody)
        call.enqueue(object : Callback<PutFileResponse> {
            override fun onResponse(call: Call<PutFileResponse>, response: Response<PutFileResponse>) {
                Log.i(TAG, "Upload File: PUT completed with ${response.code()}")
                if (response.code() != 200) {
                    return
                }
                
                CoroutineScope(Dispatchers.Main).launch {
                    getConversionProgress(conversionId)
                }
                
            }
            
            override fun onFailure(call: Call<PutFileResponse>, t: Throwable) {
                Log.i(TAG, "Upload File: Failure")
                
            }
        })
    }
    
    private fun unzipFile(file: File, destinationFolder: File): File {
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(file)
        } catch (e: IOException) {
            Log.e(TAG, "Can't open zip pdfFile", e)
        }
        
        val entries = zipFile!!.entries()
        var counter = 1
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            try {
                var name = "$counter.${FilenameUtils.getExtension(entry.name)}"
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
}
