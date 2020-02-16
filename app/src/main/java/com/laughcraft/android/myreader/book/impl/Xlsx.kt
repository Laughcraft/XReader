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

package com.laughcraft.android.myreader.book.impl

import android.util.Log
import com.laughcraft.android.myreader.book.abstr.TableBook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.math.RoundingMode
import java.util.*
import kotlin.math.max

class Xlsx(parent: String, child: String) : TableBook(parent, child) {
    override var sheetCount: Int = -1
    
    private lateinit var workBook: XSSFWorkbook
    private lateinit var evaluator: XSSFFormulaEvaluator
    
    override suspend fun open(): Job = CoroutineScope(Dispatchers.IO).launch {
        val fs = FileInputStream(this@Xlsx)
        workBook = XSSFWorkbook(fs)
        sheetCount = workBook.numberOfSheets
        
        evaluator = workBook.creationHelper.createFormulaEvaluator()
    }
    
    override suspend fun close() {}
    
    override fun getSheetNames(): Array<String> {
        val sheets = mutableListOf<String>()
        workBook.sheetIterator().forEach { sheets.add(it.sheetName) }
        return sheets.toTypedArray()
    }
    
    override fun getSheetName(sheet: Int): String = workBook.getSheetAt(sheet).sheetName
    
    override fun getSheet(sheet: Int): Array<Array<String>> {
        return createArray(workBook.getSheetAt(sheet))
    }
    
    override fun getMergedCells(sheet: Int): Array<CellRangeAddress> {
        return workBook.getSheetAt(sheet).mergedRegions.toTypedArray()
    }
    
    private fun createArray(sheet: XSSFSheet): Array<Array<String>> {
        val width = sheet.physicalNumberOfRows
        var height = 0
        
        sheet.rowIterator().forEach {
            height = max(height, it.physicalNumberOfCells)
        }
        val array: Array<Array<String>> = Array(height) {
            Array(width) { "" }
        }
        
        //Converting rows to columns
        
        sheet.rowIterator().forEach { row ->
            kotlin.run {
                row.cellIterator().forEach {
                    array[it.columnIndex][it.rowIndex] = getStringFromCell(it as XSSFCell)
                }
            }
        }
        
        return array
    }
    
    private fun getStringFromCell(cell: XSSFCell): String {
        var result = "ERROR"
        try {
            result = when (cell.cellType) {
                0 -> cell.numericCellValue.toBigDecimal().setScale(2,
                                                                   RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()
                1 -> addLinebreaks(cell.stringCellValue, 40)
                2 -> {
                    val value = evaluator.evaluateInCell(cell)
                    when (value.cellType) {
                        0 -> value.numericCellValue.toBigDecimal().setScale(2,
                                                                            RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()
                        else -> ""
                    }
                }
                4 -> cell.booleanCellValue.toString()
                else -> ""
            }
        } catch (e: RuntimeException) {
            // Don't touch this
            Log.e("XLSX", "Error: ", e)
        }
        
        return result
    }
    
    private fun addLinebreaks(input: String, maxLineLength: Int): String {
        val tok = StringTokenizer(input, " ")
        val output = StringBuilder(input.length)
        var lineLen = 0
        while (tok.hasMoreTokens()) {
            val word = tok.nextToken()
            
            if (lineLen + word.length > maxLineLength) {
                output.append("\n")
                lineLen = 0
            }
            output.append(word)
            output.append(" ")
            lineLen += word.length + 1
        }
        return output.toString()
    }
    
}