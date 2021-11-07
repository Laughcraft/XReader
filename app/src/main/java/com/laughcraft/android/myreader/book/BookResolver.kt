package com.laughcraft.android.myreader.book

import android.content.Context
import com.laughcraft.android.myreader.const.SuitableFiles
import com.laughcraft.android.myreader.di.module.MainModule
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class BookResolver @Inject constructor(private val applicationContext: Context,
                                       @Named(MainModule.TEMP_DIR_REQUEST) private val tempDir: File){
    fun getBook(path: String): Book {
        return getBook(File(path))
    }

    fun getBook(file: File): Book {
        return when (file.extension.lowercase()){
            "xlsx" -> {
                setupPoi()
                Xlsx(file)
            }
            "docx" -> {
                setupPoi()
                val fontStream = applicationContext.resources.assets.open("times.ttf")
                val fontFile = File(createTempDirectory("Fonts"), "font_${System.currentTimeMillis()}.ttf")
                fontFile.createNewFile()
                FileUtils.copyInputStreamToFile(fontStream, fontFile)
                Docx(file, createTempDirectory(file.nameWithoutExtension), fontFile.absolutePath)
            }
            "pdf"  -> Pdf(file)
            "djvu" -> {
                Natives.loadLibraries(applicationContext, "djvu", "djvulibrejni")
                Djvu(file)
            }
            "epub" -> Epub(file, createTempDirectory(file.nameWithoutExtension))
            "fb2"  -> Fb2(file, createTempDirectory(file.nameWithoutExtension))
            "txt"  -> Txt(file)
            "html" -> Html(file)
            in SuitableFiles.suitableExtensions[SuitableFiles.FileType.Images]!! -> Image(file)
            else   -> throw IllegalArgumentException("Unacceptable extension")
        }
    }
    
    private fun createTempDirectory(name: String): File {
        return File(tempDir, name).apply { mkdir() }
    }
    
    private fun setupPoi(){
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl")
    }
}