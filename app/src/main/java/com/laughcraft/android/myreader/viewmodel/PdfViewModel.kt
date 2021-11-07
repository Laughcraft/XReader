package com.laughcraft.android.myreader.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import com.laughcraft.android.myreader.book.BookResolver
import com.laughcraft.android.myreader.book.Docx
import com.laughcraft.android.myreader.book.Pdf
import com.laughcraft.android.myreader.db.dao.FavoritesDao
import com.laughcraft.android.myreader.db.dao.RecentsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfViewModel @Inject constructor(private var bookResolver: BookResolver,
                                       private val exceptionHandler: CoroutineExceptionHandler,
                                       favDao: FavoritesDao,
                                       recentsDao: RecentsDao
): FileViewModel(exceptionHandler, favDao, recentsDao){
    val book: MutableLiveData<Pdf> = MutableLiveData()

    var page: Int = 0

    var nightMode: Boolean = false

    fun prepare(path: String, page:Int){
        this.page = page

        book.value = null

        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            when (val b = bookResolver.getBook(path)){
                is Docx -> {
                    b.open()
                    book.postValue(Pdf(b.pdfFile).apply { open() })
                }
                is Pdf -> book.postValue(b)
                else -> throw IllegalArgumentException("Wrong Book! I've expected Pdf but got ${b.javaClass}")
            }
        }
    }
}