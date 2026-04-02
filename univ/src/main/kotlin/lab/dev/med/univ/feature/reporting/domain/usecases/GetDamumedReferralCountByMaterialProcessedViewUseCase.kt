package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.domain.models.DamumedReferralCountByMaterialProcessedView
import lab.dev.med.univ.feature.reporting.domain.services.DamumedReferralCountByMaterialProcessedViewQueryService
import org.springframework.stereotype.Service

interface GetDamumedReferralCountByMaterialProcessedViewUseCase {
    suspend operator fun invoke(uploadId: String): DamumedReferralCountByMaterialProcessedView
}

@Service
class GetDamumedReferralCountByMaterialProcessedViewUseCaseImpl(
    private val queryService: DamumedReferralCountByMaterialProcessedViewQueryService,
) : GetDamumedReferralCountByMaterialProcessedViewUseCase {
    override suspend fun invoke(uploadId: String): DamumedReferralCountByMaterialProcessedView {
        return queryService.getView(uploadId)
    }
}
