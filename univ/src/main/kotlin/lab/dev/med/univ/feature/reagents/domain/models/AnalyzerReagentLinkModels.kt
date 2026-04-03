package lab.dev.med.univ.feature.reagents.domain.models

import java.time.LocalDateTime

enum class ReagentUsageRole {
    MAIN,       // Primary working reagent
    DILUENT,    // Diluent / saline
    WASH,       // Cleaning / wash solution
    CALIBRATOR, // Calibration material
    CONTROL,    // QC control material
    OTHER,
}

data class AnalyzerReagentLink(
    val id: String,
    val analyzerId: String,
    val reagentInventoryId: String,
    val usageRole: ReagentUsageRole = ReagentUsageRole.MAIN,
    val normReagentName: String? = null,
    val estimatedDailyMl: Double? = null,
    val estimatedDailyUnits: Int? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String? = null,
    val version: Long? = null,
)

/** View model joining link with inventory details. */
data class AnalyzerReagentLinkView(
    val link: AnalyzerReagentLink,
    val analyzerName: String,
    val reagentName: String,
    val reagentLotNumber: String?,
    val reagentStatus: String?,
    val totalVolumeMl: Double?,
    val totalUnits: Int?,
    val unitType: String,
)

data class CreateAnalyzerReagentLinkRequest(
    val analyzerId: String,
    val reagentInventoryId: String,
    val usageRole: ReagentUsageRole = ReagentUsageRole.MAIN,
    val normReagentName: String? = null,
    val estimatedDailyMl: Double? = null,
    val estimatedDailyUnits: Int? = null,
    val notes: String? = null,
)

/** Summary of reagents assigned to an analyzer. */
data class AnalyzerReagentSummary(
    val analyzerId: String,
    val analyzerName: String,
    val links: List<AnalyzerReagentLinkView>,
    val totalActiveLinks: Int,
)
