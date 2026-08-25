package lab.dev.med.univ.feature.reagents.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reagents.data.entity.ReconciliationSummaryEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface ReconciliationSummaryRepository : CoroutineCrudRepository<ReconciliationSummaryEntity, String> {

    fun findAllBySummaryDateBetweenOrderBySummaryDateAscServiceNameCanonicalAsc(
        from: LocalDate,
        to: LocalDate,
    ): Flow<ReconciliationSummaryEntity>

    fun findAllByAnalyzerIdAndSummaryDateBetweenOrderBySummaryDateAscServiceNameCanonicalAsc(
        analyzerId: String,
        from: LocalDate,
        to: LocalDate,
    ): Flow<ReconciliationSummaryEntity>

    fun findAllBySummaryDateBetweenAndDiscrepancyCountGreaterThanOrderByDiscrepancyCountDescSummaryDateDesc(
        from: LocalDate,
        to: LocalDate,
        minDiscrepancy: Int,
    ): Flow<ReconciliationSummaryEntity>

    suspend fun deleteAllByAnalyzerIdAndSummaryDateBetween(
        analyzerId: String?,
        from: LocalDate,
        to: LocalDate,
    )

    @Query("""
        DELETE FROM reconciliation_daily_summary
        WHERE summary_date BETWEEN :from AND :to
    """)
    suspend fun deleteAllBySummaryDateBetween(from: LocalDate, to: LocalDate)

    @Query("""
        SELECT r.* FROM reconciliation_daily_summary r
        WHERE r.summary_date BETWEEN :from AND :to
          AND (:analyzerId IS NULL OR r.analyzer_id = :analyzerId)
        ORDER BY r.discrepancy_count DESC, r.summary_date ASC
        LIMIT :pageSize OFFSET :offset
    """)
    fun findPagedByDateRange(
        from: LocalDate,
        to: LocalDate,
        analyzerId: String?,
        pageSize: Int,
        offset: Int,
    ): Flow<ReconciliationSummaryEntity>

    @Query("""
        SELECT
            MAX(r.summary_date)                 AS summary_date,
            r.analyzer_id,
            r.service_catalog_id,
            r.service_name_canonical,
            r.category,
            SUM(r.logs_count)                   AS logs_count,
            SUM(r.lis_count)                    AS lis_count,
            SUM(r.reconciled_count)             AS reconciled_count,
            SUM(r.discrepancy_count)            AS discrepancy_count,
            SUM(r.pending_grace_count)          AS pending_grace_count,
            SUM(r.wash_test_count)              AS wash_test_count,
            SUM(r.estimated_wasted_cost_tenge)  AS estimated_wasted_cost_tenge,
            MAX(r.lis_price_per_test_tenge)     AS lis_price_per_test_tenge,
            MAX(r.last_rebuilt_at)              AS last_rebuilt_at,
            MIN(r.id)                           AS id,
            NULL::bigint                        AS version
        FROM reconciliation_daily_summary r
        WHERE r.summary_date BETWEEN :from AND :to
          AND (:analyzerId IS NULL OR r.analyzer_id = :analyzerId)
        GROUP BY r.analyzer_id, r.service_catalog_id, r.service_name_canonical, r.category
        ORDER BY SUM(r.discrepancy_count) DESC, r.service_name_canonical ASC
        LIMIT :pageSize OFFSET :offset
    """)
    fun findAggregatedByService(
        from: LocalDate,
        to: LocalDate,
        analyzerId: String?,
        pageSize: Int,
        offset: Int,
    ): Flow<ReconciliationSummaryEntity>

    @Query("""
        SELECT
            r.summary_date,
            NULL::varchar            AS analyzer_id,
            NULL::varchar            AS service_catalog_id,
            ''                       AS service_name_canonical,
            NULL::varchar            AS category,
            SUM(r.logs_count)                   AS logs_count,
            SUM(r.lis_count)                    AS lis_count,
            SUM(r.reconciled_count)             AS reconciled_count,
            SUM(r.discrepancy_count)            AS discrepancy_count,
            SUM(r.pending_grace_count)          AS pending_grace_count,
            SUM(r.wash_test_count)              AS wash_test_count,
            SUM(r.estimated_wasted_cost_tenge)  AS estimated_wasted_cost_tenge,
            NULL::numeric            AS lis_price_per_test_tenge,
            MAX(r.last_rebuilt_at)   AS last_rebuilt_at,
            MIN(r.id)                AS id,
            NULL::bigint             AS version
        FROM reconciliation_daily_summary r
        WHERE r.summary_date BETWEEN :from AND :to
          AND (:analyzerId IS NULL OR r.analyzer_id = :analyzerId)
        GROUP BY r.summary_date
        ORDER BY r.summary_date ASC
    """)
    fun findDailyTimeline(
        from: LocalDate,
        to: LocalDate,
        analyzerId: String?,
    ): Flow<ReconciliationSummaryEntity>
}
