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
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.dd.processbutton.iml.ActionProcessButton
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReaderApplication
import com.laughcraft.android.myreader.book.Books
import com.laughcraft.android.myreader.contract.ViewConverter
import com.laughcraft.android.myreader.presenter.ConverterPresenter
import kotlinx.android.synthetic.main.activity_converter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.io.File
import javax.inject.Inject

@RuntimePermissions
class ConverterActivity : MvpAppCompatActivity(), ViewConverter {
    @InjectPresenter lateinit var presenter: ConverterPresenter
    
    @ProvidePresenter
    fun provideConverterPresenter(): ConverterPresenter {
        val directory = intent.getStringExtra(FOLDER_KEY)!!
        val name = intent.getStringExtra(FILENAME_KEY)!!
        val file = File(directory, name)
        
        return ConverterPresenter(file)
    }
    
    @Inject lateinit var books: Books
    
    private val TAG = ConverterActivity::class.java.simpleName
    
    override fun onCreate(savedInstanceState: Bundle?) {
        XReaderApplication.bookComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter)
        
        converter_file_to_convert_full_name_textview.text = presenter.file.name
        
        val colorId = chooseColor(FilenameUtils.getExtension(presenter.file.name))
        val extension = FilenameUtils.getExtension(presenter.file.name)
        when {
            extension.length >= 5 -> converter_source_textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP, 32f)
            extension.length == 4 -> converter_source_textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP, 36f)
            else -> converter_source_textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
        }
        
        DrawableCompat.setTint(converter_source_imageView.drawable,
                               ContextCompat.getColor(this, colorId))
        converter_source_textView.setTextColor(ContextCompat.getColor(this, colorId))
        converter_source_textView.text = FilenameUtils.getExtension(presenter.file.name)
        
        button.setMode(ActionProcessButton.Mode.ENDLESS)
        
        converter_extension_spinner.adapter = ArrayAdapter<String>(this, R.layout.spinner_item,
                                                                   resources.getStringArray(
                                                                           R.array.document_conversions))
        
        presenter.onViewCreated()
        button.setOnClickListener { presenter.onConversionButtonClick() }
    }
    
    @NeedsPermission(Manifest.permission.INTERNET)
    fun startConversion(sourceFile: File) {
        presenter.startConversion(sourceFile, converter_extension_spinner.selectedItem.toString(),
                                  null)
        button.isClickable = false
    }
    
    override fun showWarningDialog() {
        val size = com.laughcraft.android.myreader.model.FilenameUtils.humanReadableByteCount(
                presenter.file.length())
        AlertDialog.Builder(this).setTitle(R.string.converter_file_size_warning_title).setMessage(
                resources.getString(R.string.converter_file_size_warning_text,
                                    size)).setPositiveButton(R.string.converter_convert) { _, _ ->
            startConversionWithPermissionCheck(presenter.file)
        }.setNegativeButton(R.string.dismiss) { _, _ -> }.create().show()
    }
    
    private fun chooseColor(extension: String): Int {
        return books.getColor(extension)
    }
    
    override fun updateProgress(progress: Int, messageId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            if (progress < 0) {
                button.isClickable = false
            }
            button.text = resources.getString(messageId)
            button.progress = progress
        }
    }
    
    override fun updateProgress(progress: Int, message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            if (progress < 0) {
                button.isClickable = false
            }
            button.progress = progress
            button.text = message
        }
    }
    
    companion object {
        @JvmStatic
        fun getIntent(context: Context,
                      folder: String,
                      filename: String) = Intent(context, ConverterActivity::class.java).apply {
            putExtra(FOLDER_KEY, folder)
            putExtra(FILENAME_KEY, filename)
        }
    }
}
