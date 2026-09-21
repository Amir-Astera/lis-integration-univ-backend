package lab.dev.med.univ.feature.reagents.presentation.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class ReconciliationCaseListItemDto(
    val id: String,
    val parsedSampleId: String,
    val sampleDate: LocalDate,
    val sampleTimestamp: LocalDateTime,
    val analyzerId: String?,
    val analyzerName: String?,
    val barcodeMasked: String,
    val serviceName: String?,
    val category: String?,
    val caseType: String,
    val sourceStatus: String,
    val workflowStatus: String,
    val conclusion: String?,
    val assignedTo: String?,
    val updatedAt: LocalDateTime,
)

data class ReconciliationCasePageDto(
    val rows: List<ReconciliationCaseListItemDto>,
    val total: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int,
)

data class ReconciliationEvidenceStateDto(
    val state: String,
    val detail: String,
)

data class ReconciliationCaseEvidenceDto(
    val orderInDriver: ReconciliationEvidenceStateDto,
    val receivedFromAnalyzer: ReconciliationEvidenceStateDto,
    val savedByDriver: ReconciliationEvidenceStateDto,
    val presentInCompletedJournal: ReconciliationEvidenceStateDto,
)

data class ReconciliationCaseTimelineEventDto(
    val timestamp: LocalDateTime?,
    val source: String,
    val event: String,
    val evidenceState: String,
    val detail: String,
)

data class ReconciliationCaseAuditHistoryDto(
    val id: String,
    val workflowStatus: String,
    val conclusion: String?,
    val comment: String?,
    val assignedTo: String?,
    val changedBy: String?,
    val changedAt: LocalDateTime,
)

data class ReconciliationCaseDetailDto(
    val id: String,
    val parsedSampleId: String,
    val sampleTimestamp: LocalDateTime,
    val analyzerId: String?,
    val analyzerName: String?,
    val barcodeMasked: String,
    val serviceName: String?,
    val category: String?,
    val sourceType: String,
    val caseType: String,
    val sourceStatus: String,
    val reason: String?,
    val workflowStatus: String,
    val conclusion: String?,
    val assignedTo: String?,
    val latestComment: String?,
    val evidence: ReconciliationCaseEvidenceDto,
    val timeline: List<ReconciliationCaseTimelineEventDto>,
    val history: List<ReconciliationCaseAuditHistoryDto>,
)

data class UpdateReconciliationCaseRequest(
    val workflowStatus: String,
    val conclusion: String? = null,
    val assignedTo: String? = null,
    val comment: String? = null,
)
