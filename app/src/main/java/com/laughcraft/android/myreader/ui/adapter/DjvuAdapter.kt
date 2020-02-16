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
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.book.abstr.ImageBook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DjvuAdapter(private val imageBook: ImageBook) : RecyclerView.Adapter<DjvuAdapter.PageHolder>() {
    private var width: Int = 0
    private var height: Int = 0
    
    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.djvu_page, parent,
                                                                   false)
        context = parent.context
        width = parent.width
        height = parent.height
        
        return PageHolder(itemView)
    }
    
    override fun getItemCount(): Int = imageBook.pageCount
    
    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            holder.bind(position)
        }
    }
    
    inner class PageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var photoView: PhotoView? = null
        private var photoView2: PhotoView? = null
        
        fun bind(page: Int) {
            photoView = itemView.findViewById(R.id.image_book_page_photo_view)
            photoView2 = itemView.findViewById(R.id.image_book_page_photo_view2)
            CoroutineScope(Dispatchers.IO).launch {
                if (photoView2 != null) {
                    loadPage(photoView!!, page).join()
                    loadPage(photoView2!!, page + 1)
                } else {
                    loadPage(photoView!!, page)
                }
            }
        }
        
        private fun loadPage(photoView: PhotoView, page: Int): Job {
            return CoroutineScope(Dispatchers.IO).launch {
                var bitmap: Bitmap? = imageBook.getPage(page) ?: return@launch
                
                if (bitmap!!.height > 2048 || bitmap.width > 2048) bitmap = Bitmap.createScaledBitmap(
                        bitmap, width, height, true)
                CoroutineScope(Dispatchers.Main).launch { photoView.setImageBitmap(bitmap) }
            }
        }
    }
}
