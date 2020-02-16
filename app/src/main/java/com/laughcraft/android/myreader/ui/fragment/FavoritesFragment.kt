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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.contract.ViewFavorites
import com.laughcraft.android.myreader.database.Bookmark
import com.laughcraft.android.myreader.presenter.FavoritesPresenter
import com.laughcraft.android.myreader.ui.activity.MainActivity
import com.laughcraft.android.myreader.ui.adapter.BookmarkAdapter
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "FavoritesFragment"

class FavoritesFragment : FileListFragment(), MainActivity.IOnBackPressed, ViewFavorites {
    
    @InjectPresenter lateinit var presenter: FavoritesPresenter
    
    @ProvidePresenter
    fun provideFavoritesPresenter(): FavoritesPresenter {
        return FavoritesPresenter(activity!!)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView_favorites.layoutManager = LinearLayoutManager(activity,
                                                                   LinearLayoutManager.VERTICAL,
                                                                   false)
        recyclerView_favorites.adapter = BookmarkAdapter(context!!, null, newOnItemClickListener())
    }
    
    override fun onResume() {
        super.onResume()
        favorites_progressBar.visibility = View.VISIBLE
        presenter.showBookmarks()
    }
    
    override fun showBookmarks(bookmarks: List<File>) {
        CoroutineScope(Dispatchers.Main).launch {
            favorites_progressBar.visibility = View.INVISIBLE
            
            if (bookmarks.isNullOrEmpty()) favorites_no_bookmarks_textView.visibility = View.VISIBLE
            else {
                favorites_no_bookmarks_textView.visibility = View.INVISIBLE
            }
            recyclerView_favorites.adapter = BookmarkAdapter(context!!, bookmarks,
                                                             newOnItemClickListener())
        }
    }
    
    companion object {
        @JvmStatic
        fun newInstance() = FavoritesFragment()
    }
    
    override fun onBackPressedCallback(): Boolean {
        (activity as MainActivity).viewPager.currentItem = 1
        return true
    }
    
    private fun newOnItemClickListener(): BookmarkAdapter.OnItemClickListener {
        return object : BookmarkAdapter.OnItemClickListener {
            override fun onDeleteClick(position: Int) {
                presenter.deleteBookmarkedBook(position)
            }
            
            override fun onItemClick(position: Int) {
                presenter.openBookmarkedBook(position)
            }
            
            override fun onItemLongClick(position: Int): Boolean {
                return true
            }
        }
    }
    
    override fun openBook(bookmark: Bookmark) {
        books.openBook(bookmark, activity!!)
    }
    
    override fun showBookmarkChoosingDialog(info: List<String>, onItemClick: (pos: Int) -> Unit) {
        AlertDialog.Builder(activity!!).setTitle(R.string.choose_bookmark).setItems(
                info.toTypedArray()) { _, pos -> onItemClick.invoke(pos) }.create().show()
    }
    
    override fun showDeleteDialog(delete: () -> Unit) {
        AlertDialog.Builder(activity!!).setTitle(R.string.confirm_delete_title).setPositiveButton(
                R.string.delete_button) { _, _ -> delete.invoke() }.setNeutralButton(
                R.string.dismiss) { _, _ -> }.create().show()
    }
}
