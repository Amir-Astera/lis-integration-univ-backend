package lab.dev.med.univ.feature.reagents.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reagents.data.entity.AnalyzerReagentLinkEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AnalyzerReagentLinkRepository : CoroutineCrudRepository<AnalyzerReagentLinkEntity, String> {
    fun findAllByAnalyzerIdOrderByUsageRoleAscCreatedAtAsc(analyzerId: String): Flow<AnalyzerReagentLinkEntity>
    fun findAllByAnalyzerIdAndIsActiveTrueOrderByUsageRoleAsc(analyzerId: String): Flow<AnalyzerReagentLinkEntity>
    fun findAllByReagentInventoryIdOrderByCreatedAtAsc(reagentInventoryId: String): Flow<AnalyzerReagentLinkEntity>
    suspend fun findByAnalyzerIdAndReagentInventoryIdAndUsageRole(
        analyzerId: String,
        reagentInventoryId: String,
        usageRole: String,
    ): AnalyzerReagentLinkEntity?
    suspend fun deleteAllByAnalyzerId(analyzerId: String)
    suspend fun deleteAllByReagentInventoryId(reagentInventoryId: String)
}
