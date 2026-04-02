package lab.dev.med.univ.feature.reporting.domain.models

import java.math.BigDecimal
import java.time.Instant

/**
 * Сводные данные из отчета Журнал регистрации направлений (REFERRAL_REGISTRATION_JOURNAL)
 */
data class DamumedReferralRegistrationSummary(
    val generatedAt: Instant,
    val periodLabel: String,
    val sourceUploadId: String,
    val summary: ReferralRegistrationPeriodSummary,
)

data class ReferralRegistrationPeriodSummary(
    val label: String,
    val researchCount: Int,
    val patientCount: Int,
    val departmentCount: Int,
    val sentResultsCount: Int,
    val materialsCount: Int,
)
