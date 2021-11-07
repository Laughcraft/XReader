package com.laughcraft.android.myreader.book

import android.util.Log
import com.laughcraft.android.myreader.book.Book
import com.lowagie.text.DocumentException
import com.lowagie.text.Font
import com.lowagie.text.pdf.BaseFont
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions
import fr.opensagres.xdocreport.itext.extension.font.ITextFontRegistry
import org.apache.commons.io.FilenameUtils
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class Docx(file: File, private val tempDir: File, private val fontPath: String): Book(file.absolutePath) {

    override var onError: ((Throwable) -> Unit)? = null

    override var title: String = name

    lateinit var pdfFile: File

    override fun getPagesCount(): Int = 0

    override fun open() {
        pdfFile = File(tempDir, FilenameUtils.getBaseName(this@Docx.name) + ".pdf")
        pdfFile.createNewFile()
        pdfFile = convertDocxToPDF(this@Docx, pdfFile)
    }

    override fun close() {
        try { pdfFile.delete() } catch (e:Throwable) { onError?.invoke(e) }
    }

    private fun convertDocxToPDF(docxFile: File, pdfFile: File): File {
        try {
            val docxStream = FileInputStream(docxFile)
            val pdfStream = FileOutputStream(pdfFile)
            // 1) Load DOCX into XWPFDocument
            val document = XWPFDocument(docxStream)

            // 2) Prepare Pdf options
            val options = PdfOptions.create()

            options.fontProvider { familyName, encoding, size, style, color ->
                try {
                    val urName = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED)

                    val urFontName = Font(urName, size)

                    urFontName.setFamily(familyName)
                    return@fontProvider urFontName
                } catch (e: IOException) {
                    onError?.invoke(e)
                    return@fontProvider ITextFontRegistry.getRegistry().getFont(familyName,
                        encoding, size,
                        style, color)
                } catch (e: DocumentException) {
                    onError?.invoke(e)
                    return@fontProvider ITextFontRegistry.getRegistry().getFont(familyName,
                        encoding, size,
                        style, color)
                }
            }
            // 3) Convert XWPFDocument to Pdf
            PdfConverter.getInstance().convert(document, pdfStream, options)
        } catch (e: Exception) {
            onError?.invoke(e)
            Log.e("Docx", "Exception in fontProvider: ", e)
        }

        return pdfFile
    }
}