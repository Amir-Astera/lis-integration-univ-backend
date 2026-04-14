package lab.dev.med.univ.feature.reporting.domain

/**
 * Normalization aligned with Damumed CSV / workbook normalization so that
 * applog barcodes and journal axis values compare consistently.
 */
object JournalAxisTextNormalization {

    private val COLLAPSE_WHITESPACE = Regex("\\s+")

    fun normalizeReferral(raw: String): String =
        raw.trim().lowercase().replace(COLLAPSE_WHITESPACE, " ")

    /** Full service line from journal (name, code, commas). */
    fun normalizeServiceLine(raw: String): String =
        raw.trim().lowercase().replace(COLLAPSE_WHITESPACE, " ")

    /** Short fragment from analyzer / LIS JSON (parameter or service name). */
    fun normalizeLogServiceHint(raw: String): String =
        raw.trim().lowercase().replace(COLLAPSE_WHITESPACE, " ")
}
