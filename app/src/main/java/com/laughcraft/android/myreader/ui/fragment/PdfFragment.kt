package com.laughcraft.android.myreader.ui.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.book.Pdf
import com.laughcraft.android.myreader.databinding.FragmentPdfBinding
import com.laughcraft.android.myreader.viewmodel.PdfViewModel
import javax.inject.Inject

class PdfFragment: BookMenuFragment(R.layout.fragment_pdf) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: PdfViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPdfBinding.bind(view)

        gestureDetector = GestureDetectorCompat(requireContext(), MenuGestureListener(view as ViewGroup, 0))

        val page = PdfFragmentArgs.fromBundle(requireArguments()).page
        viewModel.prepare(PdfFragmentArgs.fromBundle(requireArguments()).filepath, page)

        viewModel.book.observe(viewLifecycleOwner){ loadPdf(it) }
        binding.pdfView
        night = {
            viewModel.nightMode = !viewModel.nightMode
            loadPdf(viewModel.book.value, binding.pdfView.currentPage, viewModel.nightMode)
        }
        share = { shareFile(viewModel.book.value!!) }
        convert = { findNavController().navigate(PdfFragmentDirections.actionPdfFragmentToConverterFragment(viewModel.book.value!!.absolutePath)) }
        rename = { showRenameDialog(viewModel.book.value!!) { viewModel.renameFile(viewModel.book.value!!, it) } }
        bookmark = {
            showBookmarkDialog(viewModel.book.value!!,0, binding.pdfView.currentPage){
                viewModel.addBookMark(viewModel.book.value!!, 0, binding.pdfView.currentPage, it.comment!!)
            }
        }
        navigate = {
            showNavigateDialog(binding.pdfView.currentPage, 0,
                0..binding.pdfView.pageCount, -2..-1){_, page ->
                binding.pdfView.jumpTo(page)
            }
        }
    }

    private fun loadPdf(pdf: Pdf?, page: Int = 0, nightMode: Boolean = false){
        if (pdf == null) {
            binding.pbPdf.visibility = View.VISIBLE
        } else {
            binding.pbPdf.visibility = View.GONE
            binding.pdfView
                .fromFile(pdf)
                .swipeHorizontal(true)
                .pageSnap(true)
                .defaultPage(page)
                .pageFitPolicy(FitPolicy.WIDTH)
                .onPageChange { newPage, _ -> viewModel.page = newPage }
                .autoSpacing(true)
                .nightMode(nightMode)
                .pageFling(true)
                .load()
        }
    }
}