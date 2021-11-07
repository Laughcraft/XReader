package com.laughcraft.android.myreader.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.const.Directories
import com.laughcraft.android.myreader.const.SuitableFiles
import com.laughcraft.android.myreader.databinding.FragmentListBinding
import com.laughcraft.android.myreader.file.FileResolver
import com.laughcraft.android.myreader.ui.adapter.FileAdapter
import com.laughcraft.android.myreader.viewmodel.ExplorerViewModel
import java.io.File
import javax.inject.Inject
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.webkit.MimeTypeMap
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.textfield.TextInputEditText
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.string.Translator
import com.laughcraft.android.myreader.string.XTranslator

class FileListFragment: Fragment(R.layout.fragment_list) {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var adapter: FileAdapter
    @Inject lateinit var resolver: FileResolver
    @Inject lateinit var translator: Translator

    private val viewModel: ExplorerViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentListBinding

    private lateinit var menuTransition: Transition

    private var directoryDepth = 0

    val cb = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            val name = viewModel.back()?.name ?: ""

            Log.i("XReader", "Back Pressed. Depth: $directoryDepth, Name: $name")
            if (directoryDepth == 1){
                binding.tvDirectoryName.text = translator.getString(R.string.explorer_title)
            } else {
                binding.tvDirectoryName.text = name
            }
            isEnabled = directoryDepth > 1
            directoryDepth--
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListBinding.bind(view)

        menuTransition = Slide(Gravity.BOTTOM).apply {
            duration = 400
            addTarget(binding.fabTransform)
            addTarget(binding.fabRename)
            addTarget(binding.fabShare)
            addTarget(binding.fabDelete)
        }

        adapter.onSelection = { hideShareButtonIfNeeded(it) }
        adapter.onClick = { processClick(it) }
        adapter.onLongClick = { if (it) showMenu() else hideMenu() }
        binding.rvFiles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFiles.adapter = adapter

        val type = FileListFragmentArgs.fromBundle(requireArguments()).listType
        
        binding.ivBack.setOnClickListener { requireActivity().onBackPressed() }
        binding.fabDelete.setOnClickListener { showDeleteDialog() }
        binding.fabShare.setOnClickListener { shareFile(adapter.selectedFiles.first()) }
        binding.fabRename.setOnClickListener { showRenameDialog(adapter.selectedFiles.toList()) }
        binding.fabTransform.setOnClickListener {
            findNavController().navigate(FileListFragmentDirections.actionFileListFragmentToConverterFragment(adapter.selectedFiles.first().absolutePath))
        }
        binding.ivBack.setOnClickListener {
            when(directoryDepth){
                0 -> requireActivity().onBackPressed()
                1 -> {
                    translator.getString(R.string.explorer_title)
                    cb.handleOnBackPressed()
                }
                else -> cb.handleOnBackPressed()
            }
        }

