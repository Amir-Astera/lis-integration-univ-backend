package lab.dev.med.univ.feature.reagents.data.entity

import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerReagentLink
import lab.dev.med.univ.feature.reagents.domain.models.ReagentUsageRole

fun AnalyzerReagentLinkEntity.toModel() = AnalyzerReagentLink(
    id = id,
    analyzerId = analyzerId,
    reagentInventoryId = reagentInventoryId,
    usageRole = runCatching { ReagentUsageRole.valueOf(usageRole) }.getOrDefault(ReagentUsageRole.MAIN),
    normReagentName = normReagentName,
    estimatedDailyMl = estimatedDailyMl,
    estimatedDailyUnits = estimatedDailyUnits,
    isActive = isActive,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    createdBy = createdBy,
    version = version,
)

fun AnalyzerReagentLink.toEntity() = AnalyzerReagentLinkEntity(
    id = id,
    analyzerId = analyzerId,
    reagentInventoryId = reagentInventoryId,
    usageRole = usageRole.name,
    normReagentName = normReagentName,
    estimatedDailyMl = estimatedDailyMl,
    estimatedDailyUnits = estimatedDailyUnits,
    isActive = isActive,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    createdBy = createdBy,
    version = version,
)
