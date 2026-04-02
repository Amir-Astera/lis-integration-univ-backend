package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.domain.models.DamumedWorkplaceCompletedStudiesProcessedView
import lab.dev.med.univ.feature.reporting.domain.services.DamumedWorkplaceCompletedStudiesProcessedViewQueryService
import org.springframework.stereotype.Service

interface GetDamumedWorkplaceCompletedStudiesProcessedViewUseCase {
    suspend operator fun invoke(uploadId: String): DamumedWorkplaceCompletedStudiesProcessedView
}

@Service
class GetDamumedWorkplaceCompletedStudiesProcessedViewUseCaseImpl(
    private val queryService: DamumedWorkplaceCompletedStudiesProcessedViewQueryService,
) : GetDamumedWorkplaceCompletedStudiesProcessedViewUseCase {
    override suspend fun invoke(uploadId: String): DamumedWorkplaceCompletedStudiesProcessedView {
        return queryService.getView(uploadId)
    }
}
