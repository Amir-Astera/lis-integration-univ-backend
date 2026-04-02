package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import lab.dev.med.univ.feature.reporting.domain.services.DamumedReportUploadQueryService
import org.springframework.stereotype.Service

interface GetDamumedReportUploadsUseCase {
    suspend operator fun invoke(reportKind: DamumedLabReportKind? = null): List<DamumedReportUpload>
}

@Service
internal class GetDamumedReportUploadsUseCaseImpl(
    private val queryService: DamumedReportUploadQueryService,
) : GetDamumedReportUploadsUseCase {
    override suspend fun invoke(reportKind: DamumedLabReportKind?): List<DamumedReportUpload> {
        return queryService.getAll(reportKind)
    }
}
