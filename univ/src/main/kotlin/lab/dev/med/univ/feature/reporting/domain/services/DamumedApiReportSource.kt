package lab.dev.med.univ.feature.reporting.domain.services

import lab.dev.med.univ.feature.reporting.domain.errors.DamumedApiIntegrationNotReadyException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import org.springframework.stereotype.Service

interface DamumedApiReportSource {
    suspend fun synchronize(requestedBy: String? = null): List<DamumedReportUpload>
}

@Service
internal class DamumedApiReportSourceImpl : DamumedApiReportSource {
    override suspend fun synchronize(requestedBy: String?): List<DamumedReportUpload> {
        throw DamumedApiIntegrationNotReadyException()
    }
}
