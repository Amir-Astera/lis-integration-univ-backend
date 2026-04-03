package lab.dev.med.univ.feature.reagents.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reagents.data.entity.LogAnomalyDailySummaryEntity
import lab.dev.med.univ.feature.reagents.data.entity.LogAnomalyRecordEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface LogAnomalyRecordRepository : CoroutineCrudRepository<LogAnomalyRecordEntity, String> {
    fun findAllByAnomalyDateBetweenOrderByAnomalyDateDescAnomalyTimestampDesc(
        from: LocalDate,
        to: LocalDate,
    ): Flow<LogAnomalyRecordEntity>

    fun findAllByAnalyzerIdAndAnomalyDateBetweenOrderByAnomalyDateDescAnomalyTimestampDesc(
        analyzerId: String,
        from: LocalDate,
        to: LocalDate,
    ): Flow<LogAnomalyRecordEntity>

    fun findAllByLogUploadIdOrderByAnomalyTimestampAsc(logUploadId: String): Flow<LogAnomalyRecordEntity>

    suspend fun deleteAllByLogUploadId(logUploadId: String)
}

interface LogAnomalyDailySummaryRepository : CoroutineCrudRepository<LogAnomalyDailySummaryEntity, String> {
    fun findAllBySummaryDateBetweenOrderBySummaryDateAsc(
        from: LocalDate,
        to: LocalDate,
    ): Flow<LogAnomalyDailySummaryEntity>

    fun findAllByAnalyzerIdAndSummaryDateBetweenOrderBySummaryDateAsc(
        analyzerId: String,
        from: LocalDate,
        to: LocalDate,
    ): Flow<LogAnomalyDailySummaryEntity>

    suspend fun findBySummaryDateAndAnalyzerId(summaryDate: LocalDate, analyzerId: String?): LogAnomalyDailySummaryEntity?

    suspend fun deleteAllByAnalyzerIdAndSummaryDateBetween(analyzerId: String?, from: LocalDate, to: LocalDate)
}
