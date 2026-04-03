package lab.dev.med.univ.feature.reporting.domain.usecases

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReferralRegistrationSummary
import lab.dev.med.univ.feature.reporting.domain.models.ReferralDailyRegistrationStat
import lab.dev.med.univ.feature.reporting.domain.models.ReferralDepartmentStat
import lab.dev.med.univ.feature.reporting.domain.models.ReferralFundingSourceStat
import lab.dev.med.univ.feature.reporting.domain.models.ReferralRegistrationPeriodSummary
import lab.dev.med.univ.feature.reporting.domain.models.ReferralServiceStat
import lab.dev.med.univ.feature.reporting.domain.models.ReferralStatusStat
import org.springframework.stereotype.Service
import java.time.Instant

interface GetDamumedReferralRegistrationSummaryUseCase {
    suspend operator fun invoke(): DamumedReferralRegistrationSummary
}

@Service
class GetDamumedReferralRegistrationSummaryUseCaseImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val factRepository: DamumedNormalizedFactRepository,
    private val factDimensionRepository: DamumedNormalizedFactDimensionRepository,
) : GetDamumedReferralRegistrationSummaryUseCase {

    override suspend fun invoke(): DamumedReferralRegistrationSummary {
        val latestUpload = uploadRepository
            .findAllByReportKindOrderByUploadedAtDesc(DamumedLabReportKind.REFERRAL_REGISTRATION_JOURNAL)
            .firstOrNull()
            ?: throw IllegalStateException("No REFERRAL_REGISTRATION_JOURNAL upload found")

        val facts = factRepository
            .findAllByUploadIdOrderBySheetIdAscSourceRowIndexAscSourceColumnIndexAsc(latestUpload.id)
            .toList()

        val factIds = facts.map { it.entityId }
        val dimensionsByFactId = if (factIds.isNotEmpty()) {
            factDimensionRepository.findAllByFactIdInOrderByAxisKeyAsc(factIds).toList()
                .groupBy { it.factId }
        } else {
            emptyMap()
        }

        val factEnvelopes = facts.map { fact ->
            FactEnvelope(
                fact = fact,
                dimensions = dimensionsByFactId[fact.entityId] ?: emptyList(),
            )
        }

        val rows = factEnvelopes.mapNotNull { envelope -> extractRow(envelope) }

        val uniqueResearchKeys = rows.map { "${it.referralNumber}::${it.service}" }.toSet()
        val researchCount = uniqueResearchKeys.size

        val uniquePatients = rows.mapNotNull { it.patientKey }.toSet()
        val patientCount = uniquePatients.size

        val uniqueDepartments = rows.mapNotNull { it.patientDepartment?.takeIf { d -> d.isNotBlank() } }.toSet()
        val departmentCount = uniqueDepartments.size

        val sentResultsCount = rows.count { it.status.equals("Результат отправлен", ignoreCase = true) }
        val pendingCount = rows.count { !it.status.equals("Результат отправлен", ignoreCase = true) }
        val materialsCount = rows.count { !it.material.isNullOrBlank() }
        val emergencyCount = rows.count { it.emergencyFlag?.trim()?.equals("да", ignoreCase = true) == true }

        // Department stats
        val departmentStats = rows
            .groupBy { it.patientDepartment?.trim()?.takeIf { d -> d.isNotBlank() } ?: "Не указано" }
            .map { (dept, deptRows) ->
                val completed = deptRows.count { it.status.equals("Результат отправлен", ignoreCase = true) }
                val inProgress = deptRows.count { it.status.contains("в работе", ignoreCase = true) || it.status.contains("выполняется", ignoreCase = true) }
                ReferralDepartmentStat(
                    department = dept,
                    total = deptRows.size,
                    completed = completed,
                    pending = deptRows.size - completed - inProgress,
                    inProgress = inProgress,
                )
            }
            .sortedByDescending { it.total }
            .take(20)

        // Status stats
        val statusStats = rows
            .groupBy { it.status.trim().takeIf { s -> s.isNotBlank() } ?: "Не указан" }
            .map { (status, statusRows) -> ReferralStatusStat(status = status, count = statusRows.size) }
            .sortedByDescending { it.count }

        // Service stats (top 30)
        val serviceStats = rows
            .groupBy { it.service.trim() }
            .map { (svc, svcRows) ->
                val completed = svcRows.count { it.status.equals("Результат отправлен", ignoreCase = true) }
                ReferralServiceStat(
                    service = svc,
                    total = svcRows.size,
                    completed = completed,
                    pending = svcRows.size - completed,
                )
            }
            .sortedByDescending { it.total }
            .take(30)

        // Funding source stats
        val fundingSourceStats = rows
            .filter { !it.fundingSource.isNullOrBlank() }
            .groupBy { it.fundingSource!!.trim() }
            .map { (src, srcRows) -> ReferralFundingSourceStat(fundingSource = src, count = srcRows.size) }
            .sortedByDescending { it.count }

        // Daily registration — by referral_date if available, else skip
        val dailyStats = rows
            .filter { !it.referralDate.isNullOrBlank() }
            .groupBy { it.referralDate!!.trim().take(10) }
            .map { (date, dateRows) ->
                ReferralDailyRegistrationStat(
                    date = date,
                    registered = dateRows.size,
                    completed = dateRows.count { it.status.equals("Результат отправлен", ignoreCase = true) },
                )
            }
            .sortedBy { it.date }

        return DamumedReferralRegistrationSummary(
            generatedAt = Instant.now(),
            periodLabel = latestUpload.detectedPeriodText ?: "",
            sourceUploadId = latestUpload.id,
            summary = ReferralRegistrationPeriodSummary(
                label = "За период",
                researchCount = researchCount,
                patientCount = patientCount,
                departmentCount = departmentCount,
                sentResultsCount = sentResultsCount,
                pendingCount = pendingCount,
                materialsCount = materialsCount,
                emergencyCount = emergencyCount,
            ),
            departmentStats = departmentStats,
            statusStats = statusStats,
            serviceStats = serviceStats,
            fundingSourceStats = fundingSourceStats,
            dailyRegistrationStats = dailyStats,
        )
    }

    private fun extractRow(envelope: FactEnvelope): ReferralJournalRow? {
        if (envelope.fact.reportKind != DamumedLabReportKind.REFERRAL_REGISTRATION_JOURNAL) {
            return null
        }

        val referralNumber = envelope.dimensions
            .firstOrNull { it.axisKey == "referral_number" }?.rawValue?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val service = envelope.dimensions
            .firstOrNull { it.axisKey == "service" }?.rawValue?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val status = envelope.dimensions
            .firstOrNull { it.axisKey == "referral_status" }?.rawValue?.trim().orEmpty()

        val patientKey = envelope.dimensions
            .firstOrNull { it.axisKey == "patient_iin" }?.rawValue?.trim()?.takeIf { it.isNotBlank() }
            ?: envelope.dimensions.firstOrNull { it.axisKey == "patient_name" }?.rawValue?.trim()?.takeIf { it.isNotBlank() }

        val material = envelope.dimensions
            .firstOrNull { it.axisKey == "material" }?.rawValue?.trim()

        val patientDepartment = envelope.dimensions
            .firstOrNull { it.axisKey == "patient_department" }?.rawValue?.trim()

        val fundingSource = envelope.dimensions
            .firstOrNull { it.axisKey == "funding_source" }?.rawValue?.trim()

        val emergencyFlag = envelope.dimensions
            .firstOrNull { it.axisKey == "emergency_flag" }?.rawValue?.trim()

        val referralDate = envelope.dimensions
            .firstOrNull { it.axisKey == "referral_date" }?.rawValue?.trim()
            ?: envelope.dimensions.firstOrNull { it.axisKey == "registration_date" }?.rawValue?.trim()

        return ReferralJournalRow(
            referralNumber = referralNumber,
            service = service,
            status = status,
            patientKey = patientKey,
            material = material,
            patientDepartment = patientDepartment,
            fundingSource = fundingSource,
            emergencyFlag = emergencyFlag,
            referralDate = referralDate,
        )
    }

    private data class FactEnvelope(
        val fact: DamumedNormalizedFactEntity,
        val dimensions: List<DamumedNormalizedFactDimensionEntity>,
    )

    private data class ReferralJournalRow(
        val referralNumber: String,
        val service: String,
        val status: String,
        val patientKey: String?,
        val material: String?,
        val patientDepartment: String?,
        val fundingSource: String?,
        val emergencyFlag: String?,
        val referralDate: String?,
    )
}
