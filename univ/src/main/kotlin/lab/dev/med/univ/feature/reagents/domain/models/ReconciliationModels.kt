package lab.dev.med.univ.feature.reagents.domain.models

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Status of a single reconciliation row (per date/analyzer/service).
 * Used to drive UI color-coding and filtering.
 */
enum class ReconciliationStatus {
    /** logs_count == reconciled_count — all analyzer runs confirmed in LIS */
    CLEAN,
    /** Both sources have comparable facts, but their evidence still requires review. */
    DISCREPANCY,
    /** All unreconciled samples are within the grace window (immunology) */
    PENDING_GRACE,
    /** No analyzer data for this service on this date */
    NO_DATA,
    /** Analyzer activity exists but no Damumed source facts cover the same period. */
    NO_COMPARABLE_EVIDENCE,
}

/**
 * One materialized reconciliation row for (date, analyzer, service).
 * Corresponds to reconciliation_daily_summary table.
 */
data class ReconciliationDailySummary(
    val id: String,
    val summaryDate: LocalDate,
    val analyzerId: String?,
    val serviceCatalogId: String?,
    val serviceNameCanonical: String,
    val category: String?,
    val logsCount: Int,
    val lisCount: Int,
    val reconciledCount: Int,
    val discrepancyCount: Int,
    val pendingGraceCount: Int,
    val noComparableEvidenceCount: Int,
    val washTestCount: Int,
    val estimatedWastedCostTenge: BigDecimal,
    val lisPricePerTestTenge: BigDecimal?,
    val lastRebuiltAt: LocalDateTime,
    val version: Long? = null,
) {
    val status: ReconciliationStatus get() = when {
        logsCount == 0                             -> ReconciliationStatus.NO_DATA
        discrepancyCount > 0                       -> ReconciliationStatus.DISCREPANCY
        noComparableEvidenceCount > 0              -> ReconciliationStatus.NO_COMPARABLE_EVIDENCE
        pendingGraceCount > 0 && discrepancyCount == 0 -> ReconciliationStatus.PENDING_GRACE
        else                                       -> ReconciliationStatus.CLEAN
    }
}

// ─── Dashboard aggregate DTOs (returned by ReconciliationSummaryService) ──────

/** Top-level KPI card data for the reconciliation dashboard. */
data class ReconciliationKpiSummary(
    val periodFrom: LocalDate,
    val periodTo: LocalDate,
    val totalLogsCount: Int,
    val totalLisCount: Int,
    val totalReconciledCount: Int,
    val totalDiscrepancyCount: Int,
    val totalPendingGraceCount: Int,
    val totalNoComparableEvidenceCount: Int,
    val totalWashTestCount: Int,
    val exactDatedReferralLinkCount: Int,
    val totalWastedCostTenge: BigDecimal,
    val discrepancyRate: Double,        // discrepancyCount / logsCount %, [0..1]
    val analyzerCount: Int,
    val affectedServiceCount: Int,
)

/** Daily trend data point for time-series chart. */
data class ReconciliationDailyPoint(
    val date: LocalDate,
    val logsCount: Int,
    val lisCount: Int,
    val discrepancyCount: Int,
    val pendingGraceCount: Int,
    val noComparableEvidenceCount: Int,
    val wastedCostTenge: BigDecimal,
)

data class ReconciliationCoverageDay(
    val date: LocalDate,
    val analyzerId: String?,
    val analyzerEventCount: Int,
    val analyzerXmlSnapshotCount: Int,
    val damumedFactCount: Int,
    val comparisonStatus: String,
    val explanation: String,
)

/** Per-analyzer summary for leaderboard widget. */
data class ReconciliationAnalyzerSummary(
    val analyzerId: String?,
    val analyzerName: String?,
    val logsCount: Int,
    val lisCount: Int,
    val discrepancyCount: Int,
    val noComparableEvidenceCount: Int,
    val reconciledCount: Int,
    val discrepancyRate: Double,
    val wastedCostTenge: BigDecimal,
)

/**
 * Per-sample reconciliation ledger status.
 * Governs drill-down classification: which specific samples caused a discrepancy.
 */
enum class SampleReconciliationStatus {
    /** Sample is confirmed by LIS record (original or upgraded via journal match) */
    LEGITIMATE,
    /** No LIS record AND grace expired (or no grace configured) — reagent waste */
    DISCREPANCY,
    /** No LIS record YET but grace window has not expired (e.g. hepatitis ≤72h) */
    PENDING_GRACE,
    /** No Damumed source fact covers this period; absence is not a discrepancy. */
    NO_COMPARABLE_EVIDENCE,
    /** Technical cleaning/QC/blank — never billed, excluded from both logs and LIS counts */
    WASH_TEST,
    /** Re-run of a previously performed sample — does not consume NEW budget unit */
    RERUN,
}

enum class ComparisonAvailability {
    COMPARABLE,
    NO_DAMUMED_REPORT,
    CONTEXTUAL_ANALYZER_SNAPSHOT,
    UNKNOWN,
}

/** Per-sample reconciliation record (materialized from ParsedAnalyzerSample + catalog match). */
data class SampleReconciliation(
    val id: String,
    val parsedSampleId: String,
    val sampleDate: LocalDate,
    val analyzerId: String?,
    val serviceCatalogId: String?,
    val serviceNameRaw: String?,
    val serviceNameCanonical: String?,
    val category: String?,
    val reconciliationStatus: SampleReconciliationStatus,
    val comparisonAvailability: ComparisonAvailability = ComparisonAvailability.UNKNOWN,
    val comparisonReason: String? = null,
    val reason: String?,
    val graceHours: Int,
    val graceDeadlineAt: LocalDateTime?,
    val matchConfidence: ServiceMatchConfidence?,
    val lisPricePerTestTenge: java.math.BigDecimal?,
    val estimatedWasteTenge: java.math.BigDecimal,
    val lisReferralKey: String?,
    val reconciledAt: LocalDateTime,
    val version: Long? = null,
)

/** Per-service comparison row for reconciliation table. */
data class ReconciliationServiceRow(
    val serviceCatalogId: String?,
    val serviceNameCanonical: String,
    val category: String?,
    val logsCount: Int,
    val lisCount: Int,
    val discrepancyCount: Int,
    val reconciledCount: Int,
    val pendingGraceCount: Int,
    val noComparableEvidenceCount: Int,
    val discrepancyRate: Double,
    val wastedCostTenge: BigDecimal,
    val lisPricePerTestTenge: BigDecimal?,
    val status: ReconciliationStatus,
)
