package lab.dev.med.univ.feature.reagents.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reagents.data.entity.ReconciliationSourceCoverageEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface ReconciliationSourceCoverageRepository : CoroutineCrudRepository<ReconciliationSourceCoverageEntity, String> {
    fun findAllByCoverageDateBetweenOrderByCoverageDateAsc(
        from: LocalDate,
        to: LocalDate,
    ): Flow<ReconciliationSourceCoverageEntity>

    @Query("""
        DELETE FROM reconciliation_source_coverage
        WHERE coverage_date BETWEEN :from AND :to
          AND (:analyzerId IS NULL OR analyzer_id = :analyzerId)
          AND source_kind IN ('ANALYZER_APPLOG', 'ANALYZER_XML_SNAPSHOT')
    """)
    suspend fun deleteAnalyzerCoverage(
        from: LocalDate,
        to: LocalDate,
        analyzerId: String?,
    )

    @Query("""
        DELETE FROM reconciliation_source_coverage
        WHERE coverage_date BETWEEN :from AND :to
          AND source_kind = 'DAMUMED_COMPLETED_JOURNAL'
    """)
    suspend fun deleteDamumedCoverage(from: LocalDate, to: LocalDate)
}
