package lab.dev.med.univ.feature.director.presentation.dto

data class DirectorAssistantQuestionDto(
    val question: String,
    val model: String = "luna",
)

data class DirectorAssistantAnswerDto(
    val answer: String,
    val model: String,
    val evidenceGeneratedAt: String,
    val evidenceScope: List<String>,
    val evidenceSnapshot: DirectorAssistantEvidenceSnapshotDto,
)

data class DirectorAssistantEvidenceSnapshotDto(
    val periodLabel: String,
    val periodFrom: String?,
    val periodTo: String?,
    val registeredServices: Int,
    val sentResults: Int,
    val pendingServices: Int,
    val comparableDays: Int,
    val waitingForDamumedDays: Int,
    val sourceStatus: String,
    val sourceStatusText: String,
    val sourceCoverageLabel: String,
    val discrepancyCandidates: Int,
    val confirmedMappings: Int,
    val suggestedMappings: Int,
    val tat: List<DirectorAssistantTatInsightDto>,
)

data class DirectorAssistantTatInsightDto(
    val service: String,
    val medianMinutes: Int,
    val p90Minutes: Int,
    val count: Int,
    val outlierSensitive: Boolean,
)

data class DirectorAssistantHistoryMessageDto(
    val id: String,
    val role: String,
    val content: String,
    val model: String? = null,
    val createdAt: String,
    val evidenceGeneratedAt: String? = null,
    val evidenceScope: List<String> = emptyList(),
    val evidenceSnapshot: DirectorAssistantEvidenceSnapshotDto? = null,
)
