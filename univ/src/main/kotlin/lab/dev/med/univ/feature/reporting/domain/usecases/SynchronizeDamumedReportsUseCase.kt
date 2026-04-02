package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import lab.dev.med.univ.feature.reporting.domain.services.DamumedReportIngestionFacade
import org.springframework.stereotype.Service

interface SynchronizeDamumedReportsUseCase {
    suspend operator fun invoke(requestedBy: String? = null): List<DamumedReportUpload>
}

@Service
internal class SynchronizeDamumedReportsUseCaseImpl(
    private val ingestionFacade: DamumedReportIngestionFacade,
) : SynchronizeDamumedReportsUseCase {
    override suspend fun invoke(requestedBy: String?): List<DamumedReportUpload> {
        return ingestionFacade.synchronizeFromActiveSource(requestedBy)
    }
}
