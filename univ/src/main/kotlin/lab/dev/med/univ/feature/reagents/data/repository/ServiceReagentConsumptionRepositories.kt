package lab.dev.med.univ.feature.reagents.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reagents.data.entity.DamumedReportReagentConsumptionEntity
import lab.dev.med.univ.feature.reagents.data.entity.ServiceReagentConsumptionNormEntity
import lab.dev.med.univ.feature.reagents.data.entity.ServiceToAnalyzerMappingEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Repository for service reagent consumption norms.
 * Defines how much reagent/consumable is used per laboratory service execution.
 */
interface ServiceReagentConsumptionNormRepository : CoroutineCrudRepository<ServiceReagentConsumptionNormEntity, String> {
    fun findAllByIsActiveTrueOrderByServiceNameAsc(): Flow<ServiceReagentConsumptionNormEntity>
    fun findAllByServiceCategoryOrderByServiceNameAsc(serviceCategory: String): Flow<ServiceReagentConsumptionNormEntity>
    fun findAllByAnalyzerIdOrderByServiceNameAsc(analyzerId: String): Flow<ServiceReagentConsumptionNormEntity>
    fun findAllByReagentNameOrderByServiceNameAsc(reagentName: String): Flow<ServiceReagentConsumptionNormEntity>
    fun findAllByServiceNameNormalizedContainingIgnoreCaseAndIsActiveTrue(serviceName: String): Flow<ServiceReagentConsumptionNormEntity>
    suspend fun findByServiceNameNormalizedAndReagentNameAndIsActiveTrue(
        serviceNameNormalized: String,
        reagentName: String,
    ): ServiceReagentConsumptionNormEntity?
}

/**
 * Repository for calculated reagent consumption from Damumed reports.
 */
interface DamumedReportReagentConsumptionRepository : CoroutineCrudRepository<DamumedReportReagentConsumptionEntity, String> {
    fun findAllByUploadIdOrderByCalculatedAtDesc(uploadId: String): Flow<DamumedReportReagentConsumptionEntity>
    fun findAllByFactId(factId: String): Flow<DamumedReportReagentConsumptionEntity>
    fun findAllByServiceNameOrderByCalculatedAtDesc(serviceName: String): Flow<DamumedReportReagentConsumptionEntity>
    suspend fun deleteAllByUploadId(uploadId: String)
}

/**
 * Repository for service-to-analyzer auto-mapping rules.
 */
interface ServiceToAnalyzerMappingRepository : CoroutineCrudRepository<ServiceToAnalyzerMappingEntity, String> {
    fun findAllByIsActiveTrueOrderByMatchingPriorityAsc(): Flow<ServiceToAnalyzerMappingEntity>
    fun findAllByServiceCategoryOrderByMatchingPriorityAsc(serviceCategory: String): Flow<ServiceToAnalyzerMappingEntity>
    fun findAllByAnalyzerIdOrderByMatchingPriorityAsc(analyzerId: String): Flow<ServiceToAnalyzerMappingEntity>
}
