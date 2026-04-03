package lab.dev.med.univ.feature.reporting.domain.models

import java.time.Instant

/**
 * Сводные данные из отчета Журнал регистрации направлений (REFERRAL_REGISTRATION_JOURNAL)
 */
data class DamumedReferralRegistrationSummary(
    val generatedAt: Instant,
    val periodLabel: String,
    val sourceUploadId: String,
    val summary: ReferralRegistrationPeriodSummary,
    val departmentStats: List<ReferralDepartmentStat> = emptyList(),
    val statusStats: List<ReferralStatusStat> = emptyList(),
    val serviceStats: List<ReferralServiceStat> = emptyList(),
    val fundingSourceStats: List<ReferralFundingSourceStat> = emptyList(),
    val dailyRegistrationStats: List<ReferralDailyRegistrationStat> = emptyList(),
)

data class ReferralRegistrationPeriodSummary(
    val label: String,
    val researchCount: Int,
    val patientCount: Int,
    val departmentCount: Int,
    val sentResultsCount: Int,
    val pendingCount: Int,
    val materialsCount: Int,
    val avgCompletionMinutes: Double? = null,
    val emergencyCount: Int = 0,
)

data class ReferralDepartmentStat(
    val department: String,
    val total: Int,
    val completed: Int,
    val pending: Int,
    val inProgress: Int,
)

data class ReferralStatusStat(
    val status: String,
    val count: Int,
)

data class ReferralServiceStat(
    val service: String,
    val total: Int,
    val completed: Int,
    val pending: Int,
)

data class ReferralFundingSourceStat(
    val fundingSource: String,
    val count: Int,
)

data class ReferralDailyRegistrationStat(
    val date: String,
    val registered: Int,
    val completed: Int,
)
