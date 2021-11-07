package com.laughcraft.android.myreader.color

import android.content.res.ColorStateList

interface Painter {
    fun getColor(color: Int): Int
    fun getColorStateList(color: Int): ColorStateList
}