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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.contract.ViewRecents
import com.laughcraft.android.myreader.presenter.RecentsPresenter
import com.laughcraft.android.myreader.ui.activity.MainActivity
import com.laughcraft.android.myreader.ui.adapter.FilesListAdapter
import kotlinx.android.synthetic.main.fragment_recents.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "RecentsFragment"

class RecentsFragment : FileListFragment(), ViewRecents {
    companion object {
        @JvmStatic
        fun newInstance() = RecentsFragment()
    }
    
    @InjectPresenter lateinit var presenter: RecentsPresenter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recents, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView_recents.layoutManager = LinearLayoutManager(activity,
                                                                 LinearLayoutManager.VERTICAL,
                                                                 false)
        recyclerView_recents.adapter = FilesListAdapter(context!!, null, newOnItemClickListener())
    }
    
    override fun onResume() {
        super.onResume()
        recents_progressBar.visibility = View.VISIBLE
        presenter.showRecents()
    }
    
    override fun showRecents(recents: List<File>) {
        CoroutineScope(Dispatchers.Main).launch {
            recents_progressBar.visibility = View.INVISIBLE
            if (recents.isNullOrEmpty()) recents_no_recents_textView.visibility = View.VISIBLE
            else {
                recents_no_recents_textView.visibility = View.INVISIBLE
            }
            recyclerView_recents.adapter = FilesListAdapter(context!!, recents,
                                                            newOnItemClickListener())
        }
    }
    
    override fun onBackPressedCallback(): Boolean {
        (activity as MainActivity).viewPager.currentItem = 1
        return true
    }
    
    override fun openRecentBook(file: File) {
        books.openBook(file, activity!!)
    }
    
    private fun newOnItemClickListener(): FilesListAdapter.OnItemClickListener {
        return object : FilesListAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                presenter.onItemClick(position)
            }
            
            override fun onItemLongClick(position: Int): Boolean {
                return true
            }
        }
    }
}

