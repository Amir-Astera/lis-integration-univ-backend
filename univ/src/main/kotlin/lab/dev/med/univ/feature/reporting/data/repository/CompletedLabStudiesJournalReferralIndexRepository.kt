package lab.dev.med.univ.feature.reporting.data.repository

import kotlinx.coroutines.reactor.awaitSingle
import lab.dev.med.univ.feature.reporting.domain.JournalAxisTextNormalization
import lab.dev.med.univ.feature.reporting.domain.models.CompletedLabStudiesJournalReconciliationIndex
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import java.time.LocalDate

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
     * Loads the full journal regardless of date (used for sample-level reconciliation).
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

    /**
     * Loads service name → count aggregates from the completed lab studies journal
     * for the given date range.
     *
     * The [dateFrom] / [dateTo] filter is applied against the `completed_at` dimension
     * stored as raw text. Damumed exports dates in DD.MM.YYYY format; we use
     * TO_DATE(..., 'DD.MM.YYYY') on the PostgreSQL side to avoid pulling all rows.
     *
     * Returns a map: rawServiceName (as stored in LIS) → count of occurrences.
     * Empty services ('') are excluded.
     */
    suspend fun loadServiceStatsByDateRange(dateFrom: LocalDate, dateTo: LocalDate): Map<String, Int> {
        val sql = """
            SELECT TRIM(s.raw_value) AS service_raw,
                   COUNT(*)          AS cnt
            FROM damumed_report_normalized_facts f
            INNER JOIN damumed_report_uploads u
                ON u.id = f.upload_id
               AND u.report_kind = 'COMPLETED_LAB_STUDIES_JOURNAL'
               AND u.normalization_status = 'NORMALIZED'
            INNER JOIN damumed_report_normalized_fact_dimensions s
                ON s.fact_id = f.id AND s.axis_key = 'service'
            LEFT JOIN damumed_report_normalized_fact_dimensions d
                ON d.fact_id = f.id AND d.axis_key = 'completed_at'
            WHERE s.raw_value IS NOT NULL
              AND TRIM(s.raw_value) <> ''
              AND (
                  d.raw_value IS NULL
                  OR (
                      d.raw_value ~ '^\d{2}\.\d{2}\.\d{4}$'
                      AND TO_DATE(d.raw_value, 'DD.MM.YYYY') BETWEEN :dateFrom AND :dateTo
                  )
              )
            GROUP BY TRIM(s.raw_value)
            ORDER BY cnt DESC
        """.trimIndent()

        val rows: List<Pair<String, Int>> = databaseClient.sql(sql)
            .bind("dateFrom", dateFrom)
            .bind("dateTo", dateTo)
            .map { row, _ ->
                val service = row.get("service_raw", String::class.java) ?: ""
                val count = (row.get("cnt", Number::class.java) ?: 0).toInt()
                service to count
            }
            .all()
            .collectList()
            .awaitSingle()

        return rows.filter { it.first.isNotBlank() }.toMap()
    }

    /**
     * Period-filtered reconciliation index — referral keys only from rows
     * whose completed_at falls within [dateFrom]..[dateTo].
     * Used for tighter reconciliation that avoids cross-contamination between date ranges.
     */
    suspend fun loadReconciliationIndexByDateRange(
        dateFrom: LocalDate,
        dateTo: LocalDate,
    ): CompletedLabStudiesJournalReconciliationIndex {
        val sql = """
            SELECT TRIM(r.raw_value)              AS referral_raw,
                   TRIM(COALESCE(s.raw_value,'')) AS service_raw
            FROM damumed_report_normalized_facts f
            INNER JOIN damumed_report_uploads u
                ON u.id = f.upload_id
               AND u.report_kind = 'COMPLETED_LAB_STUDIES_JOURNAL'
               AND u.normalization_status = 'NORMALIZED'
            INNER JOIN damumed_report_normalized_fact_dimensions r
                ON r.fact_id = f.id AND r.axis_key = 'referral_number'
            LEFT JOIN damumed_report_normalized_fact_dimensions s
                ON s.fact_id = f.id AND s.axis_key = 'service'
            LEFT JOIN damumed_report_normalized_fact_dimensions d
                ON d.fact_id = f.id AND d.axis_key = 'completed_at'
            WHERE (
                d.raw_value IS NULL
                OR (
                    d.raw_value ~ '^\d{2}\.\d{2}\.\d{4}$'
                    AND TO_DATE(d.raw_value, 'DD.MM.YYYY') BETWEEN :dateFrom AND :dateTo
                )
            )
        """.trimIndent()

        val rows: List<Pair<String?, String?>> = databaseClient.sql(sql)
            .bind("dateFrom", dateFrom)
            .bind("dateTo", dateTo)
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
