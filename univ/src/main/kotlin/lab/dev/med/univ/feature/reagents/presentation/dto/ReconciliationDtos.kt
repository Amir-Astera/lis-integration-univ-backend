package lab.dev.med.univ.feature.reagents.presentation.dto

import lab.dev.med.univ.feature.reagents.domain.models.ReconciliationAnalyzerSummary
import lab.dev.med.univ.feature.reagents.domain.models.ReconciliationDailyPoint
import lab.dev.med.univ.feature.reagents.domain.models.ReconciliationKpiSummary
import lab.dev.med.univ.feature.reagents.domain.models.ReconciliationServiceRow
import lab.dev.med.univ.feature.reagents.domain.models.SampleReconciliation
import lab.dev.med.univ.feature.reagents.domain.services.DrillDownPage
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class ReconciliationKpiSummaryDto(
    val periodFrom: LocalDate,
    val periodTo: LocalDate,
    val totalLogsCount: Int,
    val totalLisCount: Int,
    val totalReconciledCount: Int,
    val totalDiscrepancyCount: Int,
    val totalPendingGraceCount: Int,
    val totalWashTestCount: Int,
    val totalWastedCostTenge: BigDecimal,
    val discrepancyRatePct: Double,
    val analyzerCount: Int,
    val affectedServiceCount: Int,
)

fun ReconciliationKpiSummary.toDto() = ReconciliationKpiSummaryDto(
    periodFrom             = periodFrom,
    periodTo               = periodTo,
    totalLogsCount         = totalLogsCount,
    totalLisCount          = totalLisCount,
    totalReconciledCount   = totalReconciledCount,
    totalDiscrepancyCount  = totalDiscrepancyCount,
    totalPendingGraceCount = totalPendingGraceCount,
    totalWashTestCount     = totalWashTestCount,
    totalWastedCostTenge   = totalWastedCostTenge,
    discrepancyRatePct     = (discrepancyRate * 100).toBigDecimal().setScale(1, java.math.RoundingMode.HALF_UP).toDouble(),
    analyzerCount          = analyzerCount,
    affectedServiceCount   = affectedServiceCount,
)

data class ReconciliationDailyPointDto(
    val date: LocalDate,
    val logsCount: Int,
    val lisCount: Int,
    val discrepancyCount: Int,
    val pendingGraceCount: Int,
    val wastedCostTenge: BigDecimal,
)

fun ReconciliationDailyPoint.toDto() = ReconciliationDailyPointDto(
    date              = date,
    logsCount         = logsCount,
    lisCount          = lisCount,
    discrepancyCount  = discrepancyCount,
    pendingGraceCount = pendingGraceCount,
    wastedCostTenge   = wastedCostTenge,
)

data class ReconciliationAnalyzerSummaryDto(
    val analyzerId: String?,
    val analyzerName: String?,
    val logsCount: Int,
    val lisCount: Int,
    val discrepancyCount: Int,
    val reconciledCount: Int,
    val discrepancyRatePct: Double,
    val wastedCostTenge: BigDecimal,
)

fun ReconciliationAnalyzerSummary.toDto() = ReconciliationAnalyzerSummaryDto(
    analyzerId         = analyzerId,
    analyzerName       = analyzerName,
    logsCount          = logsCount,
    lisCount           = lisCount,
    discrepancyCount   = discrepancyCount,
    reconciledCount    = reconciledCount,
    discrepancyRatePct = (discrepancyRate * 100).toBigDecimal().setScale(1, java.math.RoundingMode.HALF_UP).toDouble(),
    wastedCostTenge    = wastedCostTenge,
)

data class ReconciliationServiceRowDto(
    val serviceCatalogId: String?,
    val serviceNameCanonical: String,
    val category: String?,
    val logsCount: Int,
    val lisCount: Int,
    val discrepancyCount: Int,
    val reconciledCount: Int,
    val pendingGraceCount: Int,
    val discrepancyRatePct: Double,
    val wastedCostTenge: BigDecimal,
    val lisPricePerTestTenge: BigDecimal?,
    val status: String,
)

fun ReconciliationServiceRow.toDto() = ReconciliationServiceRowDto(
    serviceCatalogId     = serviceCatalogId,
    serviceNameCanonical = serviceNameCanonical,
    category             = category,
    logsCount            = logsCount,
    lisCount             = lisCount,
    discrepancyCount     = discrepancyCount,
    reconciledCount      = reconciledCount,
    pendingGraceCount    = pendingGraceCount,
    discrepancyRatePct   = (discrepancyRate * 100).toBigDecimal().setScale(1, java.math.RoundingMode.HALF_UP).toDouble(),
    wastedCostTenge      = wastedCostTenge,
    lisPricePerTestTenge = lisPricePerTestTenge,
    status               = status.name,
)

/** Single per-sample reconciliation row for drill-down table. */
data class SampleReconciliationDto(
    val id: String,
    val parsedSampleId: String,
    val sampleDate: LocalDate,
    val analyzerId: String?,
    val serviceCatalogId: String?,
    val serviceNameRaw: String?,
    val serviceNameCanonical: String?,
    val category: String?,
    val reconciliationStatus: String,
    val reason: String?,
    val graceHours: Int,
    val graceDeadlineAt: LocalDateTime?,
    val matchConfidence: String?,
    val lisPricePerTestTenge: BigDecimal?,
    val estimatedWasteTenge: BigDecimal,
    val reconciledAt: LocalDateTime,
)

fun SampleReconciliation.toDto() = SampleReconciliationDto(
    id                    = id,
    parsedSampleId        = parsedSampleId,
    sampleDate            = sampleDate,
    analyzerId            = analyzerId,
    serviceCatalogId      = serviceCatalogId,
    serviceNameRaw        = serviceNameRaw,
    serviceNameCanonical  = serviceNameCanonical,
    category              = category,
    reconciliationStatus  = reconciliationStatus.name,
    reason                = reason,
    graceHours            = graceHours,
    graceDeadlineAt       = graceDeadlineAt,
    matchConfidence       = matchConfidence?.name,
    lisPricePerTestTenge  = lisPricePerTestTenge,
    estimatedWasteTenge   = estimatedWasteTenge,
    reconciledAt          = reconciledAt,
)

/** Paginated drill-down response. */
data class DrillDownPageDto(
    val rows: List<SampleReconciliationDto>,
    val total: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int,
)

fun DrillDownPage.toDto(): DrillDownPageDto = DrillDownPageDto(
    rows       = rows.map { it.toDto() },
    total      = total,
    page       = page,
    size       = size,
    totalPages = if (size <= 0) 0 else ((total + size - 1) / size).toInt(),
)
