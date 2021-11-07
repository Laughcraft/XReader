package com.laughcraft.android.myreader.viewmodel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineExceptionHandler
import com.laughcraft.android.myreader.book.BookResolver
import com.laughcraft.android.myreader.book.Xlsx
import com.laughcraft.android.myreader.db.dao.FavoritesDao
import com.laughcraft.android.myreader.db.dao.RecentsDao
import org.apache.poi.ss.util.CellRangeAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TableViewModel @Inject constructor(private var bookResolver: BookResolver,
                                         private val exceptionHandler: CoroutineExceptionHandler,
                                         favDao: FavoritesDao,
                                         recentsDao: RecentsDao
): FileViewModel(exceptionHandler, favDao, recentsDao){
    lateinit var xlsx: Xlsx

    val table: MutableLiveData<Array<Array<String>>> = MutableLiveData(arrayOf())
    val mergedCells: MutableLiveData<Array<CellRangeAddress>> = MutableLiveData(arrayOf())

    var nightMode: Boolean = false
    var currentSheet = 0

    fun prepare(path: String, sheet: Int){
        xlsx = bookResolver.getBook(path) as Xlsx
        xlsx.open()

        loadSheet(sheet)
    }

    fun loadSheet(index: Int){
        currentSheet = 0
        mergedCells.postValue(xlsx.getMergedCells(index))
        table.postValue(xlsx.getSheet(index))
    }
}