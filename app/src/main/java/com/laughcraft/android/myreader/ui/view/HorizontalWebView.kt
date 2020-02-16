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

package com.laughcraft.android.myreader.ui.view

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import com.laughcraft.android.myreader.model.HtmlLinkHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.round

private const val NIGHT_MODE_KEY = "night_mode_key"
private const val PAGE_KEY = "page_key"
private const val STATE_KEY = "state_key"
private const val PERCENT_KEY = "percent_key"

private const val BASE_URL_KEY = "base_url_key"
private const val DATA_KEY = "data_key"
private const val MIME_TYPE_KEY = "mime_type_key"
private const val ENCODING_KEY = "encoding_key"
private const val HISTORY_URL_KEY = "history_url_key"

private const val FONT_SIZE_KEY = "font_size_key"


class HorizontalWebView(context: Context, attrs: AttributeSet) : WebView(context, attrs) {
    
    private var recentlyLoaded = false
    
    private var currentX = 0
    
    var leftOverScrollCallback: (() -> Unit)? = null
    var rightOverScrollCallback: (() -> Unit)? = null
    
    var fontSize = 16
        set(fontSize) {
            field = fontSize
            settings.defaultFontSize = fontSize
            loadUrl("javascript:alert(initialize())")
            if (currentX >= computeHorizontalScrollRange()) {
                currentX = computeHorizontalScrollRange() - width
            }
            
            currentX = computeHorizontalScrollRange() - width
            if (currentX < 0) currentX = 0
            currentX = 0
            page = 0
            scrollTo(currentX, 0)
        }
    
    private var baseUrl: String? = null
    private var data: String? = null
    private var mimeType: String? = null
    private var encoding: String? = null
    private var historyUrl: String? = null
    
    private var isNightMode = false
    var page = 0
    
    var scrollXInPercent = 0.0
    
    lateinit var gestureDetectorCompat: GestureDetectorCompat
    
    init {
        settings.javaScriptEnabled = true
        this.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                settings.defaultFontSize = fontSize
                if (isNightMode) {
                    injectJavascript(
                            211,
                            211,
                            211,
                            10,
                            10,
                            30)
                } else {
                    injectJavascript()
                }
            }
            
