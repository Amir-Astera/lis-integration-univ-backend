package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.data.entity.toModel
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportParseStatus
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import lab.dev.med.univ.feature.reporting.domain.services.DamumedWorkplaceCompletedStudiesProcessingService
import lab.dev.med.univ.feature.reporting.domain.services.DamumedWorkbookNormalizationService
import org.springframework.stereotype.Service

interface NormalizeDamumedReportUploadUseCase {
    suspend operator fun invoke(uploadId: String): DamumedReportUpload
}

@Service
class NormalizeDamumedReportUploadUseCaseImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val workplaceCompletedStudiesProcessingService: DamumedWorkplaceCompletedStudiesProcessingService,
    private val workbookNormalizationService: DamumedWorkbookNormalizationService,
) : NormalizeDamumedReportUploadUseCase {
    override suspend fun invoke(uploadId: String): DamumedReportUpload {
        val upload = uploadRepository.findById(uploadId)
            ?.toModel()
            ?: throw DamumedReportValidationException("Report upload not found.")
        if (upload.parseStatus != DamumedReportParseStatus.PARSED) {
            val parseHint = upload.parseErrorMessage?.trim()?.take(400)?.let { " Детали разбора: $it" }.orEmpty()
            throw DamumedReportValidationException(
                "Сначала должен успешно завершиться разбор файла (сейчас parseStatus=${upload.parseStatus}).$parseHint",
            )
        }
        return when (upload.reportKind) {
            DamumedLabReportKind.WORKPLACE_COMPLETED_STUDIES -> {
                workplaceCompletedStudiesProcessingService.normalizeExistingUpload(upload)
            }

            else -> workbookNormalizationService.normalize(upload)
        }
    }
}
