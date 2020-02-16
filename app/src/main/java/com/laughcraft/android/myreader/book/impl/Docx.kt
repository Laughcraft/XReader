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

package com.laughcraft.android.myreader.book.impl

import android.util.Log
import com.laughcraft.android.myreader.book.abstr.PdfBook
import com.lowagie.text.DocumentException
import com.lowagie.text.Font
import com.lowagie.text.pdf.BaseFont
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions
import fr.opensagres.xdocreport.itext.extension.font.ITextFontRegistry
import kotlinx.coroutines.*
import org.apache.commons.io.FilenameUtils
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class Docx(parent: String, child: String, private val tempDir: File, private val fontPath: String) : PdfBook(
        parent, child) {
    private var TAG = "DOCX"
    
    val handler = CoroutineExceptionHandler { _, e -> throw e }
    
    override lateinit var pdfFile: File
    
    override suspend fun open(): Job = CoroutineScope(Dispatchers.IO).launch(handler) {
        pdfFile = File(tempDir, FilenameUtils.getBaseName(this@Docx.name) + ".pdf")
        
        pdfFile.createNewFile()
        
        pdfFile = convertDocxToPDF(this@Docx, pdfFile.absolutePath)
    }
    
    override suspend fun close() {
    
    }
    
    private fun convertDocxToPDF(file: File, pdfPath: String): File {
        val pdfFile = File(pdfPath)
        try {
            val docxStream = FileInputStream(file)
            val pdfStream = FileOutputStream(pdfFile)
            // 1) Load DOCX into XWPFDocument
            val document = XWPFDocument(docxStream)
            
            // 2) Prepare Pdf options
            val options = PdfOptions.create()
            
            options.fontProvider { familyName, encoding, size, style, color ->
                try {
                    val urName = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H,
                                                     BaseFont.EMBEDDED)
                    
                    val urFontName = Font(urName, size)
                    
                    urFontName.setFamily(familyName)
                    return@fontProvider urFontName
                } catch (e: IOException) {
                    return@fontProvider ITextFontRegistry.getRegistry().getFont(familyName,
                                                                                encoding, size,
                                                                                style, color)
                } catch (e: DocumentException) {
                    return@fontProvider ITextFontRegistry.getRegistry().getFont(familyName,
                                                                                encoding, size,
                                                                                style, color)
                }
            }
            // 3) Convert XWPFDocument to Pdf
            PdfConverter.getInstance().convert(document, pdfStream, options)
        } catch (e: Exception) {
            Log.e(TAG, "Exception in fontProvider: ", e)
        }
        
        return pdfFile
    }
}
