package lab.dev.med.univ.feature.reporting.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reporting.data.entity.DamumedOperationalOverviewSnapshotEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedReportSourceSettingsEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedReportUploadEntity
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface DamumedReportSourceSettingsRepository : CoroutineCrudRepository<DamumedReportSourceSettingsEntity, String>

interface DamumedReportUploadRepository : CoroutineCrudRepository<DamumedReportUploadEntity, String> {
    fun findAllByOrderByUploadedAtDesc(): Flow<DamumedReportUploadEntity>

    fun findAllByReportKindOrderByUploadedAtDesc(reportKind: DamumedLabReportKind): Flow<DamumedReportUploadEntity>
}

interface DamumedOperationalOverviewSnapshotRepository : CoroutineCrudRepository<DamumedOperationalOverviewSnapshotEntity, String> {
    suspend fun findBySnapshotKey(snapshotKey: String): DamumedOperationalOverviewSnapshotEntity?
}
