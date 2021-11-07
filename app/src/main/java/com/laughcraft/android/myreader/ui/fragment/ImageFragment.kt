package com.laughcraft.android.myreader.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.databinding.FragmentImageBinding
import com.laughcraft.android.myreader.ui.custom.SnapHelperOneByOne
import com.laughcraft.android.myreader.viewmodel.ImageViewModel
import javax.inject.Inject

class ImageFragment: BookMenuFragment(R.layout.fragment_image) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: ImageViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentImageBinding.bind(view)

        binding.rvImages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        SnapHelperOneByOne().attachToRecyclerView(binding.rvImages)
        gestureDetector = GestureDetectorCompat(requireContext(), MenuGestureListener(view as ViewGroup, 0))
        ImageFragmentArgs.fromBundle(requireArguments()).apply {
            viewModel.prepare(filepath)
        }

        viewModel.adapter.observe(viewLifecycleOwner){
            if (it == null){
                binding.rvImages.visibility = View.GONE
                binding.pbImages.visibility = View.VISIBLE
            } else {
                binding.rvImages.visibility = View.VISIBLE
                binding.pbImages.visibility = View.GONE
                binding.rvImages.adapter = it
                binding.rvImages.scrollToPosition(viewModel.position)
            }
        }

        night = {
            viewModel.adapter.value!!.nightMode = !viewModel.adapter.value!!.nightMode
            if (viewModel.adapter.value?.nightMode == true){
                binding.root.setBackgroundColor(Color.BLACK)
            } else {
                binding.root.setBackgroundColor(Color.WHITE)
            }
        }
        share = { shareFile(viewModel.adapter.value!!.image) }
        convert = { findNavController().navigate(DjvuFragmentDirections.actionDjvuFragmentToConverterFragment(viewModel.adapter.value!!.image.absolutePath)) }
        rename = { showRenameDialog(viewModel.adapter.value!!.image) { viewModel.renameFile(viewModel.adapter.value!!.image, it) } }
        bookmark = {
            showBookmarkDialog(viewModel.adapter.value!!.image, 0, viewModel.adapter.value!!.currentPage){
                viewModel.addBookMark(viewModel.adapter.value!!.image, 0, viewModel.adapter.value!!.currentPage, it.comment!!)
            }
        }
        navigate = {
            showNavigateDialog(viewModel.adapter.value!!.currentPage,
                0,
                0..viewModel.adapter.value!!.image.getPagesCount(),
                -2..-1){ _, page -> binding.rvImages.scrollToPosition(page)
            }
        }

        options = {

        }
    }
}