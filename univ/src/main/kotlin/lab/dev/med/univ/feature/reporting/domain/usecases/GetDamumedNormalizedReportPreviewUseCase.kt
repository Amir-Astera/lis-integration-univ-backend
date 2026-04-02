package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.domain.models.DamumedNormalizedReportPreview
import lab.dev.med.univ.feature.reporting.domain.services.DamumedNormalizedReportPreviewQueryService
import org.springframework.stereotype.Service

interface GetDamumedNormalizedReportPreviewUseCase {
    suspend operator fun invoke(uploadId: String, maxFacts: Int = 250): DamumedNormalizedReportPreview
}

@Service
class GetDamumedNormalizedReportPreviewUseCaseImpl(
    private val queryService: DamumedNormalizedReportPreviewQueryService,
) : GetDamumedNormalizedReportPreviewUseCase {
    override suspend fun invoke(uploadId: String, maxFacts: Int): DamumedNormalizedReportPreview {
        return queryService.getPreview(uploadId, maxFacts)
    }
}
