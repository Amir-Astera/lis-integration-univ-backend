package lab.dev.med.univ.feature.reporting.data.repository

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import lab.dev.med.univ.feature.reporting.domain.JournalAxisTextNormalization
import lab.dev.med.univ.feature.reporting.domain.models.CompletedLabStudiesJournalReconciliationIndex
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import java.time.LocalDate

fun interface CompletedLabStudiesJournalReconciliationIndexLoader {
    suspend fun loadReconciliationIndex(): CompletedLabStudiesJournalReconciliationIndex
}

data class CompletedJournalCoverage(
    val coverageDate: LocalDate,
    val uploadId: String,
    val factCount: Int,
)

data class CompletedJournalDateBounds(
    val from: LocalDate,
    val to: LocalDate,
)

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
            ${selectedJournalFactsCte()}
            SELECT TRIM(r.raw_value) AS referral_raw,
                   TRIM(COALESCE(s.raw_value, '')) AS service_raw
            FROM selected_journal_facts f
            INNER JOIN damumed_report_normalized_fact_dimensions r
                ON r.fact_id = f.fact_id AND r.axis_key = 'referral_number'
            LEFT JOIN damumed_report_normalized_fact_dimensions s
                ON s.fact_id = f.fact_id AND s.axis_key = 'service'
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
            ${selectedJournalFactsCte(restrictToRange = true)}
            SELECT TRIM(s.raw_value) AS service_raw,
                   COUNT(*)          AS cnt
            FROM selected_journal_facts f
            INNER JOIN damumed_report_normalized_fact_dimensions s
                ON s.fact_id = f.fact_id AND s.axis_key = 'service'
            WHERE s.raw_value IS NOT NULL
              AND TRIM(s.raw_value) <> ''
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
     * A dated completed-journal row establishes Damumed coverage for one day.
     * Undated facts are intentionally excluded: they cannot prove that a given
     * analyzer day is comparable.
     */
    suspend fun loadCoverageByDateRange(
        dateFrom: LocalDate,
        dateTo: LocalDate,
    ): List<CompletedJournalCoverage> {
        val sql = """
            ${selectedJournalFactsCte(restrictToRange = true)}
            SELECT f.completed_date AS coverage_date,
                   f.upload_id AS upload_id,
                   COUNT(*) AS fact_count
            FROM selected_journal_facts f
            GROUP BY f.completed_date, f.upload_id
            ORDER BY coverage_date ASC
        """.trimIndent()

        return databaseClient.sql(sql)
            .bind("dateFrom", dateFrom)
            .bind("dateTo", dateTo)
            .map { row, _ ->
                CompletedJournalCoverage(
                    coverageDate = row.get("coverage_date", LocalDate::class.java)!!,
                    uploadId = row.get("upload_id", String::class.java)!!,
                    factCount = (row.get("fact_count", Number::class.java) ?: 0).toInt(),
                )
            }
            .all()
            .collectList()
            .awaitSingle()
    }

    /**
     * The actual dated bounds of one completed-studies journal upload.
     * This lets a delayed monthly upload rebuild the matching historical
     * analyzer dates instead of an arbitrary rolling window around today.
     */
    suspend fun loadDatedCoverageBounds(uploadId: String): CompletedJournalDateBounds? {
        val sql = """
            SELECT
                MIN(TO_DATE(d.raw_value, 'DD.MM.YYYY')) AS period_from,
                MAX(TO_DATE(d.raw_value, 'DD.MM.YYYY')) AS period_to
            FROM damumed_report_normalized_facts f
            INNER JOIN damumed_report_normalized_fact_dimensions d
                ON d.fact_id = f.id
               AND d.axis_key = 'completed_at'
            WHERE f.upload_id = :uploadId
              AND d.raw_value ~ '^\d{2}\.\d{2}\.\d{4}$'
            HAVING COUNT(*) > 0
        """.trimIndent()
        return databaseClient.sql(sql)
            .bind("uploadId", uploadId)
            .map { row, _ ->
                CompletedJournalDateBounds(
                    from = row.get("period_from", LocalDate::class.java)!!,
                    to = row.get("period_to", LocalDate::class.java)!!,
                )
            }
            .one()
            .awaitSingleOrNull()
    }

    /**
     * Exact referral keys indexed by their completed-study date.
     *
     * A barcode can prove that an analyzer sample belongs to a Damumed referral
     * only on the same dated journal day. A whole-period referral index would
     * allow an older study to validate a later analyzer event.
     */
    suspend fun loadReferralKeysByDateRange(
        dateFrom: LocalDate,
        dateTo: LocalDate,
    ): Map<LocalDate, Set<String>> {
        val sql = """
            ${selectedJournalFactsCte(restrictToRange = true)}
            SELECT f.completed_date,
                   TRIM(r.raw_value) AS referral_raw
            FROM selected_journal_facts f
            INNER JOIN damumed_report_normalized_fact_dimensions r
                ON r.fact_id = f.fact_id AND r.axis_key = 'referral_number'
            WHERE r.raw_value IS NOT NULL
              AND TRIM(r.raw_value) <> ''
        """.trimIndent()

        val rows = databaseClient.sql(sql)
            .bind("dateFrom", dateFrom)
            .bind("dateTo", dateTo)
            .map { row, _ ->
                row.get("completed_date", LocalDate::class.java)!! to
                    row.get("referral_raw", String::class.java).orEmpty()
            }
            .all()
            .collectList()
            .awaitSingle()

        return rows
            .groupBy({ it.first }, { JournalAxisTextNormalization.normalizeReferral(it.second) })
            .mapValues { (_, keys) -> keys.filter(String::isNotBlank).toSet() }
    }

    /**
     * Dated referral → service candidates. A single entry means that the
     * service is unambiguous for a sample already linked to that referral on
     * the same day. Multiple entries remain a candidate set and must never be
     * collapsed into an asserted analyzer service.
     */
    suspend fun loadDatedReferralServiceCandidates(
        dateFrom: LocalDate,
        dateTo: LocalDate,
    ): Map<LocalDate, Map<String, Set<String>>> {
        val sql = """
            ${selectedJournalFactsCte(restrictToRange = true)}
            SELECT f.completed_date,
                   TRIM(r.raw_value) AS referral_raw,
                   TRIM(s.raw_value) AS service_raw
            FROM selected_journal_facts f
            INNER JOIN damumed_report_normalized_fact_dimensions r
                ON r.fact_id = f.fact_id AND r.axis_key = 'referral_number'
            INNER JOIN damumed_report_normalized_fact_dimensions s
                ON s.fact_id = f.fact_id AND s.axis_key = 'service'
            WHERE r.raw_value IS NOT NULL
              AND TRIM(r.raw_value) <> ''
              AND s.raw_value IS NOT NULL
              AND TRIM(s.raw_value) <> ''
        """.trimIndent()

        val rows = databaseClient.sql(sql)
            .bind("dateFrom", dateFrom)
            .bind("dateTo", dateTo)
            .map { row, _ ->
                Triple(
                    row.get("completed_date", LocalDate::class.java)!!,
                    JournalAxisTextNormalization.normalizeReferral(row.get("referral_raw", String::class.java).orEmpty()),
                    row.get("service_raw", String::class.java).orEmpty().trim(),
                )
            }
            .all()
            .collectList()
            .awaitSingle()

        return rows
            .filter { (_, referralKey, service) -> referralKey.isNotBlank() && service.isNotBlank() }
            .groupBy { it.first }
            .mapValues { (_, dayRows) ->
                dayRows
                    .groupBy({ it.second }, { it.third })
                    .mapValues { (_, services) -> services.toSet() }
            }
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
            ${selectedJournalFactsCte(restrictToRange = true)}
            SELECT TRIM(r.raw_value)              AS referral_raw,
                   TRIM(COALESCE(s.raw_value,'')) AS service_raw
            FROM selected_journal_facts f
            INNER JOIN damumed_report_normalized_fact_dimensions r
                ON r.fact_id = f.fact_id AND r.axis_key = 'referral_number'
            LEFT JOIN damumed_report_normalized_fact_dimensions s
                ON s.fact_id = f.fact_id AND s.axis_key = 'service'
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

    /**
     * A user may upload a corrected or overlapping monthly journal later.
     * For each completed-study date, use facts from exactly one source upload:
     * the latest successfully normalized upload covering that date.
     *
     * This protects all reconciliation reads from double counting while still
     * allowing a corrected historical period to supersede its predecessor.
     */
    private fun selectedJournalFactsCte(restrictToRange: Boolean = false): String {
        val dateRestriction = if (restrictToRange) {
            "AND TO_DATE(completed.raw_value, 'DD.MM.YYYY') BETWEEN :dateFrom AND :dateTo"
        } else {
            ""
        }
        return """
        WITH dated_journal_facts AS (
            SELECT
                f.id AS fact_id,
                f.upload_id,
                TO_DATE(completed.raw_value, 'DD.MM.YYYY') AS completed_date,
                u.normalization_completed_at,
                u.uploaded_at
            FROM damumed_report_normalized_facts f
            INNER JOIN damumed_report_uploads u
                ON u.id = f.upload_id
               AND u.report_kind = 'COMPLETED_LAB_STUDIES_JOURNAL'
               AND u.normalization_status = 'NORMALIZED'
            INNER JOIN damumed_report_normalized_fact_dimensions completed
                ON completed.fact_id = f.id
               AND completed.axis_key = 'completed_at'
            WHERE completed.raw_value ~ '^\d{2}\.\d{2}\.\d{4}$'
              $dateRestriction
        ),
        latest_upload_per_date AS (
            SELECT DISTINCT ON (completed_date)
                completed_date,
                upload_id
            FROM dated_journal_facts
            ORDER BY
                completed_date,
                normalization_completed_at DESC NULLS LAST,
                uploaded_at DESC,
                upload_id DESC
        ),
        selected_journal_facts AS (
            SELECT f.fact_id, f.upload_id, f.completed_date
            FROM dated_journal_facts f
            INNER JOIN latest_upload_per_date selected
                ON selected.completed_date = f.completed_date
               AND selected.upload_id = f.upload_id
        )
        """.trimIndent()
    }
}
