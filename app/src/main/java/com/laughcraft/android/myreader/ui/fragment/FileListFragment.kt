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

package com.laughcraft.android.myreader.ui.fragment

import com.arellomobile.mvp.MvpAppCompatFragment
import com.laughcraft.android.myreader.XReaderApplication
import com.laughcraft.android.myreader.book.Books
import com.laughcraft.android.myreader.ui.activity.MainActivity
import javax.inject.Inject

abstract class FileListFragment : MvpAppCompatFragment(), MainActivity.IOnBackPressed {
    @Inject lateinit var books: Books
    
    init {
        XReaderApplication.bookComponent.inject(this)
    }
}
