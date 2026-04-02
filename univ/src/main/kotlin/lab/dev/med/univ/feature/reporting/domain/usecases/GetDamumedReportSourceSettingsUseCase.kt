package lab.dev.med.univ.feature.reporting.domain.usecases

import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSourceSettings
import lab.dev.med.univ.feature.reporting.domain.services.DamumedReportSourceSettingsService
import org.springframework.stereotype.Service

interface GetDamumedReportSourceSettingsUseCase {
    suspend operator fun invoke(): DamumedReportSourceSettings
}

@Service
internal class GetDamumedReportSourceSettingsUseCaseImpl(
    private val settingsService: DamumedReportSourceSettingsService,
) : GetDamumedReportSourceSettingsUseCase {
    override suspend fun invoke(): DamumedReportSourceSettings = settingsService.getSettings()
}
