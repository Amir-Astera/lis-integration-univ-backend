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
import lab.dev.med.univ.feature.reporting.domain.models.ReferralRegistrationPeriodSummary
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

        val factEnvelopes = facts.map { fact ->
            FactEnvelope(
                fact = fact,
                dimensions = factDimensionRepository
                    .findAllByFactIdOrderByAxisKeyAsc(fact.entityId)
                    .toList()
            )
        }

        val rows = factEnvelopes.mapNotNull { envelope -> extractRow(envelope) }

        // Количество исследований - уникальное количество зарегистрированных услуг (уникальные комбинации referralNumber + service)
        val uniqueResearchKeys = rows.map { "${it.referralNumber}::${it.service}" }.toSet()
        val researchCount = uniqueResearchKeys.size

        // Количество пациентов - уникальные пациенты
        val uniquePatients = rows.mapNotNull { it.patientKey }.toSet()
        val patientCount = uniquePatients.size

        // Количество отделений - уникальные patientDepartment
        val uniqueDepartments = rows.mapNotNull { it.patientDepartment }.toSet()
        val departmentCount = uniqueDepartments.size

        // Отправленные результаты - те которые со статусом "Результат отправлен"
        val sentResultsCount = rows.count { it.status.equals("Результат отправлен", ignoreCase = true) }

        // Количество материалов - общее количество (не уникальное) из поля material
        val materialsCount = rows.count { !it.material.isNullOrBlank() }

        return DamumedReferralRegistrationSummary(
            generatedAt = Instant.now(),
            periodLabel = latestUpload.detectedPeriodText ?: "",
            sourceUploadId = latestUpload.id,
            summary = ReferralRegistrationPeriodSummary(
                label = "За месяц",
                researchCount = researchCount,
                patientCount = patientCount,
                departmentCount = departmentCount,
                sentResultsCount = sentResultsCount,
                materialsCount = materialsCount,
            )
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

        return ReferralJournalRow(
            referralNumber = referralNumber,
            service = service,
            status = status,
            patientKey = patientKey,
            material = material,
            patientDepartment = patientDepartment,
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
    )
}
