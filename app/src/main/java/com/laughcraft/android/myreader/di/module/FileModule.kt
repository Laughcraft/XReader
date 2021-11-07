package com.laughcraft.android.myreader.di.module

import dagger.Binds
import dagger.Module
import com.laughcraft.android.myreader.file.OmegaExplorer
import com.laughcraft.android.myreader.file.OmegaSearcher
import com.laughcraft.android.myreader.file.OmegaSorter
import com.laughcraft.android.myreader.file.abstr.Explorer
import com.laughcraft.android.myreader.file.abstr.Searcher
import com.laughcraft.android.myreader.file.abstr.Sorter
import com.laughcraft.android.myreader.string.Translator
import com.laughcraft.android.myreader.string.XTranslator
import javax.inject.Singleton

@Module
interface FileModule {
    @Binds @Singleton fun bindSorter(s: OmegaSorter): Sorter
    @Binds @Singleton fun bindSearcher(s: OmegaSearcher): Searcher
    @Binds @Singleton fun bindExplorer(e: OmegaExplorer): Explorer
    @Binds @Singleton fun bindTranslator(e: XTranslator): Translator
}