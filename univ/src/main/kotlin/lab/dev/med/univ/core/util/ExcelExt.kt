package project.gigienist_reports.core.util

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell

fun Row.createOrGet(col: Int): Cell = getCell(col) ?: createCell(col)