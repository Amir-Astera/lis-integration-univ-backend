package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import lab.dev.med.univ.feature.reporting.domain.services.DamumedReportIngestionFacade
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

interface UploadManualDamumedReportUseCase {
    suspend operator fun invoke(
        reportKind: DamumedLabReportKind,
        part: FilePart,
        uploadedBy: String? = null,
    ): DamumedReportUpload
}

@Service
internal class UploadManualDamumedReportUseCaseImpl(
    private val ingestionFacade: DamumedReportIngestionFacade,
) : UploadManualDamumedReportUseCase {
    override suspend fun invoke(
        reportKind: DamumedLabReportKind,
        part: FilePart,
        uploadedBy: String?,
    ): DamumedReportUpload {
        return ingestionFacade.uploadManual(reportKind, part, uploadedBy)
    }
}
