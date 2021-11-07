package com.laughcraft.android.myreader.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.databinding.FragmentImageBinding
import com.laughcraft.android.myreader.ui.custom.SnapHelperOneByOne
import com.laughcraft.android.myreader.viewmodel.DjvuViewModel
import javax.inject.Inject

class DjvuFragment: BookMenuFragment(R.layout.fragment_image) {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: DjvuViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentImageBinding.bind(view)

        binding.rvImages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        SnapHelperOneByOne().attachToRecyclerView(binding.rvImages)

        gestureDetector = GestureDetectorCompat(requireContext(), MenuGestureListener(binding.root, 0))
        binding.rvImages.setOnTouchListener { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }
        var p: Int

        DjvuFragmentArgs.fromBundle(requireArguments()).apply {
            viewModel.prepare(filepath)
            p = page
        }

        viewModel.adapter.observe(viewLifecycleOwner){
            if (it == null){
                binding.pbImages.visibility = View.VISIBLE
            } else {
                binding.pbImages.visibility = View.GONE
                binding.rvImages.adapter = it
                binding.rvImages.scrollToPosition(p)
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
        share = { shareFile(viewModel.adapter.value!!.djvu) }
        convert = { findNavController().navigate(DjvuFragmentDirections.actionDjvuFragmentToConverterFragment(viewModel.adapter.value!!.djvu.absolutePath)) }
        rename = { showRenameDialog(viewModel.adapter.value!!.djvu) { viewModel.renameFile(viewModel.adapter.value!!.djvu, it) } }
        bookmark = {
            showBookmarkDialog(viewModel.adapter.value!!.djvu, 0, viewModel.adapter.value!!.currentPage){
                viewModel.addBookMark(viewModel.adapter.value!!.djvu, 0, viewModel.adapter.value!!.currentPage, it.comment!!)
            }
        }
        navigate = {
            showNavigateDialog(viewModel.adapter.value!!.currentPage,
                0,
                0..viewModel.adapter.value!!.djvu.getPagesCount(),
                -2..-1){ _, page -> binding.rvImages.scrollToPosition(page)
            }
        }

        options = {

        }
    }


}