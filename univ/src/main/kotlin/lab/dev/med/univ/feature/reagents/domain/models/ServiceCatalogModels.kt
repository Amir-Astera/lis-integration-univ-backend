package lab.dev.med.univ.feature.reagents.domain.models

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * High-level service category, mirrors CHECK constraint in service_catalog table.
 * Used for dashboard grouping and grace-period policy.
 */
enum class ServiceCategory {
    HEMATOLOGY,
    BIOCHEMISTRY,
    IMMUNOLOGY,
    COAGULATION,
    URINALYSIS,
    MICROBIOLOGY,
    POCT,
    OTHER;

    val displayName: String get() = when (this) {
        HEMATOLOGY   -> "Гематология"
        BIOCHEMISTRY -> "Биохимия"
        IMMUNOLOGY   -> "Иммунология"
        COAGULATION  -> "Коагулология"
        URINALYSIS   -> "ОАМ"
        MICROBIOLOGY -> "Микробиология"
        POCT         -> "POCT"
        OTHER        -> "Прочее"
    }
}

/**
 * Canonical service definition.
 * [lisAliases] — list of substrings (lowercase) to match against raw LIS service names.
 * [analyzerServiceIds] — integer IDs from HL7/analyzer driver JSON payloads.
 * [analyzerParameters] — parameter codes like WBC, RBC, HGB used for heuristic matching.
 * [analyzerTypes] — which [AnalyzerType] values this service is performed on.
 * [graceHours] — samples in logs not yet in LIS are treated as PENDING_GRACE for this many hours.
 *                0 means any mismatch is an immediate discrepancy.
 */
data class ServiceCatalogEntry(
    val id: String,
    val canonicalName: String,
    val category: ServiceCategory,
    val lisAliases: List<String>,
    val analyzerServiceIds: List<Int>,
    val analyzerParameters: List<String>,
    val analyzerTypes: List<AnalyzerType>,
    val lisPriceTenge: BigDecimal?,
    val graceHours: Int = 0,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val version: Long? = null,
)

/**
 * Confidence level of a service match result.
 * Declaration order matters — LOWER ordinal = STRONGER match
 * (BY_SERVICE_ID is the most reliable, NO_MATCH is the weakest).
 *
 * Use [isAtLeastAsStrongAs] for strength comparisons; the built-in
 * Comparable (via ordinal) happens to match this semantics — smaller ordinal wins.
 */
enum class ServiceMatchConfidence {
    /** Matched by analyzer service_id from HL7 payload — 100% certain */
    BY_SERVICE_ID,
    /** Matched by canonical name exact (case-insensitive) */
    BY_EXACT_NAME,
    /** Matched by one of the configured LIS aliases (substring) */
    BY_LIS_ALIAS,
    /** Matched by Jaro-Winkler similarity ≥ threshold */
    BY_FUZZY,
    /** No match found */
    NO_MATCH;

    val isMatch: Boolean get() = this != NO_MATCH

    /** True iff this confidence is at least as strong as [other] (i.e. ordinal ≤ other.ordinal). */
    fun isAtLeastAsStrongAs(other: ServiceMatchConfidence): Boolean = this.ordinal <= other.ordinal
}

/**
 * Result of matching an input service identifier against the service catalog.
 */
data class ServiceMatchResult(
    val entry: ServiceCatalogEntry?,
    val confidence: ServiceMatchConfidence,
    val matchedAlias: String? = null,
    val similarityScore: Double? = null,
) {
    val isMatch: Boolean get() = confidence.isMatch
}
