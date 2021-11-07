package com.laughcraft.android.myreader.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.databinding.FragmentTextBinding
import com.laughcraft.android.myreader.viewmodel.TextViewModel
import javax.inject.Inject

class TextFragment: BookMenuFragment(R.layout.fragment_text) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: TextViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentTextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTextBinding.bind(view)

        TextFragmentArgs.fromBundle(requireArguments()).apply { viewModel.prepare(filepath, page) }

        night = {
            binding.hwvText.nightMode = !binding.hwvText.nightMode
            showChapter()
        }
        share = { shareFile(viewModel.textBook) }
        convert = { findNavController().navigate(TextFragmentDirections.actionTextFragmentToConverterFragment(viewModel.textBook.absolutePath)) }
        rename = { showRenameDialog(viewModel.textBook) { viewModel.renameFile(viewModel.textBook, it) } }
        bookmark = {
            showBookmarkDialog(viewModel.textBook,viewModel.currentChapterIndex, viewModel.currentPage){
                viewModel.addBookMark(viewModel.textBook, viewModel.currentChapterIndex, viewModel.currentPage, it.comment!!)
            }
        }
        navigate = {
            showNavigateDialog(viewModel.currentPage,
                viewModel.currentChapterIndex,
                0..binding.hwvText.pages,
                0..viewModel.textBook.tableOfContents.value!!.size){chapter, page ->
                viewModel.goToChapter(chapter)
                viewModel.currentPage = page
            }
        }

        options = {

        }

        viewModel.chapter.observe(viewLifecycleOwner){
            Log.i("XReader.TextFragment", "Chapter: $it")
            viewModel.textBook.apply {
                if (it.isNullOrEmpty()){
                    binding.pbText.visibility = View.GONE
                } else {
                    showChapter(baseUrl, it, mimeType, encoding, false, viewModel.currentPage, 14)
                }
            }
        }
        Log.i("XReader.TextFragment", "Initialized!")
    }

    private fun showChapter(){
        showChapter(viewModel.textBook.baseUrl,
            viewModel.chapter.value!!,
            viewModel.textBook.mimeType,
            viewModel.textBook.encoding,
            binding.hwvText.nightMode,
            viewModel.currentPage,
            14)
    }

    private fun showChapter(baseUrl: String,
                            chapter: String,
                            mimeType: String,
                            encoding: String,
                            nightMode: Boolean,
                            page: Int,
                            fontSize: Int) {

        binding.pbText.visibility = View.GONE
        binding.hwvText.apply {
            this.nightMode = nightMode
            currentPage = page
            this.fontSize = fontSize
            requestNextChapter = { viewModel.nextChapter() }
            requestPreviousChapter = { viewModel.previousChapter() }
            openMenu = { showMenu(binding.root) }
            onPageChanged = {viewModel.currentPage = it}
            loadDataWithBaseURL(baseUrl, chapter, mimeType, encoding, null)
        }
    }

}