        subscribe(type)
    }

    private fun subscribe(type: Int){
        when (type) {
            Directories.BOOKS ->      {
                binding.tvDirectoryName.text = translator.getString(R.string.books)
                listen(viewModel.books)
            }
            Directories.DOCUMENTS ->  {
                binding.tvDirectoryName.text = translator.getString(R.string.documents)
                listen(viewModel.documents)
            }
            Directories.IMAGES ->     {
                binding.tvDirectoryName.text = translator.getString(R.string.images)
                listen(viewModel.images)
            }
            Directories.AUDIO ->      {
                binding.tvDirectoryName.text = translator.getString(R.string.audio)
                listen(viewModel.audio)
            }
            Directories.VIDEO ->      {
                binding.tvDirectoryName.text = translator.getString(R.string.video)
                listen(viewModel.video)
            }
            Directories.ARCHIVES ->   {
                binding.tvDirectoryName.text = translator.getString(R.string.archives)
                listen(viewModel.archives)
            }
            Directories.DOWNLOADS ->  {
                binding.tvDirectoryName.text = translator.getString(R.string.downloads)
                listen(viewModel.explorerFiles)
                listen(viewModel.downloads)
            }
            Directories.EXPLORER -> {
                binding.tvDirectoryName.text = translator.getString(R.string.explorer_title)
                viewModel.openDirectory()
                listen(viewModel.explorerFiles)
            }
            Directories.NEW -> {
                binding.tvDirectoryName.text = translator.getString(R.string.new_files)
                listen(viewModel.newFiles)
            }
            Directories.SEARCH -> {
                binding.tvDirectoryName.text = viewModel.searchQuery
                listen(viewModel.explorerFiles)
                listen(viewModel.searchResults)
            }
        }
    }

    private fun listen(data: MutableLiveData<List<File>>){
        data.observe(viewLifecycleOwner){
            val s = (translator as XTranslator).getElementsString(it.size)
            binding.tvItems.text = s
            updateAdapter(it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter(files: List<File>){
        adapter.files = files
        adapter.notifyDataSetChanged()
    }

    private fun hideShareButtonIfNeeded(selected: Int){
        if (selected > 1){
            binding.fabShare.visibility = View.GONE
            binding.fabTransform.visibility = View.GONE
        } else {
            binding.fabShare.visibility = View.VISIBLE
            binding.fabTransform.visibility = View.VISIBLE
        }
    }

    private fun shareFile(file: File){
        val intent = Intent(Intent.ACTION_SEND).apply {
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            setDataAndType(Uri.fromFile(file), mime)
        }

        startActivity(Intent.createChooser(intent, translator.getString(R.string.choose_app)))
    }

    private fun openFile(file: File){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            setDataAndType(Uri.fromFile(file), mime)
        }

        startActivity(Intent.createChooser(intent, translator.getString(R.string.choose_app)))
    }

    private fun processClick(file: File){
        when(resolver.resolve(file)){
            SuitableFiles.FileType.Directory -> {
                directoryDepth++
                binding.tvDirectoryName.text = file.name
                binding.tvItems.text = (translator as XTranslator).getElementsString(file.listFiles()?.size ?: 0)
                requireActivity().onBackPressedDispatcher.addCallback(cb)
                viewModel.openDirectory(file.absolutePath)
                return
            }
            SuitableFiles.FileType.Djvu -> findNavController().navigate(FileListFragmentDirections.actionFileListFragmentToDjvuFragment(file.absolutePath, 0))
            SuitableFiles.FileType.Pdfs -> findNavController().navigate(FileListFragmentDirections.actionFileListFragmentToPdfFragment(file.absolutePath, 0))
            SuitableFiles.FileType.Images -> findNavController().navigate(FileListFragmentDirections.actionFileListFragmentToImageFragment(file.absolutePath, 0))
            SuitableFiles.FileType.Tables -> findNavController().navigate(FileListFragmentDirections.actionFileListFragmentToTableFragment(file.absolutePath, 0))
            SuitableFiles.FileType.Texts -> findNavController().navigate(FileListFragmentDirections.actionFileListFragmentToTextFragment(file.absolutePath, 0, 0))
            SuitableFiles.FileType.Other -> openFile(file)
        }
        viewModel.addToRecents(file)
    }

    private fun showMenu() {
        TransitionManager.beginDelayedTransition(binding.root, menuTransition)
        binding.fabShare.visibility = View.VISIBLE
        binding.fabRename.visibility = View.VISIBLE
        binding.fabDelete.visibility = View.VISIBLE
        binding.fabTransform.visibility = View.VISIBLE
    }

    private fun hideMenu() {
        TransitionManager.beginDelayedTransition(binding.root, menuTransition)
        binding.fabRename.visibility = View.GONE
        binding.fabDelete.visibility = View.GONE
        binding.fabShare.visibility = View.GONE
        binding.fabTransform.visibility = View.GONE
    }

    private fun showRenameDialog(files: List<File>){
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_rename, null, false)
        val tiet = dialogView.findViewById<TextInputEditText>(R.id.tietRename)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(translator.getString(R.string.rename))
            .setView(dialogView)
            .setPositiveButton(translator.getString(R.string.save)){ _, _ ->
                viewModel.renameFiles(files, tiet.text.toString()){
                    adapter.updateFileHolders(it)
                    adapter.clearSelectedFiles()
                }
            }
            .setNegativeButton(translator.getString(android.R.string.cancel)){ _, _ -> }
            .create()

        tiet.setText(files[0].nameWithoutExtension)

        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle(translator.getString(R.string.delete_button))
            .setPositiveButton(translator.getString(R.string.delete_button)){ _, _ ->
                adapter.files = adapter.files - adapter.selectedFiles
                viewModel.deleteFiles(adapter.selectedFiles)
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton(translator.getString(android.R.string.cancel)){ _, _ -> }
            .create()
            .show()
    }
}