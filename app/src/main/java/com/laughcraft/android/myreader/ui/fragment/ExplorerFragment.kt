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
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.contract.ViewExplorer
import com.laughcraft.android.myreader.presenter.ExplorerPresenter
import com.laughcraft.android.myreader.ui.activity.ConverterActivity
import com.laughcraft.android.myreader.ui.adapter.FilesListAdapter
import kotlinx.android.synthetic.main.fragment_explorer.*
import java.io.File

private const val TAG = "OpenNewDocumentFragment"

class ExplorerFragment : FileListFragment(), ViewExplorer {
    @InjectPresenter lateinit var presenter: ExplorerPresenter
    
    private var mRecyclerViewAdapter: FilesListAdapter? = null
    private var mFiles: List<File>? = null
    
    @ProvidePresenter
    fun provideExplorerPresenter(): ExplorerPresenter {
        val explorerPresenter = ExplorerPresenter(activity!!.applicationContext)
        explorerPresenter.onCreateView()
        return explorerPresenter
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_explorer, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(fragment_explorer_toolbar)
        setHasOptionsMenu(true)
        
        fragment_explorer_toolbar.overflowIcon = ContextCompat.getDrawable(activity!!,
                                                                           R.drawable.ic_sort)
        
        val searchEditText = fragment_explorer_toolbar_searchview.findViewById(
                androidx.appcompat.R.id.search_src_text) as EditText
        searchEditText.setTextColor(
                ContextCompat.getColor(activity as AppCompatActivity, android.R.color.white))
        searchEditText.setHintTextColor(
                ContextCompat.getColor(activity as AppCompatActivity, android.R.color.white))
        
        fragment_explorer_toolbar_searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotEmpty()) presenter.searchFile(query)
                return false
            }
            
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty() || newText.isBlank()) presenter.onCloseSearchView()
                return false
            }
        })
        fragment_explorer_toolbar_searchview.setOnSearchClickListener {
            fragment_explorer_toolbar_title_textview.visibility = View.GONE
            presenter.onOpenSearchView()
        }
        
        fragment_explorer_toolbar_searchview.setOnCloseListener {
            fragment_explorer_toolbar_searchview.onActionViewCollapsed()
            fragment_explorer_toolbar_title_textview.visibility = View.VISIBLE
            presenter.onCloseSearchView()
            true
        }
        
        files_recycler_view.layoutManager = LinearLayoutManager(activity)
        
        app_bar!!.setExpanded(true, true)
    }
    
    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }
    
    override fun setTitle(title: String) {
        fragment_explorer_toolbar_title_textview.text = title
    }
    
    override fun onBackPressedCallback(): Boolean = if (presenter.isItHomeDirectory) {
        false
    } else {
        presenter.openUpperDirectory()
        true
    }
    
    override fun updateFiles(files: List<File>?) {
        mFiles = files
        if (mRecyclerViewAdapter == null) {
            mRecyclerViewAdapter = FilesListAdapter(context!!, files, newOnItemClickListener())
            files_recycler_view.adapter = mRecyclerViewAdapter
        } else {
            mRecyclerViewAdapter!!.setFiles(files)
            mRecyclerViewAdapter!!.notifyDataSetChanged()
        }
    }
    
    private fun newOnItemClickListener(): FilesListAdapter.OnItemClickListener {
        return object : FilesListAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val file = mFiles!![position]
                if (file.isDirectory) {
                    presenter.openInnerDirectory(file.absolutePath)
                    files_recycler_view.scrollToPosition(0)
                } else {
                    books.openBook(file, activity!!)
                }
            }
            
            override fun onItemLongClick(position: Int): Boolean {
                val file = mFiles!![position]
                startActivity(ConverterActivity.getIntent(context!!, file.parent!!, file.name))
                return true
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_explorer_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "Item Selected!")
        when (item.itemId) {
            R.id.menu_explorer_by_name -> presenter.sortingType = 1
            R.id.menu_explorer_by_date -> presenter.sortingType = 2
            R.id.menu_explorer_by_extension -> presenter.sortingType = 3
            R.id.menu_explorer_by_size -> presenter.sortingType = 4
        }
        return super.onOptionsItemSelected(item)
    }
    
    companion object {
        fun newInstance(): ExplorerFragment {
            return ExplorerFragment()
        }
    }
    
}
