package com.laughcraft.android.myreader.ui.dialog

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.databinding.DialogConvertionExtensionBinding
import com.laughcraft.android.myreader.string.Translator
import com.laughcraft.android.myreader.ui.adapter.ExtensionAdapter
import com.laughcraft.android.myreader.viewmodel.ConverterViewModel
import javax.inject.Inject

class ExtensionPickerDialog: DialogFragment(R.layout.dialog_convertion_extension) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: ConverterViewModel by viewModels { viewModelFactory }

    private lateinit var binding: DialogConvertionExtensionBinding

    @Inject lateinit var translator: Translator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DialogConvertionExtensionBinding.bind(view)
        Log.i("XReader.Extensions", "Created!")
        binding.rvExtensions.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvExtensions.adapter = ExtensionAdapter(translator.getStringArray(null, R.array.images)).apply {
            onExtensionClicked = {
                viewModel.extension.postValue(it)
                this@ExtensionPickerDialog.dismiss()
            }
        }

        binding.tlExtensions.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                Log.i("XReader.Extensions", "Tab Changed!")
                val array = when (tab.position){
                    0 -> R.array.images
                    1 -> R.array.documents
                    2 -> R.array.books
                    3 -> R.array.presentation
                    else -> R.array.images
                }
                (binding.rvExtensions.adapter as ExtensionAdapter).apply {
                    extensions = translator.getStringArray(null, array)
                    notifyDataSetChanged()
                }

            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


}