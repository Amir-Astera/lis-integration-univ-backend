package lab.dev.med.univ.feature.reporting.domain.models

data class DamumedOperationalOverview(
    val generatedAt: String,
    val sourceReportName: String?,
    val uploads: DamumedOperationalUploadSummary,
    val dashboard: DamumedOperationalDashboardSummary,
    val registry: DamumedOperationalRegistrySummary,
    val worklists: DamumedOperationalWorklistsSummary,
    val equipment: DamumedOperationalEquipmentSummary,
    val warehouse: DamumedOperationalWarehouseSummary,
    val reports: DamumedOperationalReportsSummary,
    val relations: DamumedOperationalRelationsSummary,
    val dailyStats: List<DamumedOperationalDailyStat>,
)

data class DamumedOperationalUploadSummary(
    val totalUploads: Int,
    val normalizedUploads: Int,
    val latestUploadId: String?,
    val latestUploadedAt: String?,
)

data class DamumedOperationalDashboardSummary(
    val day: DamumedOperationalDashboardPeriodSummary,
    val week: DamumedOperationalDashboardPeriodSummary,
    val month: DamumedOperationalDashboardPeriodSummary,
    val criticalSamples: Int,
    val samplesTotal: Int,
    val validationQueue: Int,
    val analyzerLoadPercent: Int,
    val departmentLoads: List<DamumedNamedMetric>,
    val tatByService: List<DamumedOperationalTatItem>,
    val workplaceItems: List<DamumedOperationalStatusItem>,
    val analyzerStatuses: List<DamumedOperationalStatusItem>,
    val stockAlerts: List<DamumedOperationalStockItem>,
)

data class DamumedOperationalDashboardPeriodSummary(
    val label: String,
    val researchCount: Int,
    val patientCount: Int,
    val departmentCount: Int,
    val sentResultsCount: Int,
    val materialsCount: Int,
    val serviceCostTotal: Double,
    val tatByService: List<DamumedOperationalTatItem>,
    val workplaceItems: List<DamumedOperationalStatusItem>,
    val dailyStats: List<DamumedOperationalDailyStat>,
)

data class DamumedOperationalRegistrySummary(
    val newArrivals: Int,
    val pendingRegistration: Int,
    val labelingErrors: Int,
    val averageAcceptanceMinutes: Int,
    val queue: List<DamumedOperationalQueueItem>,
    val routing: List<DamumedOperationalQueueItem>,
)

data class DamumedOperationalWorklistsSummary(
    val activeSheets: Int,
    val waitingSheets: Int,
    val inProgressSheets: Int,
    val completedSheets: Int,
    val queue: List<DamumedOperationalQueueItem>,
    val departmentLoads: List<DamumedNamedMetric>,
    val slaItems: List<DamumedOperationalSlaItem>,
)

data class DamumedOperationalEquipmentSummary(
    val runningCount: Int,
    val calibrationCount: Int,
    val maintenanceCount: Int,
    val readinessPercent: Int,
    val analyzers: List<DamumedOperationalStatusItem>,
    val maintenanceItems: List<DamumedOperationalMaintenanceItem>,
)

data class DamumedOperationalWarehouseSummary(
    val criticalStockCount: Int,
    val activeItemsCount: Int,
    val expiringSoonCount: Int,
    val incomingSupplyCount: Int,
    val stockItems: List<DamumedOperationalStockItem>,
    val supplies: List<DamumedOperationalStatusItem>,
    val categoryHealth: List<DamumedOperationalCategoryHealthItem>,
)

data class DamumedOperationalReportsSummary(
    val latestPeriodText: String?,
    val uploadsByKind: List<DamumedNamedMetric>,
    val normalizedFactsByKind: List<DamumedNamedMetric>,
)

data class DamumedOperationalRelationsSummary(
    val services: List<DamumedOperationalRelationItem>,
    val materials: List<DamumedOperationalRelationItem>,
    val consumables: List<DamumedOperationalRelationItem>,
    val reagents: List<DamumedOperationalRelationItem>,
    val analyzers: List<DamumedOperationalRelationItem>,
)

data class DamumedNamedMetric(
    val name: String,
    val value: Double,
)

data class DamumedOperationalStatusItem(
    val name: String,
    val secondaryText: String?,
    val status: String,
    val numericValue: Double? = null,
)

data class DamumedOperationalTatItem(
    val service: String,
    val averageMinutes: Int,
    val averageDurationText: String,
    val count: Int,
)

data class DamumedOperationalStockItem(
    val name: String,
    val analyzer: String?,
    val balanceText: String,
    val expiryText: String?,
    val critical: Boolean,
)

data class DamumedOperationalQueueItem(
    val primaryText: String,
    val secondaryText: String?,
    val code: String?,
    val status: String,
)

data class DamumedOperationalSlaItem(
    val category: String,
    val targetText: String,
    val actualText: String,
    val deviationText: String,
    val critical: Boolean,
)

data class DamumedOperationalMaintenanceItem(
    val analyzer: String,
    val procedureName: String,
    val dueText: String,
    val status: String,
    val critical: Boolean,
)

data class DamumedOperationalCategoryHealthItem(
    val category: String,
    val normPercent: Int,
    val actualPercent: Int,
    val status: String,
    val critical: Boolean,
)

data class DamumedOperationalRelationItem(
    val name: String,
    val amount: Double,
    val relatedNames: List<String>,
)

data class DamumedOperationalDailyStat(
    val date: String,
    val count: Int,
)
