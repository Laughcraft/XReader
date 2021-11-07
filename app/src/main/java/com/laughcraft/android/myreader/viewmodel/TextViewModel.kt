package com.laughcraft.android.myreader.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.laughcraft.android.myreader.book.BookResolver
import com.laughcraft.android.myreader.book.TextBook
import com.laughcraft.android.myreader.db.dao.FavoritesDao
import com.laughcraft.android.myreader.db.dao.RecentsDao
import kotlinx.coroutines.CoroutineExceptionHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextViewModel @Inject constructor(private val bookResolver: BookResolver,
                                        exceptionHandler: CoroutineExceptionHandler,
                                        favDao: FavoritesDao,
                                        recentsDao: RecentsDao)
    : FileViewModel(exceptionHandler, favDao, recentsDao){
    lateinit var textBook: TextBook

    var chapter: MutableLiveData<String> = MutableLiveData()

    var currentChapterIndex: Int = 0
    var currentPage: Int = 0

    var totalChapters: Int = 1

    fun prepare(path: String, chapterIndex: Int){
        try {
            textBook = bookResolver.getBook(path) as TextBook
            textBook.open()
            chapter = textBook.chapter

            textBook.onError = { error -> Log.e("XReader.Books", "Txt Failed", error) }
            Log.i("XReader.TextFragment", "Chapter: TextBook is ${textBook.javaClass}")
            updateChapter(chapterIndex)
        } catch (throwable: Throwable) {
            Log.e("XReader.Books", "TextViewModel Failed", throwable)
        }
    }

    fun nextChapter(){
        if (currentChapterIndex + 1 in 0..(textBook.tableOfContents.value?.lastIndex ?: 0)){
            updateChapter(++currentChapterIndex)
        } else {
            Log.e("XReader.Books", "Trying to Load wrong next chapter (${currentChapterIndex+1} out of ${textBook.tableOfContents.value?.size})")
        }
    }

    fun previousChapter(){
        if (currentChapterIndex - 1 in 0..(textBook.tableOfContents.value?.lastIndex ?: 0)){
            updateChapter(--currentChapterIndex)
        } else {
            Log.e("XReader.Books", "Trying to Load wrong previous chapter (${currentChapterIndex-1} out of ${textBook.tableOfContents.value?.size})")
        }
    }

    fun goToChapter(chapterIndex: Int){
        updateChapter(chapterIndex)
    }

    private fun updateChapter(chapterIndex: Int){
        Log.i("XReader.TextFragment", "Updating Chapter $chapterIndex")
        this.currentChapterIndex = chapterIndex
        textBook.loadChapter(chapterIndex)
    }
}