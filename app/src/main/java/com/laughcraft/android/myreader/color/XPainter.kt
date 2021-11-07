package com.laughcraft.android.myreader.color

import android.content.Context
import android.content.res.ColorStateList
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class XPainter @Inject constructor(private val applicationContext: Context): Painter {

    override fun getColor(@ColorRes color: Int): Int = ContextCompat.getColor(applicationContext, color)
    override fun getColorStateList(@ColorRes color: Int): ColorStateList = ContextCompat.getColorStateList(applicationContext, color)!!
}