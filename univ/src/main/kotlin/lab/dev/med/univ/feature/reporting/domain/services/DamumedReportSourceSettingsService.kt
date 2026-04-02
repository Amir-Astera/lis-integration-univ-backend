package lab.dev.med.univ.feature.reporting.domain.services

import lab.dev.med.univ.feature.reporting.data.entity.toEntity
import lab.dev.med.univ.feature.reporting.data.entity.toModel
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportSourceSettingsRepository
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSourceMode
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSourceSettings
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface DamumedReportSourceSettingsService {
    suspend fun getSettings(): DamumedReportSourceSettings

    suspend fun updateMode(mode: DamumedReportSourceMode, updatedBy: String? = null): DamumedReportSourceSettings
}

@Service
internal class DamumedReportSourceSettingsServiceImpl(
    private val repository: DamumedReportSourceSettingsRepository,
) : DamumedReportSourceSettingsService {
    override suspend fun getSettings(): DamumedReportSourceSettings {
        val existing = repository.findById(DamumedReportSourceSettings.DEFAULT_ID)
        return existing?.toModel() ?: repository.save(DamumedReportSourceSettings().toEntity()).toModel()
    }

    override suspend fun updateMode(mode: DamumedReportSourceMode, updatedBy: String?): DamumedReportSourceSettings {
        val current = getSettings()
        val updated = current.copy(
            mode = mode,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy,
        )
        return repository.save(updated.toEntity()).toModel()
    }
}
