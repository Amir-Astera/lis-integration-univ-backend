package lab.dev.med.univ.feature.reagents.domain.services

/**
 * Heuristic match between a normalized journal service line (Damumed) and a short hint from applogs / LIS JSON.
 */
object CompletedLabJournalServiceLineMatching {

    /** NMU-style codes, optional letter prefix (e.g. B04.149.002). */
    private val SERVICE_CODE_PATTERN = Regex(
        "\\b([A-Za-zА-Яа-яЁё]?\\d{2}\\.\\d{3}\\.\\d{3})\\b",
    )

    fun likelySame(journalNormalized: String, logHintNormalized: String): Boolean {
        if (journalNormalized.isBlank() || logHintNormalized.isBlank()) return false
        if (journalNormalized.contains(logHintNormalized) || logHintNormalized.contains(journalNormalized)) {
            return true
        }

        val jCodes = SERVICE_CODE_PATTERN.findAll(journalNormalized).map { it.groupValues[1].lowercase() }.toSet()
        val lCodes = SERVICE_CODE_PATTERN.findAll(logHintNormalized).map { it.groupValues[1].lowercase() }.toSet()
        if (jCodes.isNotEmpty() && lCodes.isNotEmpty()) {
            // If both sides carry NMU-style codes, only a real code overlap counts (avoids false positives
            // from shared numeric fragments like "002" after splitting on dots).
            return jCodes.intersect(lCodes).isNotEmpty()
        }

        val jTokens = meaningfulTokens(journalNormalized)
        val lTokens = meaningfulTokens(logHintNormalized)
        if (jTokens.isEmpty() || lTokens.isEmpty()) return false

        val overlap = jTokens.intersect(lTokens)
        if (overlap.any { it.length >= 6 }) return true
        if (overlap.size >= 2) return true
        if (overlap.isNotEmpty() && (journalNormalized.length <= 48 || logHintNormalized.length <= 48)) {
            return true
        }
        return false
    }

    private fun meaningfulTokens(s: String): Set<String> =
        s.split(Regex("[^\\p{L}\\p{Nd}]+"))
            .map { it.lowercase() }
            .filter { it.length >= 3 }
            .toSet()
}
