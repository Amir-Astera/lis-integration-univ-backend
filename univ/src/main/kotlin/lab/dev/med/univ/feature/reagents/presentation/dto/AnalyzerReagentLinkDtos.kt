package lab.dev.med.univ.feature.reagents.presentation.dto

import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerReagentLink
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerReagentLinkView
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerReagentSummary
import lab.dev.med.univ.feature.reagents.domain.models.CreateAnalyzerReagentLinkRequest
import lab.dev.med.univ.feature.reagents.domain.models.ReagentUsageRole

data class AnalyzerReagentLinkDto(
    val id: String,
    val analyzerId: String,
    val reagentInventoryId: String,
    val usageRole: String,
    val normReagentName: String?,
    val estimatedDailyMl: Double?,
    val estimatedDailyUnits: Int?,
    val isActive: Boolean,
    val notes: String?,
    val createdAt: String,
    val createdBy: String?,
)

data class AnalyzerReagentLinkViewDto(
    val link: AnalyzerReagentLinkDto,
    val analyzerName: String,
    val reagentName: String,
    val reagentLotNumber: String?,
    val reagentStatus: String?,
    val totalVolumeMl: Double?,
    val totalUnits: Int?,
    val unitType: String,
)

data class AnalyzerReagentSummaryDto(
    val analyzerId: String,
    val analyzerName: String,
    val links: List<AnalyzerReagentLinkViewDto>,
    val totalActiveLinks: Int,
)

data class CreateAnalyzerReagentLinkRequestDto(
    val reagentInventoryId: String,
    val usageRole: String = "MAIN",
    val normReagentName: String? = null,
    val estimatedDailyMl: Double? = null,
    val estimatedDailyUnits: Int? = null,
    val notes: String? = null,
)

fun AnalyzerReagentLink.toDto() = AnalyzerReagentLinkDto(
    id = id,
    analyzerId = analyzerId,
    reagentInventoryId = reagentInventoryId,
    usageRole = usageRole.name,
    normReagentName = normReagentName,
    estimatedDailyMl = estimatedDailyMl,
    estimatedDailyUnits = estimatedDailyUnits,
    isActive = isActive,
    notes = notes,
    createdAt = createdAt.toString(),
    createdBy = createdBy,
)

fun AnalyzerReagentLinkView.toDto() = AnalyzerReagentLinkViewDto(
    link = link.toDto(),
    analyzerName = analyzerName,
    reagentName = reagentName,
    reagentLotNumber = reagentLotNumber,
    reagentStatus = reagentStatus,
    totalVolumeMl = totalVolumeMl,
    totalUnits = totalUnits,
    unitType = unitType,
)

fun AnalyzerReagentSummary.toDto() = AnalyzerReagentSummaryDto(
    analyzerId = analyzerId,
    analyzerName = analyzerName,
    links = links.map { it.toDto() },
    totalActiveLinks = totalActiveLinks,
)

fun CreateAnalyzerReagentLinkRequestDto.toDomain(analyzerId: String) = CreateAnalyzerReagentLinkRequest(
    analyzerId = analyzerId,
    reagentInventoryId = reagentInventoryId,
    usageRole = runCatching { ReagentUsageRole.valueOf(usageRole) }.getOrDefault(ReagentUsageRole.MAIN),
    normReagentName = normReagentName,
    estimatedDailyMl = estimatedDailyMl,
    estimatedDailyUnits = estimatedDailyUnits,
    notes = notes,
)
