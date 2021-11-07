package com.laughcraft.android.myreader.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import com.laughcraft.android.myreader.di.ViewModelKey
import com.laughcraft.android.myreader.viewmodel.*

@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
    
    @Binds
    @IntoMap
    @ViewModelKey(FavoritesViewModel::class)
    abstract fun bindFavoritesViewModel(viewModel: FavoritesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecentsViewModel::class)
    abstract fun bindRecentsViewModel(viewModel: RecentsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ExplorerViewModel::class)
    abstract fun bindHomeViewModel(viewModel: ExplorerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TextViewModel::class)
    abstract fun bindTextViewModel(viewModel: TextViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ImageViewModel::class)
    abstract fun bindImageViewModel(viewModel: ImageViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DjvuViewModel::class)
    abstract fun bindDjvuViewModel(viewModel: DjvuViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PdfViewModel::class)
    abstract fun bindPdfViewModel(viewModel: PdfViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TableViewModel::class)
    abstract fun bindTableViewModel(viewModel: TableViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConverterViewModel::class)
    abstract fun bindConverterViewModel(viewModel: ConverterViewModel): ViewModel
}