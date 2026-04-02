package lab.dev.med.univ.feature.reagents.presentation.dto

import lab.dev.med.univ.feature.reagents.domain.models.ReagentConsumptionReport
import java.time.LocalDate
import java.time.LocalDateTime

data class GenerateReagentConsumptionReportRequestDto(
    val analyzerId: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
)

data class ReagentConsumptionReportResponseDto(
    val id: String,
    val analyzerId: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val legitimateTestCount: Int,
    val legitimateReagentConsumptionJson: String?,
    val legitimateCostTenge: Double,
    val serviceOperationsJson: String?,
    val serviceReagentConsumptionJson: String?,
    val serviceCostTenge: Double,
    val suspiciousTestCount: Int,
    val rerunTestCount: Int,
    val washTestCount: Int,
    val unauthorizedReagentConsumptionJson: String?,
    val unauthorizedCostTenge: Double,
    val inventoryStartJson: String?,
    val inventoryReceivedJson: String?,
    val inventoryEndExpectedJson: String?,
    val inventoryEndActualJson: String?,
    val discrepancyJson: String?,
    val discrepancyTotalTenge: Double,
    val generatedAt: LocalDateTime,
    val generatedBy: String?,
)

fun ReagentConsumptionReport.toResponseDto() = ReagentConsumptionReportResponseDto(
    id = id,
    analyzerId = analyzerId,
    periodStart = periodStart,
    periodEnd = periodEnd,
    legitimateTestCount = legitimateTestCount,
    legitimateReagentConsumptionJson = legitimateReagentConsumptionJson,
    legitimateCostTenge = legitimateCostTenge,
    serviceOperationsJson = serviceOperationsJson,
    serviceReagentConsumptionJson = serviceReagentConsumptionJson,
    serviceCostTenge = serviceCostTenge,
    suspiciousTestCount = suspiciousTestCount,
    rerunTestCount = rerunTestCount,
    washTestCount = washTestCount,
    unauthorizedReagentConsumptionJson = unauthorizedReagentConsumptionJson,
    unauthorizedCostTenge = unauthorizedCostTenge,
    inventoryStartJson = inventoryStartJson,
    inventoryReceivedJson = inventoryReceivedJson,
    inventoryEndExpectedJson = inventoryEndExpectedJson,
    inventoryEndActualJson = inventoryEndActualJson,
    discrepancyJson = discrepancyJson,
    discrepancyTotalTenge = discrepancyTotalTenge,
    generatedAt = generatedAt,
    generatedBy = generatedBy,
)
