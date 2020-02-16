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

package com.laughcraft.android.myreader.ui.activity

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.viewpager.widget.ViewPager
import com.arellomobile.mvp.MvpAppCompatActivity
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.ui.adapter.MainViewPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

private const val TAG = "MainActivity"

@RuntimePermissions
class MainActivity : MvpAppCompatActivity() {
    private lateinit var viewPagerAdapter: MainViewPagerAdapter
    
    lateinit var viewPager: ViewPager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager = view_pager_main
        initWithPermissionCheck()
    }
    
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                     Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun init() {
        bottom_navigation_view_main_activity.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.tab_recents -> {
                    view_pager_main.currentItem = 0
                    true
                }
                R.id.tab_explorer -> {
                    view_pager_main.currentItem = 1
                    true
                }
                R.id.tab_favorites -> {
                    view_pager_main.currentItem = 2
                    true
                }
                else -> throw Exception("Wrong menu item")
            }
        }
        
        viewPagerAdapter = MainViewPagerAdapter(supportFragmentManager,
                                                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        
        view_pager_main.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> bottom_navigation_view_main_activity.selectedItemId = R.id.tab_recents
                    1 -> bottom_navigation_view_main_activity.selectedItemId = R.id.tab_explorer
                    2 -> bottom_navigation_view_main_activity.selectedItemId = R.id.tab_favorites
                }
            }
            
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
        })
        bottom_navigation_view_main_activity.selectedItemId = R.id.tab_explorer
        
        view_pager_main.adapter = viewPagerAdapter
        view_pager_main.currentItem = 1
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }
    
    override fun onBackPressed() {
        if (!viewPagerAdapter.getRegisteredFragment(
                    view_pager_main.currentItem)!!.onBackPressedCallback()) {
            super.onBackPressed()
        }
    }
    
    interface IOnBackPressed {
        fun onBackPressedCallback(): Boolean
    }
}


