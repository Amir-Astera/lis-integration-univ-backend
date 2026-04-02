package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.domain.models.DamumedEmployeeCompletedStudiesSummaryProcessedView
import lab.dev.med.univ.feature.reporting.domain.services.DamumedEmployeeCompletedStudiesSummaryProcessedViewQueryService
import org.springframework.stereotype.Service

interface GetDamumedEmployeeCompletedStudiesSummaryProcessedViewUseCase {
    suspend operator fun invoke(uploadId: String): DamumedEmployeeCompletedStudiesSummaryProcessedView
}

@Service
class GetDamumedEmployeeCompletedStudiesSummaryProcessedViewUseCaseImpl(
    private val queryService: DamumedEmployeeCompletedStudiesSummaryProcessedViewQueryService,
) : GetDamumedEmployeeCompletedStudiesSummaryProcessedViewUseCase {
    override suspend fun invoke(uploadId: String): DamumedEmployeeCompletedStudiesSummaryProcessedView {
        return queryService.getView(uploadId)
    }
}
