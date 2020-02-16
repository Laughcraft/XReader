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

package com.laughcraft.android.myreader.model

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.io.File
import java.io.IOException

private const val TAG = "HtmlLinkHandler"

class HtmlLinkHandler(private val context: Context, uri: String, private val callback: OnLinkProcessedCallback?) {
    
    init {
        handleLinkEvent(uri)
    }
    
    interface OnLinkProcessedCallback {
        fun onInternalLinkProceeded(text: String)
    }
    
    private fun handleLinkEvent(uri: String?) {
        Log.i(TAG, "handleLinkEvent(): $uri")
        if (!uri.isNullOrEmpty()) {
            if (uri.startsWith("pdfFile://")) {
                handleInternalLink(uri)
            } else if (uri.startsWith("http")) {
                handleExternalLink(uri)
            } else if (uri.contains("#")) {
                handleInternalLink(uri)
            }
        }
    }
    
    private fun followInternalLink(uri: String) {
    
    }
    
    private fun handleInternalLink(uriString: String) {
        val index = uriString.indexOf("#")
        val noteId = uriString.substring(index)
        val fileUriString = uriString.substring(0, index)
        
        val uri = Uri.parse(fileUriString)
        
        var doc: Document? = null
        try {
            doc = Jsoup.parse(File(uri.path!!), "UTF-8")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        
        var note = doc!!.select(noteId).select("p")
        //val child = note.child(0)
        //if (child != null) note = child
        
        callback!!.onInternalLinkProceeded(note.text())
    }
    
    private fun handleExternalLink(uri: String) {
        val parsedUri = Uri.parse(uri)
        val intent = Intent(Intent.ACTION_VIEW, parsedUri)
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Log.w(TAG, "No activity found for URI: $uri")
        }
    }
    
}
