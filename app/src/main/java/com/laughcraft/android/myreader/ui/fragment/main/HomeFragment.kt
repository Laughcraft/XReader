package com.laughcraft.android.myreader.ui.fragment.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.color.FilePainter
import com.laughcraft.android.myreader.const.Directories
import com.laughcraft.android.myreader.databinding.FragmentHomeBinding
import com.laughcraft.android.myreader.permission.Permissioner
import com.laughcraft.android.myreader.string.Translator
import com.laughcraft.android.myreader.ui.fragment.MainFragment
import com.laughcraft.android.myreader.viewmodel.ExplorerViewModel
import java.io.File
import javax.inject.Inject

class HomeFragment: Fragment(R.layout.fragment_home) {

    companion object { @JvmStatic fun newInstance(): HomeFragment = HomeFragment() }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var permissioner: Permissioner
    @Inject lateinit var translator: Translator
    @Inject lateinit var painter: FilePainter

    private val viewModel: ExplorerViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)

        checkPermissions()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        viewModel.newFiles.observe(viewLifecycleOwner){ updateNewFiles(it) }
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updateDiagram()
    }

    private fun checkPermissions(){
        val granted = permissioner.requestAllFilesPermissionIfNeeded(this, {}, {
            Log.i("XReader", "Permission Granted? Callback $it")
            setup(it)
        })

        Log.i("XReader", "Permission Granted? $granted")
        setup(granted)
    }

    private fun setup(granted: Boolean){
        if (granted) viewModel.collectFiles { requireActivity().runOnUiThread { updateDiagram() } }
        else hideExplorerUi()
    }

    private fun setupListeners(){
        binding.apply {
            val mainFragment = parentFragment as MainFragment
            ivBooks.setOnClickListener  { mainFragment.openDirectory(Directories.BOOKS) }
            ivDoc.setOnClickListener    { mainFragment.openDirectory(Directories.DOCUMENTS) }
            ivAudio.setOnClickListener  { mainFragment.openDirectory(Directories.AUDIO) }
            ivVideo.setOnClickListener  { mainFragment.openDirectory(Directories.VIDEO) }
            ivImages.setOnClickListener { mainFragment.openDirectory(Directories.IMAGES) }
            ivDlds.setOnClickListener   { mainFragment.openDirectory(Directories.DOWNLOADS) }
            ivArchives.setOnClickListener{mainFragment.openDirectory(Directories.ARCHIVES) }
            ivAll.setOnClickListener    { mainFragment.openDirectory(Directories.EXPLORER) }
            clNew.setOnClickListener    { mainFragment.openDirectory(Directories.NEW) }

            ivNew1.setOnClickListener   { mainFragment.openFile(viewModel.newFiles.value!![0]) }
            ivNew2.setOnClickListener   { mainFragment.openFile(viewModel.newFiles.value!![1]) }
            ivNew3.setOnClickListener   { mainFragment.openFile(viewModel.newFiles.value!![2]) }
            ivNew4.setOnClickListener   { mainFragment.openFile(viewModel.newFiles.value!![3]) }
        }
    }

    private fun hideExplorerUi(){

    }

    private fun updateNewFiles(newFiles: List<File>?){
        if (newFiles == null) {
            binding.pbNewFiles.visibility = View.VISIBLE
            return
        } else {
            binding.pbNewFiles.visibility = View.GONE
        }

        if (newFiles.size >= 3){
            binding.flowNewFiles.visibility = View.VISIBLE
            binding.pbNewFiles.visibility = View.GONE
            Log.i("XReader", "Updating New Files... (${newFiles.size})")

            newFiles[0].apply {
                binding.tvNew1.text = name
                binding.ivNew1.setColorFilter(painter.getColorForFile(this))
            }

            newFiles[1].apply {
                binding.tvNew2.text = name
                binding.ivNew2.setColorFilter(painter.getColorForFile(this))
            }

            newFiles[2].apply {
                binding.tvNew3.text = name
                binding.ivNew3.setColorFilter(painter.getColorForFile(this))
            }

            newFiles[3].apply {
                binding.tvNew4.text = name
                binding.ivNew4.setColorFilter(painter.getColorForFile(this))
            }
        } else {
            Log.i("XReader", "Not enough New Files: ${viewModel.newFiles.value?.size})")
        }
    }

    private fun updateDiagram(){
        Log.i("XReader", "Updating Diagram...")
        val entries = arrayListOf(
            PieEntry(viewModel.booksSpace),
            PieEntry(viewModel.documentsSpace),
            PieEntry(viewModel.audioSpace),
            PieEntry(viewModel.videoSpace),
            PieEntry(viewModel.imagesSpace),
            PieEntry(viewModel.archivesSpace),
            PieEntry(viewModel.emptySpace),
            PieEntry(viewModel.otherSpace))

        val dataset = PieDataSet(entries, "Storage")

        val colors: ArrayList<Int> = arrayListOf(
            painter.getColor(R.color.books),
            painter.getColor(R.color.documents),
            painter.getColor(R.color.audio),
            painter.getColor(R.color.video),
            painter.getColor(R.color.images),
            painter.getColor(R.color.archive),
            painter.getColor(R.color.white),
            painter.getColor(R.color.gray_text))

        dataset.colors = colors
        val pieData = PieData(dataset)

        binding.pcStorage.apply {
            if (viewModel.inited){
                binding.pbStorage.visibility = View.GONE
                visibility = View.VISIBLE
            } else {
                binding.pbStorage.visibility = View.VISIBLE
                visibility = View.GONE
            }

            setHoleColor(Color.TRANSPARENT)
            description = Description().apply { isEnabled = false }
            data = pieData
            legend.isEnabled = false
            invalidate()
        }
    }
}