package com.laughcraft.android.myreader.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import java.lang.StringBuilder
import kotlin.math.abs

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
class NewWebView(context: Context, attrs: AttributeSet) : WebView(context, attrs) {

    enum class Orientation { HORIZONTAL, VERTICAL }

    var gestureDetectorCompat: GestureDetectorCompat = GestureDetectorCompat(context, TextBookGestureListener(this))

    var nightMode = true
    var orientation = Orientation.HORIZONTAL

    var fontColor: Int = Color.BLACK
    var windowBackgroundColor: Int = Color.WHITE

    var nightModeFontColor: Int = Color.WHITE
    var nightModeWindowBackgroundColor: Int = Color.DKGRAY

    var fontSize = 14

    var currentPage = 0
    var pages: Int = 0

    var requestNextChapter: (()->Unit)? = null
    var requestPreviousChapter: (()->Unit)? = null
    var openMenu: (()->Unit)? = null
    var closeMenu: (()->Unit)? = null

    var onPageChanged = { page: Int -> }

    init {
        settings.javaScriptEnabled = true

        webChromeClient = createWebChromeClient()
        webViewClient = createWebViewClient()

        setOnLongClickListener { true }

        setOnTouchListener { _, motionEvent ->
            return@setOnTouchListener gestureDetectorCompat.onTouchEvent(motionEvent)
        }
    }

    fun next(){
        if (currentPage < pages) { goToPage(++currentPage) } else { requestNextChapter?.invoke() }
        Log.i("NewWebView", "Next! Scrolling to $currentPage page")
    }

    fun previous(){
        if (currentPage > 0) { goToPage(--currentPage) } else { requestPreviousChapter?.invoke() }
        Log.i("NewWebView", "Previous! Scrolling to $currentPage page")
    }

    fun goToPage(page: Int){
        currentPage = page
        onPageChanged.invoke(page)
        if (orientation == Orientation.HORIZONTAL) scrollTo( width * page,0) else scrollTo( 0,height * page)
    }

    private fun injectCSS() {
        loadUrl("javascript:(function() {" +
                "var parent = document.getElementsByTagName('head').item(0);" +
                "var style = document.createElement('style');" +
                "style.type = 'text/css';" +
                "style.innerHTML = 'body { padding: 20px 20px!important; }';" +
                "parent.appendChild(style)" + "})()")
    }

    private fun injectJavascript(nightMode: Boolean, orientation: Orientation = Orientation.HORIZONTAL) {

        fun getNightModeString(nightMode: Boolean): String {
            val font: Int = if (nightMode) nightModeFontColor else fontColor
            val back: Int = if (nightMode) nightModeWindowBackgroundColor else windowBackgroundColor

            return "    d.style.color = \"rgb(${Color.red(font)}, ${Color.green(font)}, ${Color.blue(font)})\";\n" +
                    "    d.style.backgroundColor = \"rgb(${Color.red(back)}, ${Color.green(back)}, ${Color.blue(back)})\";\n"
        }

        fun getColumnString(orientation: Orientation): String{
            return if (orientation == Orientation.HORIZONTAL) {
                val columns = if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    1
                else
                    2

                StringBuilder().apply {
                    append("    var ourH = window.innerHeight - 40;\n")
                    append("    var ourW = window.innerWidth - (2*20);\n")
                    append("    var fullH = d.offsetHeight;\n")
                    append("    var pageCount = Math.floor(fullH/ourH);\n")
                    append("    var newW = pageCount*window.innerWidth - (2*20);\n")
                    append("    d.style.height = ourH+'px';\n")
                    append("    d.style.width = newW+'px';\n")
                    append("    d.style.margin = 0;\n")
                    append("    d.style.webkitColumnGap = '40px';\n")
                    append("    d.style.webkitColumnCount = pageCount * $columns;\n")
                    append("    document.head.innerHTML = document.head.innerHTML + '<meta name=\"viewport\" content=\"height=device-height, user-scalable=no\" />';")
                    append("    return pageCount;\n")
                }.toString()
            } else {
                "    return 10;\n"
            }
        }

        val js = "function initialize(){\n" +
                "    var d = document.getElementsByTagName('body')[0];\n" +
                getNightModeString(nightMode) +
                getColumnString(orientation) +
                "}"

        loadUrl("javascript:$js")
        loadUrl("javascript:alert(initialize())")
    }

    private fun createWebChromeClient(): WebChromeClient {
        return object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                injectCSS()
                Handler(context.mainLooper).postDelayed({
                    goToPage(currentPage)
                    //recentlyLoaded = false
                }, 1200L)

                pages = calculatePages(message, orientation)

                Log.i("NewWebView", "There are $pages pages in this text")
                result.confirm()
                return true
            }
        }
    }

    private fun createWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                settings.defaultFontSize = fontSize
                injectJavascript(nightMode, orientation)
            }

            val linkHandler = object : HtmlLinkHandler.OnLinkProcessedCallback {
                override fun onInternalLinkProceeded(text: String) {
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                HtmlLinkHandler(context, url, linkHandler)
                return true
            }
        }
    }

    private fun calculatePages(jsAnswer: String, orientation: Orientation): Int{
        if (orientation == Orientation.VERTICAL) return 1
        val rawPages = try { Integer.parseInt(jsAnswer) } catch (e: Throwable) {
            Log.e("NewWebView", "Cannot extract number from this string:$jsAnswer", e)
            1
        }

        return if (rawPages > 2) rawPages + 2 else rawPages
    }

    class TextBookGestureListener(private val webView: NewWebView) : GestureDetector.SimpleOnGestureListener() {
        private val velocityThreshold = 0

        override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            Log.i("NewWebView", "Fling! Event1: ${event1.x}, ${event1.y} Event2: ${event2.x}, ${event2.y}. Velocity: $velocityX")
            if (event1.y < 300 && event2.y - event1.y > 100 && abs(velocityY) > velocityThreshold) {
                if (webView.orientation == Orientation.HORIZONTAL){ webView.openMenu?.invoke() } else { webView.next() }
            }
            if (event2.y < 300 && event1.y - event2.y > 100 && abs(velocityY) > velocityThreshold) {
                if (webView.orientation == Orientation.HORIZONTAL){ webView.closeMenu?.invoke() } else { webView.previous() }
            }
            if (event2.x - event1.x > 50 && abs(velocityX) > velocityThreshold) {
                if (webView.orientation == Orientation.HORIZONTAL){ webView.previous() } else { webView.closeMenu?.invoke() }
            }
            if (event1.x - event2.x > 50 && abs(velocityX) > velocityThreshold) {
                if (webView.orientation == Orientation.HORIZONTAL){ webView.next() } else { webView.openMenu?.invoke() }
            }
            return true
        }
    }
}