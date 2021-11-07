package com.laughcraft.android.myreader.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bin.david.form.data.CellRange
import com.bin.david.form.data.format.draw.TextDrawFormat
import com.bin.david.form.data.table.ArrayTableData
import com.bin.david.form.data.table.TableData
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.databinding.FragmentExcelBinding
import com.laughcraft.android.myreader.viewmodel.TableViewModel
import org.apache.poi.ss.util.CellRangeAddress
import javax.inject.Inject

class TableFragment: BookMenuFragment(R.layout.fragment_excel) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: TableViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentExcelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExcelBinding.bind(view)

        binding.stExcel.setZoom(true)

        TableFragmentArgs.fromBundle(requireArguments()).apply { viewModel.prepare(filepath, page) }

        viewModel.table.observe(viewLifecycleOwner){
            showTable("", it, viewModel.mergedCells.value!!)
        }

        night = {
            viewModel.nightMode = !viewModel.nightMode
            if (viewModel.nightMode){
                binding.root.setBackgroundColor(Color.BLACK)
            } else {
                binding.root.setBackgroundColor(Color.WHITE)
            }
        }
        share = { shareFile(viewModel.xlsx) }
        convert = { findNavController().navigate(DjvuFragmentDirections.actionDjvuFragmentToConverterFragment(viewModel.xlsx.absolutePath)) }
        rename = { showRenameDialog(viewModel.xlsx) { viewModel.renameFile(viewModel.xlsx, it) } }
        bookmark = {
            showBookmarkDialog(viewModel.xlsx, 0, viewModel.currentSheet){
                viewModel.addBookMark(viewModel.xlsx, 0, viewModel.currentSheet, it.comment!!)
            }
        }
        navigate = {
            showNavigateDialog(viewModel.currentSheet,
                0,
                0..viewModel.xlsx.getPagesCount(),
                -2..-1){ _, page -> viewModel.loadSheet(page)
            }
        }

        options = {

        }
    }

    private fun showTable(tableName: String,
                          table: Array<Array<String>>,
                          mergedCells: Array<CellRangeAddress>) {
        val names = Array(table.size) { "" }
        //activity_table_toolbar_title_textview.text = tableName

        if (table.isEmpty()) {
            Log.e("XReader.Xlsx", "Empty Array")
            binding.stExcel.tableData = TableData("Empty", arrayListOf())
            return
        }
        val tableData:TableData<Any> = ArrayTableData.create(tableName, names, table, TextDrawFormat())
        val ranges = mergedCells.map { CellRange(it.firstRow, it.lastRow, it.firstColumn, it.lastColumn) }
        tableData.userCellRange = ranges
        
        binding.stExcel.tableData = tableData

        binding.pbExcel.visibility = View.INVISIBLE
    }

}