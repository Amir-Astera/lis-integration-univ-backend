package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSourceMode
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSourceSettings
import lab.dev.med.univ.feature.reporting.domain.services.DamumedReportSourceSettingsService
import org.springframework.stereotype.Service

interface UpdateDamumedReportSourceModeUseCase {
    suspend operator fun invoke(mode: DamumedReportSourceMode, updatedBy: String? = null): DamumedReportSourceSettings
}

@Service
internal class UpdateDamumedReportSourceModeUseCaseImpl(
    private val settingsService: DamumedReportSourceSettingsService,
) : UpdateDamumedReportSourceModeUseCase {
    override suspend fun invoke(mode: DamumedReportSourceMode, updatedBy: String?): DamumedReportSourceSettings {
        return settingsService.updateMode(mode, updatedBy)
    }
}
