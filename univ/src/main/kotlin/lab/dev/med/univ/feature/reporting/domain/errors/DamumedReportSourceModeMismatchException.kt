package lab.dev.med.univ.feature.reporting.domain.errors

import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSourceMode

class DamumedReportSourceModeMismatchException(
    expected: DamumedReportSourceMode,
    actual: DamumedReportSourceMode,
) : RuntimeException("Damumed report source mode mismatch. Expected '$expected', but current mode is '$actual'.")
