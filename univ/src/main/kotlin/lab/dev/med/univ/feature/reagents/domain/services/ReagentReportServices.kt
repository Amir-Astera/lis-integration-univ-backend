package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.ReagentConsumptionReportRepository
import lab.dev.med.univ.feature.reagents.domain.errors.ReagentConsumptionReportNotFoundException
import lab.dev.med.univ.feature.reagents.domain.models.ReagentConsumptionReport
import org.springframework.stereotype.Service

interface ReagentReportQueryService {
    suspend fun getReports(analyzerId: String? = null): List<ReagentConsumptionReport>
    suspend fun getReport(reportId: String): ReagentConsumptionReport
}

@Service
internal class ReagentReportQueryServiceImpl(
    private val reportRepository: ReagentConsumptionReportRepository,
) : ReagentReportQueryService {

    override suspend fun getReports(analyzerId: String?): List<ReagentConsumptionReport> {
        val items = if (analyzerId.isNullOrBlank()) {
            reportRepository.findAll().toList()
        } else {
            reportRepository.findAllByAnalyzerIdOrderByGeneratedAtDesc(analyzerId).toList()
        }
        return items.map { it.toModel() }.sortedByDescending { it.generatedAt }
    }

    override suspend fun getReport(reportId: String): ReagentConsumptionReport {
        return reportRepository.findById(reportId)?.toModel()
            ?: throw ReagentConsumptionReportNotFoundException(reportId)
    }
}
