package com.laughcraft.android.myreader.book

import android.util.Log
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.math.RoundingMode
import java.util.*
import kotlin.math.max

class Xlsx(file: File): Book(file.absolutePath) {
    override var onError: ((Throwable) -> Unit)? = null
    override var title: String = nameWithoutExtension

    private lateinit var workBook: XSSFWorkbook
    private lateinit var evaluator: XSSFFormulaEvaluator

    private var sheetCount = 1

    override fun getPagesCount(): Int = sheetCount

    override fun open() {
        val fs = FileInputStream(this@Xlsx)
        workBook = XSSFWorkbook(fs)
        sheetCount = workBook.numberOfSheets
        Log.i("XReader.Xlsx", "$name. Sheets: $sheetCount")
        evaluator = workBook.creationHelper.createFormulaEvaluator()
    }

    override fun close() {}

    fun getSheetNames(): Array<String> {
        val sheets = mutableListOf<String>()
        workBook.sheetIterator().forEach { sheets.add(it.sheetName) }
        return sheets.toTypedArray()
    }

    fun getSheetName(sheet: Int): String = workBook.getSheetAt(sheet).sheetName

    fun getSheet(sheet: Int): Array<Array<String>> {
        val xlsxSheet = workBook.getSheetAt(sheet)
        Log.i("XReader.Xlsx", "${xlsxSheet.firstHeader}")
        return createArray(xlsxSheet)
    }

    fun getMergedCells(sheet: Int): Array<CellRangeAddress> {
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
                1 -> addLineBreaks(cell.stringCellValue, 40)
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
            Log.e("XReader.Xlsx", "Error: ", e)
        }

        return result
    }

    private fun addLineBreaks(input: String, maxLineLength: Int): String {
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