            val linkHandler = object : HtmlLinkHandler.OnLinkProcessedCallback {
                override fun onInternalLinkProceeded(text: String) {
                    Toast.makeText(this@HorizontalWebView.context, text, Toast.LENGTH_LONG).show()
                }
            }
            
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                HtmlLinkHandler(getContext(), url, linkHandler)
                return true
            }
        }
        this.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                injectCSS()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1200)
                    recentlyLoaded = false
                    goToPage(page)
                }
                
                result.confirm()
                return true
            }
        }
        this.setOnTouchListener { _, motionEvent ->
            return@setOnTouchListener gestureDetectorCompat.onTouchEvent(motionEvent)
        }
    }
    
    fun snapToClosestPage() {
        goToPage(round(scrollX.toDouble() / width).toInt())
    }
    
    override fun onSaveInstanceState(): Parcelable? {
        page = computeCurrentPage()
        
        val bundle = Bundle()
        bundle.putParcelable(STATE_KEY, super.onSaveInstanceState())
        bundle.putInt(PAGE_KEY, page)
        bundle.putBoolean(NIGHT_MODE_KEY, isNightMode)
        bundle.putDouble(PERCENT_KEY, scrollXInPercent)
        
        bundle.putInt(FONT_SIZE_KEY, fontSize)
        
        bundle.putString(BASE_URL_KEY, baseUrl)
        bundle.putString(DATA_KEY, data)
        bundle.putString(MIME_TYPE_KEY, mimeType)
        bundle.putString(ENCODING_KEY, encoding)
        bundle.putString(HISTORY_URL_KEY, historyUrl)
        return bundle
    }
    
    override fun onRestoreInstanceState(state: Parcelable?) {
        isNightMode = (state as Bundle).getBoolean(NIGHT_MODE_KEY)
        fontSize = state.getInt(FONT_SIZE_KEY)
        page = state.getInt(PAGE_KEY)
        scrollXInPercent = state.getDouble(PERCENT_KEY)
        
        baseUrl = state.getString(BASE_URL_KEY)
        data = state.getString(DATA_KEY)
        mimeType = state.getString(MIME_TYPE_KEY)
        encoding = state.getString(ENCODING_KEY)
        historyUrl = state.getString(HISTORY_URL_KEY)
        
        loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
        super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE)
    }
    
    fun turnPageLeft() {
        if (currentX >= computeHorizontalScrollRange()) {
            currentX = computeHorizontalScrollRange() - width
            scrollTo(currentX, 0)
        }
        if (currentX > 0) {
            currentX -= width
            
            scrollTo(currentX, 0)
            
        }
    }
    
    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        if (scrollX == 0 && clampedX && !recentlyLoaded) {
            leftOverScrollCallback?.invoke()
            recentlyLoaded = true
        } else if (scrollX > 0 && clampedX && !recentlyLoaded) {
            rightOverScrollCallback?.invoke()
            recentlyLoaded = true
        }
    }
    
    fun turnPageRight() {
        if (currentX >= computeHorizontalScrollRange()) {
            currentX = computeHorizontalScrollRange() - width
            scrollTo(currentX, 0)
        } else if (currentX + width < computeHorizontalScrollRange()) {
            currentX += width
            scrollTo(currentX, 0)
        }
    }
    
    fun goToPage(page: Int) {
        this.page = page
        if (page <= getPageCount()) {
            currentX = page * width
            scrollTo(currentX, 0)
        }
    }
    
    fun computeCurrentPage(): Int = currentX / width
    
    fun getPageCount(): Int = (computeHorizontalScrollRange() / width) + 1
    
    fun toggleNightMode() {
        isNightMode = !isNightMode
        page = computeCurrentPage()
        loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }
    
    override fun loadDataWithBaseURL(baseUrl: String?, data: String?, mimeType: String?, encoding: String?, historyUrl: String?) {
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
        this.baseUrl = baseUrl
        this.data = data
        this.mimeType = mimeType
        this.encoding = encoding
        this.historyUrl = historyUrl
    }
    
    private fun injectJavascript(fontColorR: Int = -1, fontColorG: Int = -1, fontColorB: Int = -1, backgroundColorR: Int = -1, backgroundColorG: Int = -1, backgroundColorB: Int = -1) {
        var columns = 1
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columns = 2
        }
        
        var fontColorJsString = "    d.style.color = \"rgb($fontColorR, $fontColorG, $fontColorB)\";\n"
        
        if (fontColorR !in 0..255 || fontColorG !in 0..255 || fontColorB !in 0..255) {
            fontColorJsString = ""
        }
        
        var backgroundColorJsString = "    d.style.backgroundColor = \"rgb($backgroundColorR, $backgroundColorG, $backgroundColorB)\";\n"
        
        if (fontColorR !in 0..255 || fontColorG !in 0..255 || fontColorB !in 0..255) {
            backgroundColorJsString = ""
        }
        
        val js = "function initialize(){\n" +
                "    var d = document.getElementsByTagName('body')[0];\n" +
                "    var ourH = window.innerHeight - 40;\n" +
                "    var ourW = window.innerWidth - (2*20);\n" +
                "    var fullH = d.offsetHeight;\n" +
                "    var pageCount = Math.floor(fullH/ourH)+1;\n" +
                "    var savedPage = 0;\n" +
                "    var newW = pageCount*window.innerWidth - (2*20);\n" +
                "    d.style.height = ourH+'px';\n" +
                "    d.style.width = newW+'px';\n" +
                "    d.style.margin = 0;\n" +
                backgroundColorJsString +
                fontColorJsString +
                "    d.style.webkitColumnGap = '40px';\n" +
                "    d.style.webkitColumnCount = pageCount * $columns;\n" +
                "    document.head.innerHTML = document.head.innerHTML + '<meta name=\"viewport\" content=\"height=device-height, user-scalable=no\" />';" +
                "    return pageCount;\n" + "}"
        
        this.loadUrl("javascript:$js")
        this.loadUrl("javascript:alert(initialize())")
    }
    
    private fun injectCSS() {
        this.loadUrl(
                "javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        "style.innerHTML = 'body { padding: 20px 20px !important; }';" +
                        "parent.appendChild(style)" + "})()")
    }
}