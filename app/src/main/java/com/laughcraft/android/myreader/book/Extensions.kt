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

package com.laughcraft.android.myreader.book

import android.annotation.SuppressLint
import androidx.annotation.ColorRes
import com.laughcraft.android.myreader.R

class Extensions {
    companion object {
        @SuppressLint("DefaultLocale")
        fun checkExtension(extension: String): String? {
            val checkedExtension = extension.toLowerCase()
            
            if (DjvuExtensions.find(checkedExtension) != null) return checkedExtension
            
            if (ImageExtensions.find(checkedExtension) != null) return checkedExtension
            
            if (TextExtensions.find(checkedExtension) != null) return checkedExtension
            
            if (PdfExtensions.find(checkedExtension) != null) return checkedExtension
            
            if (TableExtensions.find(checkedExtension) != null) return checkedExtension
            
            return null
        }
        
        fun getColor(extension: String): Int {
            val checkedExtension = extension.toLowerCase()
            
            if (DjvuExtensions.find(checkedExtension) != null) return DjvuExtensions.find(
                    checkedExtension)!!.colorId
            
            if (ImageExtensions.find(checkedExtension) != null) return ImageExtensions.find(
                    checkedExtension)!!.colorId
            
            if (TextExtensions.find(checkedExtension) != null) return TextExtensions.find(
                    checkedExtension)!!.colorId
            
            if (PdfExtensions.find(checkedExtension) != null) return PdfExtensions.find(
                    checkedExtension)!!.colorId
            
            if (TableExtensions.find(checkedExtension) != null) return TableExtensions.find(
                    checkedExtension)!!.colorId
            
            throw IllegalArgumentException("Wrong extension")
        }
    }
    
    enum class DjvuExtensions(val extension: String, @ColorRes val colorId: Int) {
        DJVU("djvu", R.color.djvu);
        
        companion object {
            fun find(extension: String): DjvuExtensions? = values().find { extension == it.extension }
        }
    }
    
    enum class TextExtensions(val extension: String, @ColorRes val colorId: Int) {
        TXT("txt", R.color.txt),
        FB2("fb2", R.color.fb2),
        HTML("html", R.color.html),
        XHTML("xhtml", R.color.html),
        EPUB("epub", R.color.epub);
        
        companion object {
            fun find(extension: String): TextExtensions? = values().find { extension == it.extension }
        }
    }
    
    enum class TableExtensions(val extension: String, @ColorRes val colorId: Int) {
        XLSX("xlsx", R.color.xlsx);
        
        companion object {
            fun find(extension: String): TableExtensions? = values().find { extension == it.extension }
        }
    }
    
    enum class ImageExtensions(val extension: String, @ColorRes val colorId: Int) {
        PNG("png", R.color.image),
        JPG("jpg", R.color.image),
        JPEG("jpeg", R.color.image),
        BMP("bmp", R.color.image);
        
        companion object {
            fun find(extension: String): ImageExtensions? = values().find { extension == it.extension }
            var extensions = values().map { it.extension }
        }
    }
    
    enum class PdfExtensions(val extension: String, @ColorRes val colorId: Int) {
        DOCX("docx", R.color.docx),
        PDF("pdf", R.color.pdf);
        
        companion object {
            fun find(extension: String): PdfExtensions? = values().find { extension == it.extension }
        }
    }
}