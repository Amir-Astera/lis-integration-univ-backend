package lab.dev.med.univ.feature.reporting.domain.models

import java.time.LocalDateTime

enum class DamumedReportSourceMode {
    MANUAL,
    DAMUMED_API,
}

enum class DamumedReportParseStatus {
    PENDING,
    PROCESSING,
    PARSED,
    FAILED,
}

enum class DamumedReportNormalizationStatus {
    PENDING,
    PROCESSING,
    NORMALIZED,
    FAILED,
    SUPERSEDED,
}

enum class DamumedLabReportKind(
    val displayName: String,
    val storageDirectory: String,
) {
    REFERRAL_REGISTRATION_JOURNAL(
        displayName = "ЖУРНАЛ регистрации лабораторных направлений",
        storageDirectory = "referral-registration-journal",
    ),
    COMPLETED_LAB_STUDIES_JOURNAL(
        displayName = "ЖУРНАЛ регистрации выполненных лабораторных исследований",
        storageDirectory = "completed-lab-studies-journal",
    ),
    REJECT_LOG(
        displayName = "Бракеражный журнал",
        storageDirectory = "reject-log",
    ),
    POSITIVE_RESULTS_JOURNAL(
        displayName = "Журнал исследований с положительным результатом",
        storageDirectory = "positive-results-journal",
    ),
    CONSUMABLE_COST_QUANTITY(
        displayName = "Количественный отчет по затратам расходных материалов лаборатории",
        storageDirectory = "consumable-cost-quantity",
    ),
    LAB_EXPENSE_ESTIMATE_QUANTITY(
        displayName = "Количественный отчет по оценке расходов лаборатории",
        storageDirectory = "lab-expense-estimate-quantity",
    ),
    GOBMP_COMPLETED_SERVICES(
        displayName = "Отчет Выполненные услуги по ГОБМП, ГОБМП-2",
        storageDirectory = "gobmp-completed-services",
    ),
    WORKPLACE_COMPLETED_STUDIES(
        displayName = "Отчет по выполненным исследованиям на рабочих местах",
        storageDirectory = "workplace-completed-studies",
    ),
    REFERRAL_COUNT_BY_MATERIAL(
        displayName = "Отчет по количеству направлений на лабораторные исследования",
        storageDirectory = "referral-count-by-material",
    ),
    EMPLOYEE_COMPLETED_STUDIES_SUMMARY(
        displayName = "Отчёт по выполненным лабораторным исследованиям",
        storageDirectory = "employee-completed-studies-summary",
    ),
    CONSUMABLE_COST_LIST(
        displayName = "Списочный отчет по затратам расходных материалов лаборатории",
        storageDirectory = "consumable-cost-list",
    ),
}

data class DamumedReportSourceSettings(
    val id: String = DEFAULT_ID,
    val mode: DamumedReportSourceMode = DamumedReportSourceMode.MANUAL,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val updatedBy: String? = null,
    val version: Long? = null,
) {
    companion object {
        const val DEFAULT_ID: String = "damumed-lab-report-source"
    }
}

data class DamumedReportUpload(
    val id: String,
    val reportKind: DamumedLabReportKind,
    val sourceMode: DamumedReportSourceMode,
    val originalFileName: String,
    val storedFileName: String,
    val storagePath: String,
    val format: String,
    val contentType: String?,
    val checksumSha256: String,
    val sizeBytes: Long,
    val uploadedAt: LocalDateTime = LocalDateTime.now(),
    val uploadedBy: String? = null,
    val parseStatus: DamumedReportParseStatus = DamumedReportParseStatus.PENDING,
    val parseStartedAt: LocalDateTime? = null,
    val parseCompletedAt: LocalDateTime? = null,
    val parseErrorMessage: String? = null,
    val parsedSheetCount: Int = 0,
    val parsedRowCount: Int = 0,
    val parsedCellCount: Int = 0,
    val parsedMergedRegionCount: Int = 0,
    val detectedReportTitle: String? = null,
    val detectedPeriodText: String? = null,
    val normalizationStatus: DamumedReportNormalizationStatus = DamumedReportNormalizationStatus.PENDING,
    val normalizationStartedAt: LocalDateTime? = null,
    val normalizationCompletedAt: LocalDateTime? = null,
    val normalizationErrorMessage: String? = null,
    val normalizedSectionCount: Int = 0,
    val normalizedFactCount: Int = 0,
    val normalizedDimensionCount: Int = 0,
    val version: Long? = null,
)
