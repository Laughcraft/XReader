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

package com.laughcraft.android.myreader.ui.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.arellomobile.mvp.MvpAppCompatFragment

import com.laughcraft.android.myreader.R

class DocumentPageFragment : MvpAppCompatFragment() {
    private var bitmap: Bitmap? = null
    private var text: String? = null
    private lateinit var textView: TextView
    private lateinit var imageView: ImageView
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_document_page, container, false)
        textView = view.findViewById(R.id.page_text_view)
        imageView = view.findViewById(R.id.page_image_view)
        
        if (text == null) textView.visibility = View.GONE
        else textView.text = text
        if (bitmap == null) imageView.visibility = View.GONE
        else imageView.setImageBitmap(bitmap)
        
        return view
    }
    
    companion object {
        @JvmStatic
        fun newInstance(text: String): DocumentPageFragment {
            val fragment = DocumentPageFragment()
            fragment.text = text
            return fragment
        }
        
        @JvmStatic
        fun newInstance(bitmap: Bitmap): DocumentPageFragment {
            val fragment = DocumentPageFragment()
            fragment.bitmap = bitmap
            return fragment
        }
        
    }
}
