package com.laughcraft.android.myreader.file

import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.string.Translator
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileLengthFormatter @Inject constructor(private val translator: Translator) {

    fun humanReadableByteCount(bytes: Long): String {
        val k:Double = bytes / 1024.0
        val m:Double = bytes / 1024.0 / 1024
        val g:Double = bytes / 1024.0 / 1024 / 1024
        val t:Double = bytes / 1024.0 / 1024 / 1024 / 1024

        val hrSize = when {
            t >= 1 -> "%.2f".format(Locale.ENGLISH, t).plus(translator.getString(R.string.tb))
            g >= 1 -> "%.2f".format(Locale.ENGLISH, g).plus(translator.getString(R.string.gb))
            m >= 1 -> "%.2f".format(Locale.ENGLISH, m).plus(translator.getString(R.string.mb))
            k >= 1 -> "%.2f".format(Locale.ENGLISH, k).plus(translator.getString(R.string.kb))
            else -> "$bytes bytes"
        }

        return hrSize
    }
}