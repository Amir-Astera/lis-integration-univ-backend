package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.domain.models.DamumedParsedWorkbookPreview
import lab.dev.med.univ.feature.reporting.domain.services.DamumedParsedWorkbookPreviewQueryService
import org.springframework.stereotype.Service

interface GetDamumedParsedWorkbookPreviewUseCase {
    suspend operator fun invoke(uploadId: String, maxRowsPerSheet: Int = 25): DamumedParsedWorkbookPreview
}

@Service
class GetDamumedParsedWorkbookPreviewUseCaseImpl(
    private val queryService: DamumedParsedWorkbookPreviewQueryService,
) : GetDamumedParsedWorkbookPreviewUseCase {
    override suspend fun invoke(uploadId: String, maxRowsPerSheet: Int): DamumedParsedWorkbookPreview {
        return queryService.getPreview(uploadId, maxRowsPerSheet)
    }
}
