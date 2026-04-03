package lab.dev.med.univ.feature.reagents.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reagents.data.entity.AnalyzerEntity
import lab.dev.med.univ.feature.reagents.data.entity.AnalyzerLogUploadEntity
import lab.dev.med.univ.feature.reagents.data.entity.AnalyzerReagentRateEntity
import lab.dev.med.univ.feature.reagents.data.entity.ConsumableInventoryEntity
import lab.dev.med.univ.feature.reagents.data.entity.ParsedAnalyzerSampleEntity
import lab.dev.med.univ.feature.reagents.data.entity.ReagentConsumptionReportEntity
import lab.dev.med.univ.feature.reagents.data.entity.ReagentInventoryEntity
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogParseStatus
import lab.dev.med.univ.feature.reagents.domain.models.ReagentInventoryStatus
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AnalyzerRepository : CoroutineCrudRepository<AnalyzerEntity, String> {
    fun findAllByOrderByNameAsc(): Flow<AnalyzerEntity>
    fun findAllByIsActiveTrueOrderByNameAsc(): Flow<AnalyzerEntity>
    suspend fun findByLisDeviceSystemName(lisDeviceSystemName: String): AnalyzerEntity?
    suspend fun findByLisAnalyzerId(lisAnalyzerId: Int): AnalyzerEntity?
}

interface AnalyzerReagentRateRepository : CoroutineCrudRepository<AnalyzerReagentRateEntity, String> {
    fun findAllByAnalyzerIdOrderByReagentNameAscOperationTypeAsc(analyzerId: String): Flow<AnalyzerReagentRateEntity>
    fun findAllByAnalyzerIdAndTestModeOrderByReagentNameAscOperationTypeAsc(
        analyzerId: String,
        testMode: String,
    ): Flow<AnalyzerReagentRateEntity>
    suspend fun deleteAllByAnalyzerId(analyzerId: String)
}

interface ReagentInventoryRepository : CoroutineCrudRepository<ReagentInventoryEntity, String> {
    fun findAllByOrderByReceivedAtDescCreatedAtDesc(): Flow<ReagentInventoryEntity>
    fun findAllByAnalyzerIdOrderByReceivedAtDescCreatedAtDesc(analyzerId: String): Flow<ReagentInventoryEntity>
    fun findAllByStatusOrderByReceivedAtDescCreatedAtDesc(status: ReagentInventoryStatus): Flow<ReagentInventoryEntity>
}

interface ConsumableInventoryRepository : CoroutineCrudRepository<ConsumableInventoryEntity, String> {
    fun findAllByOrderByReceivedAtDescCreatedAtDesc(): Flow<ConsumableInventoryEntity>
}

interface AnalyzerLogUploadRepository : CoroutineCrudRepository<AnalyzerLogUploadEntity, String> {
    fun findAllByOrderByUploadedAtDesc(): Flow<AnalyzerLogUploadEntity>
    fun findAllByAnalyzerIdOrderByUploadedAtDesc(analyzerId: String): Flow<AnalyzerLogUploadEntity>
    fun findAllByParseStatusOrderByUploadedAtDesc(parseStatus: AnalyzerLogParseStatus): Flow<AnalyzerLogUploadEntity>
}

interface ParsedAnalyzerSampleRepository : CoroutineCrudRepository<ParsedAnalyzerSampleEntity, String> {
    fun findAllByLogUploadIdOrderBySampleTimestampAsc(logUploadId: String): Flow<ParsedAnalyzerSampleEntity>
    fun findAllByAnalyzerIdOrderBySampleTimestampAsc(analyzerId: String): Flow<ParsedAnalyzerSampleEntity>
    fun findAllByAnalyzerIdAndClassificationOrderBySampleTimestampAsc(
        analyzerId: String,
        classification: SampleClassification,
    ): Flow<ParsedAnalyzerSampleEntity>
    fun findAllBySampleTimestampBetweenOrderBySampleTimestampAsc(
        from: java.time.LocalDateTime,
        to: java.time.LocalDateTime,
    ): Flow<ParsedAnalyzerSampleEntity>
    fun findAllByAnalyzerIdAndSampleTimestampBetweenOrderBySampleTimestampAsc(
        analyzerId: String,
        from: java.time.LocalDateTime,
        to: java.time.LocalDateTime,
    ): Flow<ParsedAnalyzerSampleEntity>
    suspend fun deleteAllByLogUploadId(logUploadId: String)
}

interface ReagentConsumptionReportRepository : CoroutineCrudRepository<ReagentConsumptionReportEntity, String> {
    fun findAllByAnalyzerIdOrderByGeneratedAtDesc(analyzerId: String): Flow<ReagentConsumptionReportEntity>
}
