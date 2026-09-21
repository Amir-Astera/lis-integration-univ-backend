package lab.dev.med.univ.feature.reagents.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reagents.data.entity.ReconciliationCaseAuditEntity
import lab.dev.med.univ.feature.reagents.data.entity.ReconciliationCaseAuditHistoryEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ReconciliationCaseAuditRepository : CoroutineCrudRepository<ReconciliationCaseAuditEntity, String> {
    suspend fun findByParsedSampleId(parsedSampleId: String): ReconciliationCaseAuditEntity?
}

interface ReconciliationCaseAuditHistoryRepository : CoroutineCrudRepository<ReconciliationCaseAuditHistoryEntity, String> {
    fun findAllByCaseIdOrderByChangedAtDesc(caseId: String): Flow<ReconciliationCaseAuditHistoryEntity>
}
