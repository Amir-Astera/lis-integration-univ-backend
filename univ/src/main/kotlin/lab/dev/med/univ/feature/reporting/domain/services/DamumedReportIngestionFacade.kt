package lab.dev.med.univ.feature.reporting.domain.services

import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportSourceModeMismatchException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSourceMode
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

interface DamumedReportIngestionFacade {
    suspend fun uploadManual(
        reportKind: DamumedLabReportKind,
        part: FilePart,
        uploadedBy: String? = null,
    ): DamumedReportUpload

    suspend fun synchronizeFromActiveSource(requestedBy: String? = null): List<DamumedReportUpload>
}

@Service
internal class DamumedReportIngestionFacadeImpl(
    private val settingsService: DamumedReportSourceSettingsService,
    private val manualSource: DamumedManualReportSource,
    private val apiSource: DamumedApiReportSource,
) : DamumedReportIngestionFacade {
    override suspend fun uploadManual(
        reportKind: DamumedLabReportKind,
        part: FilePart,
        uploadedBy: String?,
    ): DamumedReportUpload {
        val settings = settingsService.getSettings()
        if (settings.mode != DamumedReportSourceMode.MANUAL) {
            throw DamumedReportSourceModeMismatchException(DamumedReportSourceMode.MANUAL, settings.mode)
        }
        return manualSource.upload(reportKind, part, uploadedBy)
    }

    override suspend fun synchronizeFromActiveSource(requestedBy: String?): List<DamumedReportUpload> {
        val settings = settingsService.getSettings()
        return when (settings.mode) {
            DamumedReportSourceMode.MANUAL -> throw DamumedReportSourceModeMismatchException(
                DamumedReportSourceMode.DAMUMED_API,
                settings.mode,
            )

            DamumedReportSourceMode.DAMUMED_API -> apiSource.synchronize(requestedBy)
        }
    }
}
