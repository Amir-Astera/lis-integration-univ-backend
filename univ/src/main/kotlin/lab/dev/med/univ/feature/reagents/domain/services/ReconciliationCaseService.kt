package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reagents.data.entity.AnalyzerLogUploadEntity
import lab.dev.med.univ.feature.reagents.data.entity.ParsedAnalyzerSampleEntity
import lab.dev.med.univ.feature.reagents.data.entity.ReconciliationCaseAuditEntity
import lab.dev.med.univ.feature.reagents.data.entity.ReconciliationCaseAuditHistoryEntity
import lab.dev.med.univ.feature.reagents.data.entity.SampleReconciliationEntity
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerLogUploadRepository
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerRepository
import lab.dev.med.univ.feature.reagents.data.repository.ParsedAnalyzerSampleRepository
import lab.dev.med.univ.feature.reagents.data.repository.ReconciliationCaseAuditHistoryRepository
import lab.dev.med.univ.feature.reagents.data.repository.ReconciliationCaseAuditRepository
import lab.dev.med.univ.feature.reagents.data.repository.SampleReconciliationRepository
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogSourceType
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import lab.dev.med.univ.feature.reagents.presentation.dto.ReconciliationCaseAuditHistoryDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReconciliationCaseDetailDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReconciliationCaseEvidenceDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReconciliationCaseListItemDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReconciliationCasePageDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReconciliationCaseTimelineEventDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReconciliationEvidenceStateDto
import lab.dev.med.univ.feature.reagents.presentation.dto.UpdateReconciliationCaseRequest
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class ReconciliationCaseService(
    private val sampleReconciliationRepository: SampleReconciliationRepository,
    private val parsedSampleRepository: ParsedAnalyzerSampleRepository,
    private val analyzerRepository: AnalyzerRepository,
    private val analyzerLogUploadRepository: AnalyzerLogUploadRepository,
    private val auditRepository: ReconciliationCaseAuditRepository,
    private val auditHistoryRepository: ReconciliationCaseAuditHistoryRepository,
) {
    suspend fun listCases(
        from: LocalDate,
        to: LocalDate,
        analyzerId: String?,
        page: Int,
        size: Int,
    ): ReconciliationCasePageDto {
        val effectivePage = page.coerceAtLeast(0)
        val effectiveSize = size.coerceIn(1, 100)
        val rows = sampleReconciliationRepository.findInvestigationCases(
            from = from,
            to = to,
            analyzerId = analyzerId,
            pageSize = effectiveSize,
            offset = effectivePage * effectiveSize,
        ).toList()

        val result = rows.mapNotNull { reconciliation ->
            val sample = parsedSampleRepository.findById(reconciliation.parsedSampleId) ?: return@mapNotNull null
            val audit = findOrCreateCase(sample.id)
            val analyzerName = sample.analyzerId?.let { analyzerRepository.findById(it)?.name }
            ReconciliationCaseListItemDto(
                id = audit.id,
                parsedSampleId = sample.id,
                sampleDate = reconciliation.sampleDate,
                sampleTimestamp = sample.sampleTimestamp,
                analyzerId = sample.analyzerId,
                analyzerName = analyzerName,
                barcodeMasked = displayBarcode(sample.barcode),
                serviceName = reconciliation.serviceNameCanonical ?: sample.serviceName,
                category = reconciliation.category,
                caseType = resolveCaseType(sample, reconciliation),
                sourceStatus = sourceStatus(reconciliation),
                workflowStatus = audit.workflowStatus,
                conclusion = audit.conclusion,
                assignedTo = audit.assignedTo,
                updatedAt = audit.updatedAt,
            )
        }
        val total = sampleReconciliationRepository.countInvestigationCases(from, to, analyzerId)
        return ReconciliationCasePageDto(
            rows = result,
            total = total,
            page = effectivePage,
            size = effectiveSize,
            totalPages = ((total + effectiveSize - 1) / effectiveSize).toInt(),
        )
    }

    suspend fun getCase(caseId: String): ReconciliationCaseDetailDto {
        val audit = auditRepository.findById(caseId)
            ?: throw NoSuchElementException("Случай расследования не найден")
        val reconciliation = sampleReconciliationRepository.findByParsedSampleId(audit.parsedSampleId)
            ?: throw IllegalArgumentException("Этот случай больше не требует автоматической сверки")
        val sample = parsedSampleRepository.findById(audit.parsedSampleId)
            ?: throw NoSuchElementException("Исходная запись анализатора не найдена")
        val history = auditHistoryRepository.findAllByCaseIdOrderByChangedAtDesc(audit.id)
            .toList()
            .map { entry -> entry.toAuditHistoryDto() }
        val analyzerName = sample.analyzerId?.let { analyzerRepository.findById(it)?.name }
        val upload = analyzerLogUploadRepository.findById(sample.logUploadId)

        return ReconciliationCaseDetailDto(
            id = audit.id,
            parsedSampleId = sample.id,
            sampleTimestamp = sample.sampleTimestamp,
            analyzerId = sample.analyzerId,
            analyzerName = analyzerName,
            barcodeMasked = displayBarcode(sample.barcode),
            serviceName = reconciliation.serviceNameCanonical ?: sample.serviceName,
            category = reconciliation.category,
            sourceType = upload?.sourceType?.name ?: "UNKNOWN",
            caseType = resolveCaseType(sample, reconciliation),
            sourceStatus = sourceStatus(reconciliation),
            reason = reconciliation.reason ?: sample.classificationReason,
            workflowStatus = audit.workflowStatus,
            conclusion = audit.conclusion,
            assignedTo = audit.assignedTo,
            latestComment = audit.latestComment,
            evidence = buildEvidence(sample, reconciliation, upload),
            timeline = buildTimeline(sample, reconciliation, upload),
            history = history,
        )
    }

    suspend fun updateCase(
        caseId: String,
        request: UpdateReconciliationCaseRequest,
        actor: String?,
    ): ReconciliationCaseDetailDto {
        require(request.workflowStatus in WORKFLOW_STATUSES) {
            "Недопустимый статус обработки случая"
        }
        val previous = auditRepository.findById(caseId)
            ?: throw NoSuchElementException("Случай расследования не найден")
        val sample = parsedSampleRepository.findById(previous.parsedSampleId)
            ?: throw NoSuchElementException("Исходная запись анализатора не найдена")
        val now = LocalDateTime.now()
        val audit = previous.copy(
            workflowStatus = request.workflowStatus,
            conclusion = request.conclusion?.trim()?.takeIf { it.isNotBlank() },
            assignedTo = request.assignedTo?.trim()?.takeIf { it.isNotBlank() },
            latestComment = request.comment?.trim()?.takeIf { it.isNotBlank() },
            updatedAt = now,
            updatedBy = actor,
        )
        val saved = auditRepository.save(audit)
        auditHistoryRepository.save(
            ReconciliationCaseAuditHistoryEntity(
                id = UUID.randomUUID().toString(),
                caseId = saved.id,
                workflowStatus = saved.workflowStatus,
                conclusion = saved.conclusion,
                comment = saved.latestComment,
                assignedTo = saved.assignedTo,
                changedBy = actor,
                changedAt = now,
            )
        )
        return getCase(caseId)
    }

    private fun buildEvidence(
        sample: ParsedAnalyzerSampleEntity,
        reconciliation: SampleReconciliationEntity,
        upload: AnalyzerLogUploadEntity?,
    ): ReconciliationCaseEvidenceDto {
        val orderKnown = sample.hasLisOrder || sample.orderId != null || sample.orderResearchId != null
        val analyzerSource = upload?.sourceType
        return ReconciliationCaseEvidenceDto(
            orderInDriver = ReconciliationEvidenceStateDto(
                state = if (orderKnown) "CONFIRMED" else "UNKNOWN",
                detail = if (orderKnown) {
                    "В драйвере есть идентификатор заказа: ${listOfNotNull(sample.orderId, sample.orderResearchId).joinToString(" / ")}"
                } else {
                    "В сохранённой записи прибора нет надёжного идентификатора заказа."
                },
            ),
            receivedFromAnalyzer = ReconciliationEvidenceStateDto(
                state = if (analyzerSource == AnalyzerLogSourceType.APPLOGS) "CONFIRMED" else "CONTEXTUAL",
                detail = if (analyzerSource == AnalyzerLogSourceType.APPLOGS) {
                    "Образец получен из событийного журнала прибора."
                } else {
                    "Источник является снимком errors.xml без надёжного времени отдельного события."
                },
            ),
            savedByDriver = ReconciliationEvidenceStateDto(
                state = "UNKNOWN",
                detail = "Отдельное событие Saving / Save string не сохраняется в текущей модели; требуется первичный лог.",
            ),
            presentInCompletedJournal = ReconciliationEvidenceStateDto(
                state = if (sample.classification == SampleClassification.LEGITIMATE) "CONFIRMED" else "UNKNOWN",
                detail = if (sample.classification == SampleClassification.LEGITIMATE) {
                    "Запись была подтверждена индексом журнала выполненных исследований."
                } else {
                    "Нет подтверждённой построчной связи с журналом выполненных исследований; выгрузка может отставать."
                },
            ),
        )
    }

    private fun buildTimeline(
        sample: ParsedAnalyzerSampleEntity,
        reconciliation: SampleReconciliationEntity,
        upload: AnalyzerLogUploadEntity?,
    ): List<ReconciliationCaseTimelineEventDto> = buildList {
        add(
            ReconciliationCaseTimelineEventDto(
                timestamp = sample.sampleTimestamp,
                source = when (upload?.sourceType) {
                    AnalyzerLogSourceType.APPLOGS -> "Журнал анализатора"
                    AnalyzerLogSourceType.ERRORS_XML -> "errors.xml"
                    else -> "Источник анализатора"
                },
                event = if (upload?.sourceType == AnalyzerLogSourceType.ERRORS_XML) "Контекстный XML-снимок" else "Образец разобран из журнала",
                evidenceState = if (upload?.sourceType == AnalyzerLogSourceType.ERRORS_XML) "CONTEXTUAL" else "CONFIRMED",
                detail = "Штрих-код: ${displayBarcode(sample.barcode)}",
            )
        )
        if (sample.orderId != null || sample.orderResearchId != null) {
            add(
                ReconciliationCaseTimelineEventDto(
                    timestamp = sample.sampleTimestamp,
                    source = "Интеграционный драйвер",
                    event = "Идентификатор заказа обнаружен",
                    evidenceState = "CONFIRMED",
                    detail = "OrderID: ${sample.orderId ?: "—"} · OrderResearchID: ${sample.orderResearchId ?: "—"}",
                )
            )
        }
        add(
            ReconciliationCaseTimelineEventDto(
                timestamp = reconciliation.reconciledAt,
                source = "Сверка источников",
                event = sourceStatus(reconciliation),
                evidenceState = if (reconciliation.reconciliationStatus == "PENDING_GRACE") "UNKNOWN" else "REVIEW",
                detail = reconciliation.reason ?: "Автоматическое сопоставление завершено.",
            )
        )
    }

    private fun resolveCaseType(
        sample: ParsedAnalyzerSampleEntity,
        reconciliation: SampleReconciliationEntity,
    ): String = when {
        isTechnicalCode(sample.barcode) -> "Технический код прибора"
        sample.classification == SampleClassification.XML_RESULT -> "XML-снимок без времени"
        sample.classification == SampleClassification.ERROR ||
            sample.classificationReason.orEmpty().contains("No result", ignoreCase = true) -> "Параметр не получен от прибора"
        reconciliation.reconciliationStatus == "PENDING_GRACE" -> "Задержка выгрузки"
        else -> "Расхождение требует аудита МИС"
    }

    private fun sourceStatus(reconciliation: SampleReconciliationEntity): String =
        when (reconciliation.reconciliationStatus) {
            "PENDING_GRACE" -> "Источник ожидается"
            "DISCREPANCY" -> "Требует проверки"
            else -> "Подтверждено источниками"
        }

    private fun displayBarcode(barcode: String): String =
        if (isTechnicalCode(barcode)) "Технический код $barcode"
        else barcode.takeLast(4).padStart(barcode.length.coerceAtMost(8), '•')

    private fun isTechnicalCode(value: String): Boolean = value.trim().matches(Regex("\\d{1,6}"))

    private suspend fun findOrCreateCase(parsedSampleId: String): ReconciliationCaseAuditEntity {
        val existing = auditRepository.findByParsedSampleId(parsedSampleId)
        if (existing != null) return existing

        val now = LocalDateTime.now()
        val created = auditRepository.save(
            ReconciliationCaseAuditEntity(
                id = UUID.randomUUID().toString(),
                parsedSampleId = parsedSampleId,
                workflowStatus = WORKFLOW_OPEN,
                createdAt = now,
                updatedAt = now,
                createdBy = "system",
                updatedBy = "system",
            )
        )
        auditHistoryRepository.save(
            ReconciliationCaseAuditHistoryEntity(
                id = UUID.randomUUID().toString(),
                caseId = created.id,
                workflowStatus = WORKFLOW_OPEN,
                comment = "Случай создан автоматически по результату сопоставления источников.",
                changedBy = "system",
                changedAt = now,
            )
        )
        return created
    }

    private fun ReconciliationCaseAuditHistoryEntity.toAuditHistoryDto() = ReconciliationCaseAuditHistoryDto(
        id = id,
        workflowStatus = workflowStatus,
        conclusion = conclusion,
        comment = comment,
        assignedTo = assignedTo,
        changedBy = changedBy,
        changedAt = changedAt,
    )

    companion object {
        private const val WORKFLOW_OPEN = "OPEN"
        private val WORKFLOW_STATUSES = setOf("OPEN", "IN_REVIEW", "CLOSED")
    }
}
