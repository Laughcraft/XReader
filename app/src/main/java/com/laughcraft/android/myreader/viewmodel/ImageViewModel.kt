package com.laughcraft.android.myreader.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.laughcraft.android.myreader.ui.adapter.ImageBookAdapter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.laughcraft.android.myreader.book.BookResolver
import com.laughcraft.android.myreader.book.Image
import com.laughcraft.android.myreader.db.dao.FavoritesDao
import com.laughcraft.android.myreader.db.dao.RecentsDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageViewModel @Inject constructor(private var bookResolver: BookResolver,
                                         private val exceptionHandler: CoroutineExceptionHandler,
                                         favDao: FavoritesDao,
                                         recentsDao: RecentsDao
                                         ): FileViewModel(exceptionHandler, favDao, recentsDao){
    private lateinit var book: Image
    var adapter: MutableLiveData<ImageBookAdapter> = MutableLiveData()

    var position = 0

    fun prepare(path: String) {
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            book = bookResolver.getBook(path) as Image
            book.open()
            position = book.currentPage
            adapter.postValue(ImageBookAdapter(book, false, exceptionHandler))
        }
    }
}