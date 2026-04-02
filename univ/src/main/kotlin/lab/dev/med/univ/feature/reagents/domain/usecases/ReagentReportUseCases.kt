package lab.dev.med.univ.feature.reagents.domain.usecases

import lab.dev.med.univ.feature.reagents.domain.models.ReagentConsumptionReport
import lab.dev.med.univ.feature.reagents.domain.services.GenerateReagentConsumptionReportService
import lab.dev.med.univ.feature.reagents.domain.services.ReagentReportQueryService
import org.springframework.stereotype.Service
import java.time.LocalDate

interface GetReagentConsumptionReportsUseCase {
    suspend operator fun invoke(analyzerId: String? = null): List<ReagentConsumptionReport>
}

interface GetReagentConsumptionReportUseCase {
    suspend operator fun invoke(reportId: String): ReagentConsumptionReport
}

interface GenerateReagentConsumptionReportUseCase {
    suspend operator fun invoke(
        analyzerId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate,
        generatedBy: String? = null,
    ): ReagentConsumptionReport
}

@Service
internal class GetReagentConsumptionReportsUseCaseImpl(
    private val queryService: ReagentReportQueryService,
) : GetReagentConsumptionReportsUseCase {
    override suspend fun invoke(analyzerId: String?): List<ReagentConsumptionReport> = queryService.getReports(analyzerId)
}

@Service
internal class GetReagentConsumptionReportUseCaseImpl(
    private val queryService: ReagentReportQueryService,
) : GetReagentConsumptionReportUseCase {
    override suspend fun invoke(reportId: String): ReagentConsumptionReport = queryService.getReport(reportId)
}

@Service
internal class GenerateReagentConsumptionReportUseCaseImpl(
    private val generationService: GenerateReagentConsumptionReportService,
) : GenerateReagentConsumptionReportUseCase {
    override suspend fun invoke(
        analyzerId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate,
        generatedBy: String?,
    ): ReagentConsumptionReport {
        return generationService.generate(analyzerId, periodStart, periodEnd, generatedBy)
    }
}
