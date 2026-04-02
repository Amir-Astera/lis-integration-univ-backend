package lab.dev.med.univ.feature.reporting.domain.services

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reporting.data.entity.toModel
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import org.springframework.stereotype.Service

interface DamumedReportUploadQueryService {
    suspend fun getAll(reportKind: DamumedLabReportKind? = null): List<DamumedReportUpload>
}

@Service
internal class DamumedReportUploadQueryServiceImpl(
    private val repository: DamumedReportUploadRepository,
) : DamumedReportUploadQueryService {
    override suspend fun getAll(reportKind: DamumedLabReportKind?): List<DamumedReportUpload> {
        return if (reportKind == null) {
            repository.findAllByOrderByUploadedAtDesc().map { it.toModel() }.toList()
        } else {
            repository.findAllByReportKindOrderByUploadedAtDesc(reportKind).map { it.toModel() }.toList()
        }
    }
}
