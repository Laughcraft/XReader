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

package com.laughcraft.android.myreader.ui.fragment.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.databinding.FragmentListBinding
import com.laughcraft.android.myreader.file.FileResolver
import com.laughcraft.android.myreader.string.Translator
import com.laughcraft.android.myreader.ui.adapter.FileAdapter
import com.laughcraft.android.myreader.ui.fragment.MainFragment
import com.laughcraft.android.myreader.viewmodel.FavoritesViewModel
import javax.inject.Inject

class FavoritesFragment private constructor(): Fragment(R.layout.fragment_list) {

    companion object { @JvmStatic fun newInstance() = FavoritesFragment() }

    private lateinit var binding: FragmentListBinding

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var adapter: FileAdapter
    @Inject lateinit var resolver: FileResolver

    @Inject lateinit var translator: Translator

    private val viewModel: FavoritesViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
        viewModel.update()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentListBinding.bind(view)
        binding.rvFiles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFiles.adapter = adapter

        binding.ivBack.visibility = View.GONE
        binding.tvDirectoryName.text = translator.getString(R.string.favorites)

        adapter.onClick = {
            val fragment = (parentFragment as MainFragment)
            val bookmark = viewModel.favorites.value!!.keys.find { bookmark -> bookmark.path == it.absolutePath }!!
            fragment.openFile(it, bookmark.chapter, bookmark.page)
        }

        viewModel.favorites.observe(viewLifecycleOwner){
            adapter.files = it.values.toList()
        }
    }
}
