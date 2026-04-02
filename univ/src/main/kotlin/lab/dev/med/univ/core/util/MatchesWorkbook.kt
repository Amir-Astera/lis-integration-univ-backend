package project.gigienist_reports.core.util

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.nio.file.Path

fun ensureExtensionMatchesWorkbook(path: Path, wb: org.apache.poi.ss.usermodel.Workbook): Path {
    val isXlsx = wb is XSSFWorkbook
    val isXls  = wb is HSSFWorkbook
    val targetExt = when {
        isXlsx -> ".xlsx"
        isXls  -> ".xls"
        else   -> ".xlsx"
    }
    val fileName = path.fileName.toString()
    val dot = fileName.lastIndexOf('.')
    val base = if (dot >= 0) fileName.substring(0, dot) else fileName
    val corrected = base + targetExt
    return path.parent?.resolve(corrected) ?: Path.of(corrected)
}