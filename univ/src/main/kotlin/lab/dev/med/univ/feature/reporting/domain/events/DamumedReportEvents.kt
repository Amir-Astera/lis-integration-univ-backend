package lab.dev.med.univ.feature.reporting.domain.events

import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import java.time.LocalDateTime

/**
 * Published after a Damumed report upload transitions to NORMALIZED status.
 * Consumers (e.g. reconciliation rebuild listener) can subscribe without creating
 * a direct dependency on reporting.
 */
data class DamumedReportNormalizedEvent(
    val uploadId: String,
    val reportKind: DamumedLabReportKind,
    val normalizedAt: LocalDateTime = LocalDateTime.now(),
)
