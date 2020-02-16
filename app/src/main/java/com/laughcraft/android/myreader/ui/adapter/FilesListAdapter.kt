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
import android.os.Environment
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReaderApplication
import com.laughcraft.android.myreader.book.Books
import com.laughcraft.android.myreader.model.FilenameUtils
import java.io.File
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class FilesListAdapter(private val context: Context, private var mFiles: List<File>?, private val mOnItemClickListener: OnItemClickListener) : RecyclerView.Adapter<FilesListAdapter.FileHolder>() {
    
    init {
        XReaderApplication.bookComponent.inject(this)
    }
    
    @Inject lateinit var books: Books
    
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemLongClick(position: Int): Boolean
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent,
                                                                   false)
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
        private val mDateOfFileTextView: TextView
        private var mFile: File? = null
        
        init {
            //itemView.setOnClickListener(this);
            mScrollView = itemView.findViewById(R.id.item_scrollview)
            mNameOfFileTextView = itemView.findViewById(R.id.item_name_textview)
            mTypeImageView = itemView.findViewById(R.id.converter_source_imageView)
            mTypeTextView = itemView.findViewById(R.id.converter_source_textView)
            mSizeOfFileTextView = itemView.findViewById(R.id.item_size_textview)
            mDateOfFileTextView = itemView.findViewById(R.id.item_date_textview)
            
            mNameOfFileTextView.setOnClickListener {
                mOnItemClickListener.onItemClick(layoutPosition)
            }
            itemView.setOnClickListener { mOnItemClickListener.onItemClick(layoutPosition) }
            itemView.setOnLongClickListener { mOnItemClickListener.onItemLongClick(layoutPosition) }
        }
        
        fun bind(file: File) {
            mFile = file
            
            if (file.absolutePath == Environment.getExternalStorageDirectory().absolutePath) {
                mNameOfFileTextView.text = context.getString(R.string.internal_storage)
            } else {
                val name = mFile!!.name
                if (name != null) {
                    mNameOfFileTextView.text = mFile!!.name
                } else {
                    mNameOfFileTextView.text = " "
                }
            }
            
            mDateOfFileTextView.text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                                                      DateFormat.MEDIUM).format(
                    Date(mFile!!.lastModified()))
            
            if (file.isDirectory) {
                mTypeImageView.setImageResource(R.drawable.ic_doc_folder)
                //DrawableCompat.setTint(mTypeImageView.drawable, ContextCompat.getColor(context, R.color.folder))
                mTypeImageView.setColorFilter(ContextCompat.getColor(context, R.color.folder))
                mTypeTextView.visibility = View.INVISIBLE
                //mSizeOfFileTextView.setVisibility(View.INVISIBLE);
                if (!file.listFiles().isNullOrEmpty()) mSizeOfFileTextView.text = mSizeOfFileTextView.context.resources.getString(
                        R.string.elements, file.listFiles()!!.size)
                else mSizeOfFileTextView.text = mSizeOfFileTextView.context.resources.getString(
                        R.string.elements, 0)
            } else {
                mTypeImageView.setImageResource(R.drawable.ic_file_outline)
                val colorId = chooseColor(file.extension)
                
                if (file.extension != null) {
                    mTypeTextView.text = file.extension
                } else {
                    mTypeTextView.text = " "
                }
                
                mTypeTextView.visibility = View.VISIBLE
                mTypeTextView.setTextColor(ContextCompat.getColor(context, colorId))
                //DrawableCompat.setTint(mTypeImageView.drawable, ContextCompat.getColor(context, colorId))
                mTypeImageView.setColorFilter(ContextCompat.getColor(context, colorId))
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
