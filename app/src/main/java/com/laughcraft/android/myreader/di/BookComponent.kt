/*
 * Copyright (c) 2019.
 * Created by Vladislav Zraevskij
 *
 * This file is part of XReader.
 *
 *     XReader is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     XReader is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with XReader.  If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>.
 *
 */

package com.laughcraft.android.myreader.di

import com.laughcraft.android.myreader.presenter.ImagePresenter
import com.laughcraft.android.myreader.presenter.PdfPresenter
import com.laughcraft.android.myreader.presenter.TablePresenter
import com.laughcraft.android.myreader.presenter.TextPresenter
import com.laughcraft.android.myreader.ui.activity.BookActivity
import com.laughcraft.android.myreader.ui.activity.ConverterActivity
import com.laughcraft.android.myreader.ui.adapter.BookmarkAdapter
import com.laughcraft.android.myreader.ui.adapter.FilesListAdapter
import com.laughcraft.android.myreader.ui.fragment.FileListFragment

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [BookModule::class])
interface BookComponent {
    fun inject(bookmarkAdapter: BookmarkAdapter)
    fun inject(fileListAdapter: FilesListAdapter)
    fun inject(fileListFragment: FileListFragment)
    fun inject(converterActivity: ConverterActivity)
    fun inject(bookActivity: BookActivity)
    fun inject(presenter: ImagePresenter)
    fun inject(presenter: PdfPresenter)
    fun inject(presenter: TextPresenter)
    fun inject(presenter: TablePresenter)
}