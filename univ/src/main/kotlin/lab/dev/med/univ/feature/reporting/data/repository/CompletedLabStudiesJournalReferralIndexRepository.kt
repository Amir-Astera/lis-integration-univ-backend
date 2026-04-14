package lab.dev.med.univ.feature.reporting.data.repository

import kotlinx.coroutines.reactor.awaitSingle
import lab.dev.med.univ.feature.reporting.domain.JournalAxisTextNormalization
import lab.dev.med.univ.feature.reporting.domain.models.CompletedLabStudiesJournalReconciliationIndex
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

fun interface CompletedLabStudiesJournalReconciliationIndexLoader {
    suspend fun loadReconciliationIndex(): CompletedLabStudiesJournalReconciliationIndex
}

/** Loads referral → service lines from normalized Damumed completed lab studies journal uploads. */
@Component
class CompletedLabStudiesJournalReferralIndexRepository(
    private val databaseClient: DatabaseClient,
) : CompletedLabStudiesJournalReconciliationIndexLoader {

    /**
     * One SQL pass: every fact row joins referral and optional service dimensions.
     */
    override suspend fun loadReconciliationIndex(): CompletedLabStudiesJournalReconciliationIndex {
        val sql = """
            SELECT TRIM(r.raw_value) AS referral_raw,
                   TRIM(COALESCE(s.raw_value, '')) AS service_raw
            FROM damumed_report_normalized_facts f
            INNER JOIN damumed_report_uploads u ON u.id = f.upload_id
            INNER JOIN damumed_report_normalized_fact_dimensions r
                ON r.fact_id = f.id AND r.axis_key = 'referral_number'
            LEFT JOIN damumed_report_normalized_fact_dimensions s
                ON s.fact_id = f.id AND s.axis_key = 'service'
            WHERE u.report_kind = 'COMPLETED_LAB_STUDIES_JOURNAL'
              AND u.normalization_status = 'NORMALIZED'
        """.trimIndent()

        val rows: List<Pair<String?, String?>> = databaseClient.sql(sql)
            .map { row, _ ->
                row.get("referral_raw", String::class.java) to row.get("service_raw", String::class.java)
            }
            .all()
            .collectList()
            .awaitSingle()

        val grouped = linkedMapOf<String, MutableSet<String>>()
        for ((referralRaw, serviceRaw) in rows) {
            val refKey = referralRaw?.let(JournalAxisTextNormalization::normalizeReferral)?.takeIf { it.isNotBlank() }
                ?: continue
            val svcNorm = JournalAxisTextNormalization.normalizeServiceLine(serviceRaw.orEmpty())
            grouped.getOrPut(refKey) { mutableSetOf() }.add(svcNorm)
        }

        return CompletedLabStudiesJournalReconciliationIndex(grouped)
    }
}
