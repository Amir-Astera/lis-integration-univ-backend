package lab.dev.med.univ.feature.reporting.domain.models

// Детальный отчёт по рабочим местам: Рабочее место → Услуга → Отделения
data class DamumedWorkplaceDetailReport(
    val generatedAt: java.time.Instant,
    val periodLabel: String?,
    val sourceUploadId: String,
    val workplaces: List<WorkplaceDetailItem>,
)

data class WorkplaceDetailItem(
    val workplaceName: String,
    val totalCompleted: Double,
    val services: List<WorkplaceServiceDetail>,
)

data class WorkplaceServiceDetail(
    val serviceName: String,
    val totalCompleted: Double,
    val departments: List<WorkplaceServiceDepartmentDetail>,
)

data class WorkplaceServiceDepartmentDetail(
    val departmentName: String,
    val departmentGroup: String?,
    val completedCount: Double,
)
