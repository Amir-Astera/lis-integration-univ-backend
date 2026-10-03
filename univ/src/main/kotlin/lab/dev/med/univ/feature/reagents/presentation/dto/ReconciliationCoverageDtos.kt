package lab.dev.med.univ.feature.reagents.presentation.dto

import java.time.LocalDate

data class ReconciliationCoverageDayDto(
    val date: LocalDate,
    val analyzerId: String?,
    val analyzerEventCount: Int,
    val analyzerXmlSnapshotCount: Int,
    val damumedFactCount: Int,
    val comparisonStatus: String,
    val explanation: String,
)
