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

package com.laughcraft.android.myreader.ui.adapter

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.laughcraft.android.myreader.ui.fragment.ExplorerFragment
import com.laughcraft.android.myreader.ui.fragment.FavoritesFragment
import com.laughcraft.android.myreader.ui.fragment.FileListFragment
import com.laughcraft.android.myreader.ui.fragment.RecentsFragment

class MainViewPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
    var registeredFragments = SparseArray<FileListFragment>()
    override fun getCount() = 3
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> RecentsFragment.newInstance()
        1 -> ExplorerFragment.newInstance()
        2 -> FavoritesFragment.newInstance()
        else -> throw Exception("How is this possible?")
    }
    
    override fun instantiateItem(container: ViewGroup, position: Int): Fragment {
        val fragment = super.instantiateItem(container, position) as FileListFragment
        registeredFragments.put(position, fragment)
        return fragment
    }
    
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        registeredFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }
    
    fun getRegisteredFragment(position: Int): FileListFragment? {
        return registeredFragments.get(position)
    }
}
