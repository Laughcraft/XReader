package com.laughcraft.android.myreader.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.laughcraft.android.myreader.db.dao.FavoritesDao
import com.laughcraft.android.myreader.db.entity.BookmarkEntity
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesViewModel @Inject constructor(private val favDao: FavoritesDao,
                                             private val exceptionHandler: CoroutineExceptionHandler
                                             ) : ViewModel() {

    val favorites: MutableLiveData<Map<BookmarkEntity, File>> = MutableLiveData(LinkedHashMap())

    fun update() {
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            favorites.postValue(favDao.getAllBookmarks().associateWith { File(it.path) })
        }
    }

    fun deleteBookMark(bookmarkEntity: BookmarkEntity) {
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            favDao.deleteBookmark(bookmarkEntity)
            favorites.value?.let { favorites.postValue(it - bookmarkEntity) }
        }
    }
}