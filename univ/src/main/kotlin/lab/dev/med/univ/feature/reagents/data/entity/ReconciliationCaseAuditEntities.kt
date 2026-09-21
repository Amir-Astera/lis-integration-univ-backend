package lab.dev.med.univ.feature.reagents.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("reconciliation_cases")
data class ReconciliationCaseAuditEntity(
    @Id
    val id: String,
    val parsedSampleId: String,
    val workflowStatus: String = "OPEN",
    val conclusion: String? = null,
    val assignedTo: String? = null,
    val latestComment: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val updatedBy: String? = null,
    @Version
    val version: Long? = null,
)

@Table("reconciliation_case_events")
data class ReconciliationCaseAuditHistoryEntity(
    @Id
    val id: String,
    val caseId: String,
    val workflowStatus: String,
    val conclusion: String? = null,
    val comment: String? = null,
    val assignedTo: String? = null,
    val changedBy: String? = null,
    val changedAt: LocalDateTime = LocalDateTime.now(),
)
