package com.laughcraft.android.myreader.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.const.SuitableFiles
import com.laughcraft.android.myreader.databinding.FragmentConverterBinding
import com.laughcraft.android.myreader.file.FileResolver
import com.laughcraft.android.myreader.net.OnlineConverter
import com.laughcraft.android.myreader.string.Translator
import com.laughcraft.android.myreader.ui.dialog.ExtensionPickerDialog
import com.laughcraft.android.myreader.viewmodel.ConverterViewModel
import java.io.File
import javax.inject.Inject

class ConverterFragment: Fragment(R.layout.fragment_converter) {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: ConverterViewModel by viewModels { viewModelFactory }

    @Inject lateinit var translator: Translator
    @Inject lateinit var resolver: FileResolver

    private lateinit var binding: FragmentConverterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentConverterBinding.bind(view)

        val path = ConverterFragmentArgs.fromBundle(requireArguments()).filepath
        viewModel.prepare(path)

        binding.tvSourceFile.text = File(path).name

        viewModel.progress.observe(viewLifecycleOwner){
            when (it){
                OnlineConverter.ProgressStage.BEFORE -> {
                    binding.tvConvertionStatus.visibility = View.GONE
                    binding.pbConvertion.visibility = View.GONE
                }
                OnlineConverter.ProgressStage.STARTED -> {
                    binding.btnConvert.visibility = View.GONE
                    binding.tvConvertionStatus.visibility = View.VISIBLE
                    binding.tvConvertionStatus.text = translator.getString(R.string.conversion_stage_started)
                    binding.pbConvertion.visibility = View.VISIBLE
                }
                OnlineConverter.ProgressStage.UPLOADING -> {
                    binding.btnConvert.visibility = View.GONE
                    binding.tvConvertionStatus.visibility = View.VISIBLE
                    binding.tvConvertionStatus.text = translator.getString(R.string.conversion_stage_uploading)
                    binding.pbConvertion.visibility = View.VISIBLE
                }
                OnlineConverter.ProgressStage.UPLOADED -> {
                    binding.btnConvert.visibility = View.GONE
                    binding.tvConvertionStatus.visibility = View.VISIBLE
                    binding.tvConvertionStatus.text = translator.getString(R.string.conversion_stage_uploaded)
                    binding.pbConvertion.visibility = View.VISIBLE
                }
                OnlineConverter.ProgressStage.CONVERTING -> {
                    binding.btnConvert.visibility = View.GONE
                    binding.tvConvertionStatus.visibility = View.VISIBLE
                    binding.tvConvertionStatus.text = translator.getString(R.string.conversion_stage_converting)
                    binding.pbConvertion.visibility = View.VISIBLE
                }
                OnlineConverter.ProgressStage.CONVERTED -> {
                    binding.btnConvert.visibility = View.GONE
                    binding.tvConvertionStatus.visibility = View.VISIBLE
                    binding.tvConvertionStatus.text = translator.getString(R.string.conversion_stage_converted)
                    binding.pbConvertion.visibility = View.VISIBLE
                }
                OnlineConverter.ProgressStage.DOWNLOADING -> {
                    binding.btnConvert.visibility = View.GONE
                    binding.tvConvertionStatus.visibility = View.VISIBLE
                    binding.tvConvertionStatus.text = translator.getString(R.string.conversion_stage_downloading)
                    binding.pbConvertion.visibility = View.VISIBLE
                }
                OnlineConverter.ProgressStage.AFTER -> {
                    binding.btnConvert.visibility = View.VISIBLE
                    binding.tvConvertionStatus.visibility = View.VISIBLE
                    binding.tvConvertionStatus.text = translator.getString(R.string.conversion_stage_ready)
                    binding.pbConvertion.visibility = View.GONE
                }
                else -> { binding.pbConvertion.visibility = View.GONE }
            }
        }

        viewModel.internetAccess.observe(viewLifecycleOwner){
            binding.btnConvert.isActivated = it ?: false
            if (it == false) {
                showInternetIsNotAvailableDialog()
            }
        }

        viewModel.extension.observe(viewLifecycleOwner){
            binding.btnChooseExtension.text = if (it == null) {
                binding.btnConvert.isActivated = false
                translator.getString(R.string.choose)
            } else {
                binding.btnConvert.isActivated = true
                it
            }
        }

        binding.btnChooseExtension.setOnClickListener {
            ExtensionPickerDialog().show(childFragmentManager, null)
        }

        binding.btnConvert.setOnClickListener { showInternetDialog() }

        binding.btnOpenResult.setOnClickListener {
            if (viewModel.lastStage == OnlineConverter.ProgressStage.AFTER){
                openConvertedFile(viewModel.convertedFile!!)
            } else {
                val message = translator.getString(R.string.conversion_not_completed)
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openConvertedFile(file: File){
        when(resolver.resolve(file)){
            SuitableFiles.FileType.Djvu ->  findNavController().navigate(ConverterFragmentDirections.actionConverterFragmentToDjvuFragment(file.absolutePath, 0))
            SuitableFiles.FileType.Pdfs ->  findNavController().navigate(ConverterFragmentDirections.actionConverterFragmentToPdfFragment(file.absolutePath, 0))
            SuitableFiles.FileType.Images -> findNavController().navigate(ConverterFragmentDirections.actionConverterFragmentToImageFragment(file.absolutePath, 0))
            SuitableFiles.FileType.Tables -> findNavController().navigate(ConverterFragmentDirections.actionConverterFragmentToTableFragment(file.absolutePath, 0))
            SuitableFiles.FileType.Texts -> findNavController().navigate(ConverterFragmentDirections.actionConverterFragmentToTextFragment(file.absolutePath, 0,0))
            SuitableFiles.FileType.Other -> openFile(file)
            else -> { /*do nothing?*/ }
        }
    }

    private fun openFile(file: File){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            setDataAndType(Uri.fromFile(file), mime)
        }

        startActivity(Intent.createChooser(intent, translator.getString(R.string.choose_app)))
    }

    private fun showInternetDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle(translator.getString(R.string.attention))
            .setMessage(translator.getString(R.string.internet_required))
            .setPositiveButton(translator.getString(R.string.converter_convert)) { _, _ -> viewModel.convert()}
            .setNegativeButton(translator.getString(android.R.string.cancel)) { _, _ -> }
            .create()
            .show()
    }

    private fun showInternetIsNotAvailableDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle(translator.getString(R.string.attention))
            .setMessage(translator.getString(R.string.converter_internet_is_unavailable))
            .setNeutralButton(translator.getString(R.string.converter_convert)) { _, _ -> }
            .create()
            .show()
    }
}