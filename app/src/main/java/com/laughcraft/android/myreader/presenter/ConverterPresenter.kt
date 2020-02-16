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

package com.laughcraft.android.myreader.presenter

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.contract.ViewConverter
import com.laughcraft.android.myreader.network.OnlineConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

@InjectViewState
class ConverterPresenter(val file: File) : MvpPresenter<ViewConverter>() {
    private val converter = OnlineConverter()
    
    fun onViewCreated() {
        isOnline()
    }
    
    fun startConversion(sourceFile: File, outputFormat: String, outputFile: File?) {
        viewState.updateProgress(10, R.string.converter_uploading)
        converter.convert(sourceFile, outputFormat, outputFile)
        converter.onUploadCallback = {
            Log.i("SSConverter", "Uploaded")
            viewState.updateProgress(30, R.string.converter_converting)
        }
        converter.onConvertCallback = {
            viewState.updateProgress(50, R.string.converter_downloading)
        }
        converter.onSuccessCallback = { viewState.updateProgress(100, R.string.converter_success) }
    }
    
    private fun isOnline() {
        CoroutineScope(Dispatchers.Main).launch {
            val online = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
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
            if (!online) {
                viewState.updateProgress(-1, R.string.converter_internet_is_unavailable)
            }
        }
    }
    
    fun onConversionButtonClick() {
        viewState.showWarningDialog()
    }
}
