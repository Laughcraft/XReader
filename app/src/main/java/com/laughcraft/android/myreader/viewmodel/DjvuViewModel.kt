package com.laughcraft.android.myreader.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.laughcraft.android.myreader.book.BookResolver
import com.laughcraft.android.myreader.book.Djvu
import com.laughcraft.android.myreader.db.dao.FavoritesDao
import com.laughcraft.android.myreader.db.dao.RecentsDao
import com.laughcraft.android.myreader.ui.adapter.DjvuAdapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DjvuViewModel @Inject constructor(private val bookResolver: BookResolver,
                                        private val exceptionHandler: CoroutineExceptionHandler,
                                        favDao: FavoritesDao,
                                        recentsDao: RecentsDao
): FileViewModel(exceptionHandler, favDao, recentsDao){

    private lateinit var book: Djvu
    var adapter:MutableLiveData<DjvuAdapter> = MutableLiveData()

    var nightMode = false

    fun prepare(path: String) {
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            book = bookResolver.getBook(path) as Djvu
            book.open()
            adapter.postValue(DjvuAdapter(book, nightMode, exceptionHandler))
        }
    }
}