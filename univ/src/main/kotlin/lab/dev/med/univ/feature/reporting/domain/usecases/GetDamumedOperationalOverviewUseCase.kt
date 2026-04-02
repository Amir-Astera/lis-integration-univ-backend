package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalOverview
import lab.dev.med.univ.feature.reporting.domain.services.DamumedOperationalOverviewQueryService
import org.springframework.stereotype.Service

interface GetDamumedOperationalOverviewUseCase {
    suspend operator fun invoke(refresh: Boolean = false): DamumedOperationalOverview
}

@Service
class GetDamumedOperationalOverviewUseCaseImpl(
    private val queryService: DamumedOperationalOverviewQueryService,
) : GetDamumedOperationalOverviewUseCase {
    override suspend fun invoke(refresh: Boolean): DamumedOperationalOverview {
        return queryService.getOverview(refresh)
    }
}
