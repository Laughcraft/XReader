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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReaderApplication
import com.laughcraft.android.myreader.book.Books
import com.laughcraft.android.myreader.model.FilenameUtils
import java.io.File
import javax.inject.Inject

class BookmarkAdapter(private val context: Context, private var mFiles: List<File>?, private val mOnItemClickListener: OnItemClickListener) : RecyclerView.Adapter<BookmarkAdapter.FileHolder>() {
    
    init {
        XReaderApplication.bookComponent.inject(this)
    }
    
    @Inject lateinit var books: Books
    
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onDeleteClick(position: Int)
        fun onItemLongClick(position: Int): Boolean
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.file_bookmark_item,
                                                                   parent, false)
        return FileHolder(itemView)
    }
    
    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        val file = mFiles!![position]
        holder.bind(file)
    }
    
    override fun getItemCount(): Int {
        return if (mFiles == null) 0
        else mFiles!!.size
    }
    
    fun setFiles(files: List<File>?) {
        mFiles = files
    }
    
    inner class FileHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val mScrollView: HorizontalScrollView
        private val mTypeImageView: ImageView
        private val mTypeTextView: TextView
        private val mNameOfFileTextView: TextView
        private val mSizeOfFileTextView: TextView
        private val deleteImageView: ImageView
        private var mFile: File? = null
        
        init {
            mScrollView = itemView.findViewById(R.id.item_scrollview)
            mNameOfFileTextView = itemView.findViewById(R.id.item_name_textview)
            mTypeImageView = itemView.findViewById(R.id.converter_source_imageView)
            mTypeTextView = itemView.findViewById(R.id.converter_source_textView)
            mSizeOfFileTextView = itemView.findViewById(R.id.item_size_textview)
            deleteImageView = itemView.findViewById(R.id.imageview_delete)
            
            deleteImageView.setOnClickListener {
                mOnItemClickListener.onDeleteClick(layoutPosition)
            }
            
            itemView.setOnClickListener { mOnItemClickListener.onItemClick(layoutPosition) }
            itemView.setOnLongClickListener { mOnItemClickListener.onItemLongClick(layoutPosition) }
        }
        
        
        fun bind(file: File) {
            mFile = file
            mNameOfFileTextView.text = mFile!!.name
            
            if (file.isDirectory) {
                mTypeImageView.setImageResource(R.drawable.ic_doc_folder)
                DrawableCompat.setTint(mTypeImageView.drawable,
                                       ContextCompat.getColor(context, R.color.folder))
                mTypeTextView.visibility = View.INVISIBLE
                //mSizeOfFileTextView.setVisibility(View.INVISIBLE);
                if (!file.listFiles().isNullOrEmpty()) mSizeOfFileTextView.text = mSizeOfFileTextView.context.resources.getString(
                        R.string.elements, file.listFiles()!!.size)
                else mSizeOfFileTextView.text = mSizeOfFileTextView.context.resources.getString(
                        R.string.elements, 0)
            } else {
                mTypeImageView.setImageResource(R.drawable.ic_file_outline)
                
                val colorId = chooseColor(file.extension)
                
                DrawableCompat.setTint(mTypeImageView.drawable,
                                       ContextCompat.getColor(context, colorId))
                mTypeTextView.text = file.extension
                mTypeTextView.visibility = View.VISIBLE
                mTypeTextView.setTextColor(ContextCompat.getColor(context, colorId))
                
                when {
                    file.extension.length >= 5 -> mTypeTextView.setTextSize(
                            TypedValue.COMPLEX_UNIT_SP, 8f)
                    file.extension.length == 4 -> mTypeTextView.setTextSize(
                            TypedValue.COMPLEX_UNIT_SP, 10f)
                    else -> mTypeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                }
                
                mSizeOfFileTextView.text = FilenameUtils.humanReadableByteCount(mFile!!.length())
            }
        }
    }
    
    fun chooseColor(extension: String): Int {
        return books.getColor(extension)
    }
}