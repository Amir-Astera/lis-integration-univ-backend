package project.gigienist_reports.core.util

fun String.normalizeCode(): String =
    trim().replace("\\s+".toRegex(), "").uppercase()