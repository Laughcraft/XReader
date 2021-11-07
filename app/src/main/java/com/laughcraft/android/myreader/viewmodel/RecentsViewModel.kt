package com.laughcraft.android.myreader.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.laughcraft.android.myreader.db.dao.RecentsDao
import com.laughcraft.android.myreader.db.entity.RecentFileEntity
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentsViewModel @Inject constructor(private val recDao: RecentsDao,
                                           private val exceptionHandler: CoroutineExceptionHandler
                                           ): ViewModel() {

    val recents: MutableLiveData<Map<RecentFileEntity, File>> = MutableLiveData(LinkedHashMap())

    fun update(){
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            recents.postValue(recDao.getAllRecentFiles().associateWith { File(it.path) })
        }
    }

    fun deleteRecent(recentFile: RecentFileEntity) {
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            recDao.deleteRecentFile(recentFile)
            recents.value?.let { recents.postValue(it - recentFile) }
        }
    }
}