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

import android.content.Context
import android.graphics.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.*
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.book.Djvu


class DjvuAdapter(val djvu: Djvu,
                  var nightMode: Boolean = false,
                  private val exceptionHandler: CoroutineExceptionHandler) : RecyclerView.Adapter<DjvuAdapter.PageHolder>() {
    private var width: Int = 0
    private var height: Int = 0
    
    private lateinit var context: Context

    var currentPage = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.djvu_page, parent, false)
        context = parent.context
        width = parent.width
        height = parent.height
        
        return PageHolder(itemView)
    }
    
    override fun getItemCount(): Int = djvu.getPagesCount()
    
    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            holder.bind(position)
        }
    }
    
    inner class PageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var photoView: PhotoView? = null
        private var photoView2: PhotoView? = null
        
        fun bind(page: Int) {
            photoView = itemView.findViewById(R.id.pvDjvu)
            photoView2 = itemView.findViewById(R.id.pvDjvu2)
            currentPage = page
            CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                if (photoView2 != null) {
                    loadPage(photoView!!, page)
                    loadPage(photoView2!!, page + 1)
                } else {
                    loadPage(photoView!!, page)
                }
            }
        }
        
        private fun loadPage(photoView: PhotoView, page: Int){
            var bitmap: Bitmap = if (nightMode) djvu.getPage(page) else invert(djvu.getPage(page))

            if (bitmap.height > 2048 || bitmap.width > 2048) {
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            }

            CoroutineScope(Dispatchers.Main).launch(exceptionHandler) { photoView.setImageBitmap(bitmap) }
        }
    }

    fun invert(src: Bitmap): Bitmap {
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
