package project.gigienist_reports.feature.report.domain.models

import project.gigienist_reports.core.config.enums.ReportType
import java.time.LocalDateTime

data class Report(
    val id: String,
    val reportCode: String,
    val reportName: String,
    val type: ReportType,
    val version: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
