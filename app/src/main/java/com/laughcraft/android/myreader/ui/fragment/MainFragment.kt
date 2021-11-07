package com.laughcraft.android.myreader.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.const.Directories
import com.laughcraft.android.myreader.const.SuitableFiles
import com.laughcraft.android.myreader.databinding.FragmentMainBinding
import com.laughcraft.android.myreader.file.FileResolver
import com.laughcraft.android.myreader.ui.fragment.main.FavoritesFragment
import com.laughcraft.android.myreader.ui.fragment.main.HomeFragment
import com.laughcraft.android.myreader.ui.fragment.main.RecentsFragment
import com.laughcraft.android.myreader.viewmodel.ExplorerViewModel
import java.io.File
import java.lang.StringBuilder
import javax.inject.Inject

class MainFragment: Fragment(R.layout.fragment_main) {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: ExplorerViewModel by viewModels { viewModelFactory }

    @Inject lateinit var resolver: FileResolver

    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
        requireActivity().intent?.let { intent -> processIntent(intent)?.let { openFile(it, 0, 0) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMainBinding.bind(view)

        setupNavigation()
    }

    private fun setupNavigation(){
        binding.vpMain.adapter = object : FragmentPagerAdapter(childFragmentManager){

            override fun getCount(): Int = 3

            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> RecentsFragment.newInstance()
                    1 -> HomeFragment.newInstance()
                    2 -> FavoritesFragment.newInstance()
                    else -> throw IllegalArgumentException("How is this possible?")
                }
            }
        }

        binding.vpMain.currentItem = 1
        binding.bnvMain.selectedItemId = R.id.tab_explorer

        binding.vpMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when (position){
                    0 -> binding.bnvMain.selectedItemId = R.id.tab_recents
                    1 -> binding.bnvMain.selectedItemId = R.id.tab_explorer
                    2 -> binding.bnvMain.selectedItemId = R.id.tab_favorites
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        binding.bnvMain.setOnItemSelectedListener {
            when (it.itemId){
                R.id.tab_recents -> binding.vpMain.currentItem = 0
                R.id.tab_explorer -> binding.vpMain.currentItem = 1
                R.id.tab_favorites -> binding.vpMain.currentItem = 2
            }
            true
        }

        binding.svMain.apply {
            setOnQueryTextListener( object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.search(query.toString())
                    openDirectory(Directories.SEARCH)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean { return false }
            })
        }
    }

    fun openDirectory(type: Int){
        findNavController().navigate(MainFragmentDirections.actionOpenDirectory(type))
    }

    fun openFile(file: File, chapter: Int = 0, page: Int = 0){
        when (resolver.resolve(file)){
            SuitableFiles.FileType.Djvu -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToDjvuFragment(file.absolutePath, page))
            SuitableFiles.FileType.Pdfs -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToPdfFragment(file.absolutePath, page))
            SuitableFiles.FileType.Tables -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToTableFragment(file.absolutePath, page))
            SuitableFiles.FileType.Texts -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToTextFragment(file.absolutePath, page, chapter))
            SuitableFiles.FileType.Images -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToImageFragment(file.absolutePath, page))
            SuitableFiles.FileType.Other -> openFileSomewhereElse(file)
            else -> openFileSomewhereElse(file)
        }
    }

    private fun openFileSomewhereElse(file: File){
        val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.fromFile(file) }
        startActivity(Intent.createChooser(intent, "Choose an application to open with:"))
    }

    private fun processIntent(intent: Intent?): File? {
        val path = intent?.data?.path ?: return null

        val rootDirPath = Environment.getExternalStorageDirectory().absolutePath
        var file: File
        val pathBuilder = StringBuilder()

        val realFiles = arrayListOf<File>()
        val delimiter = '/'
        path.split(delimiter).reversed().forEach {
            pathBuilder.insert(0, delimiter)
            pathBuilder.insert(0, it)
            pathBuilder.insert(0, delimiter)

            val newPath = rootDirPath + pathBuilder

            Log.i("XReader.Intent", "File path: $newPath")
            file = File(newPath)
            if (file.exists()) realFiles.add(file)
        }

        return realFiles.maxByOrNull { it.absolutePath.length }
    }
}