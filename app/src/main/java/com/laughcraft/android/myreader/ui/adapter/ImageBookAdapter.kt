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

package com.laughcraft.android.myreader.ui.adapter

import android.graphics.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.book.Image

open class ImageBookAdapter(val image: Image,
                            var nightMode: Boolean = false,
                            private val exceptionHandler: CoroutineExceptionHandler)
    : RecyclerView.Adapter<ImageBookAdapter.PageHolder>() {

    private var width: Int = 0
    private var height: Int = 0

    var currentPage = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_image_page, parent, false)
        width = parent.width
        height = parent.height
        return PageHolder(itemView)
    }
    
    override fun getItemCount(): Int = image.getPagesCount()
    
    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            try {
                val bitmap: Bitmap = if (nightMode) invert(image.getPage(position)) else image.getPage(position)
                CoroutineScope(Dispatchers.Main).launch(exceptionHandler) { holder.bind(bitmap) }
            } catch (e: Exception) {
                Log.e("XReader.Books", "ImageAdapter failed", e)
            }
            currentPage = position
        }
    }
    
    inner class PageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var photoView: PhotoView
        fun bind(bitmap: Bitmap) {
            photoView = itemView.findViewById(R.id.pvImages)

            if (bitmap.height > 2048 || bitmap.width > 2048) {
                photoView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, width, height, true))
                return
            }
            photoView.setImageBitmap(bitmap)
        }
    }

    protected fun invert(src: Bitmap): Bitmap {
        val height = src.height
        val width = src.width
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        val paint = Paint()
        val matrixGrayscale = ColorMatrix()
        matrixGrayscale.setSaturation(0f)
        val matrixInvert = ColorMatrix()
        matrixInvert.set(
            floatArrayOf(
                -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
        )
        matrixInvert.preConcat(matrixGrayscale)
        val filter = ColorMatrixColorFilter(matrixInvert)
        paint.colorFilter = filter
        c.drawBitmap(src, 0f, 0f, paint)
        return bitmap
    }
}
