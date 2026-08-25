package lab.dev.med.univ.feature.reagents.data.entity

import lab.dev.med.univ.feature.reagents.domain.models.ReconciliationDailySummary
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Table("reconciliation_daily_summary")
data class ReconciliationSummaryEntity(
    @Id val id: String,
    val summaryDate: LocalDate,
    val analyzerId: String? = null,
    val serviceCatalogId: String? = null,
    val serviceNameCanonical: String = "",
    val category: String? = null,
    val logsCount: Int = 0,
    val lisCount: Int = 0,
    val reconciledCount: Int = 0,
    val discrepancyCount: Int = 0,
    val pendingGraceCount: Int = 0,
    val washTestCount: Int = 0,
    val estimatedWastedCostTenge: BigDecimal = BigDecimal.ZERO,
    val lisPricePerTestTenge: BigDecimal? = null,
    val lastRebuiltAt: LocalDateTime = LocalDateTime.now(),
    @Version val version: Long? = null,
)

fun ReconciliationSummaryEntity.toModel() = ReconciliationDailySummary(
    id                        = id,
    summaryDate               = summaryDate,
    analyzerId                = analyzerId,
    serviceCatalogId          = serviceCatalogId,
    serviceNameCanonical      = serviceNameCanonical,
    category                  = category,
    logsCount                 = logsCount,
    lisCount                  = lisCount,
    reconciledCount           = reconciledCount,
    discrepancyCount          = discrepancyCount,
    pendingGraceCount         = pendingGraceCount,
    washTestCount             = washTestCount,
    estimatedWastedCostTenge  = estimatedWastedCostTenge,
    lisPricePerTestTenge      = lisPricePerTestTenge,
    lastRebuiltAt             = lastRebuiltAt,
    version                   = version,
)

fun ReconciliationDailySummary.toEntity() = ReconciliationSummaryEntity(
    id                        = id,
    summaryDate               = summaryDate,
    analyzerId                = analyzerId,
    serviceCatalogId          = serviceCatalogId,
    serviceNameCanonical      = serviceNameCanonical,
    category                  = category,
    logsCount                 = logsCount,
    lisCount                  = lisCount,
    reconciledCount           = reconciledCount,
    discrepancyCount          = discrepancyCount,
    pendingGraceCount         = pendingGraceCount,
    washTestCount             = washTestCount,
    estimatedWastedCostTenge  = estimatedWastedCostTenge,
    lisPricePerTestTenge      = lisPricePerTestTenge,
    lastRebuiltAt             = lastRebuiltAt,
    version                   = version,
)
