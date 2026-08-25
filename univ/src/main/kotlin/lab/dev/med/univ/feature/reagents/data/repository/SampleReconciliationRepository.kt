package lab.dev.med.univ.feature.reagents.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reagents.data.entity.SampleReconciliationEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate
import java.time.LocalDateTime

interface SampleReconciliationRepository : CoroutineCrudRepository<SampleReconciliationEntity, String> {

    /** Drill-down: all samples for a specific date + catalog entry (optional analyzer filter). */
    @Query("""
        SELECT sr.* FROM sample_reconciliation sr
        WHERE sr.sample_date BETWEEN :from AND :to
          AND (:catalogId IS NULL OR sr.service_catalog_id = :catalogId)
          AND (:analyzerId IS NULL OR sr.analyzer_id = :analyzerId)
          AND (:status IS NULL OR sr.reconciliation_status = :status)
        ORDER BY sr.sample_date DESC, sr.reconciled_at DESC
        LIMIT :pageSize OFFSET :offset
    """)
    fun findDrillDown(
        from: LocalDate,
        to: LocalDate,
        catalogId: String?,
        analyzerId: String?,
        status: String?,
        pageSize: Int,
        offset: Int,
    ): Flow<SampleReconciliationEntity>

    /** Count for pagination metadata. */
    @Query("""
        SELECT COUNT(*) FROM sample_reconciliation sr
        WHERE sr.sample_date BETWEEN :from AND :to
          AND (:catalogId IS NULL OR sr.service_catalog_id = :catalogId)
          AND (:analyzerId IS NULL OR sr.analyzer_id = :analyzerId)
          AND (:status IS NULL OR sr.reconciliation_status = :status)
    """)
    suspend fun countDrillDown(
        from: LocalDate,
        to: LocalDate,
        catalogId: String?,
        analyzerId: String?,
        status: String?,
    ): Long

    /** Samples whose grace has expired and are still PENDING_GRACE — scheduler target. */
    @Query("""
        SELECT sr.* FROM sample_reconciliation sr
        WHERE sr.reconciliation_status = 'PENDING_GRACE'
          AND sr.grace_deadline_at IS NOT NULL
          AND sr.grace_deadline_at < :now
        ORDER BY sr.grace_deadline_at ASC
        LIMIT :limit
    """)
    fun findExpiredGraceSamples(now: LocalDateTime, limit: Int): Flow<SampleReconciliationEntity>

    /** Bulk delete for rebuild. */
    @Query("""
        DELETE FROM sample_reconciliation
        WHERE sample_date BETWEEN :from AND :to
          AND (:analyzerId::varchar IS NULL OR analyzer_id = :analyzerId)
    """)
    suspend fun deleteByDateRangeAndAnalyzer(from: LocalDate, to: LocalDate, analyzerId: String?)

    suspend fun findByParsedSampleId(parsedSampleId: String): SampleReconciliationEntity?
}
