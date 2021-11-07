package com.laughcraft.android.myreader.di

import dagger.Component
import com.laughcraft.android.myreader.ui.activity.MainActivity
import com.laughcraft.android.myreader.di.module.FileModule
import com.laughcraft.android.myreader.di.module.MainModule
import com.laughcraft.android.myreader.di.module.ViewModelModule
import com.laughcraft.android.myreader.ui.dialog.ExtensionPickerDialog
import com.laughcraft.android.myreader.ui.fragment.*
import com.laughcraft.android.myreader.ui.fragment.main.FavoritesFragment
import com.laughcraft.android.myreader.ui.fragment.main.HomeFragment
import com.laughcraft.android.myreader.ui.fragment.main.RecentsFragment
import javax.inject.Singleton

@Component(modules = [
    MainModule::class,
    ViewModelModule::class,
    FileModule::class
])
@Singleton
@HomePath
interface MainComponent {
    fun inject(activity: MainActivity)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: FavoritesFragment)
    fun inject(fragment: RecentsFragment)
    fun inject(fragment: FileListFragment)
    fun inject(fragment: MainFragment)
    fun inject(fragment: TextFragment)
    fun inject(fragment: DjvuFragment)
    fun inject(fragment: PdfFragment)
    fun inject(fragment: TableFragment)
    fun inject(fragment: ImageFragment)
    fun inject(fragment: ExtensionPickerDialog)
    fun inject(fragment: ConverterFragment)
    fun inject(fragment: BookMenuFragment)
    fun inject(fragment: FirstFragment)
}