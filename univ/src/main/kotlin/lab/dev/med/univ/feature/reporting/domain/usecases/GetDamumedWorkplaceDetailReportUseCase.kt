package lab.dev.med.univ.feature.reporting.domain.usecases

import kotlinx.coroutines.flow.firstOrNull
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.models.*
import org.springframework.stereotype.Service
import java.time.Instant

interface GetDamumedWorkplaceDetailReportUseCase {
    suspend operator fun invoke(): DamumedWorkplaceDetailReport
}

@Service
class GetDamumedWorkplaceDetailReportUseCaseImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val processedViewUseCase: GetDamumedWorkplaceCompletedStudiesProcessedViewUseCase,
) : GetDamumedWorkplaceDetailReportUseCase {

    override suspend fun invoke(): DamumedWorkplaceDetailReport {
        val latestUpload = uploadRepository
            .findAllByReportKindOrderByUploadedAtDesc(DamumedLabReportKind.WORKPLACE_COMPLETED_STUDIES)
            .firstOrNull()
            ?: throw IllegalStateException("No WORKPLACE_COMPLETED_STUDIES upload found")

        val processedView = processedViewUseCase(latestUpload.id)

        // Преобразуем processed view в детальный отчёт
        val workplaceItems = processedView.workplaces.map { workplace ->
            val serviceDetails = workplace.services.map { service ->
                // Для каждой услуги собираем данные по отделениям
                val departmentDetails = service.cells
                    .filter { it.metricKey == "completed_count" }
                    .mapNotNull { cell ->
                        val column = processedView.departmentColumns.find { it.key == cell.columnKey }
                        if (column != null && !column.isTotal) {
                            WorkplaceServiceDepartmentDetail(
                                departmentName = column.department ?: column.departmentGroup ?: column.displayLabel,
                                departmentGroup = column.departmentGroup,
                                completedCount = cell.numericValue,
                            )
                        } else null
                    }
                    .sortedByDescending { it.completedCount }

                WorkplaceServiceDetail(
                    serviceName = service.service,
                    totalCompleted = service.completedValueTotal,
                    departments = departmentDetails,
                )
            }.sortedByDescending { it.totalCompleted }

            WorkplaceDetailItem(
                workplaceName = workplace.workplace,
                totalCompleted = workplace.summary.completedValueTotal,
                services = serviceDetails,
            )
        }.sortedByDescending { it.totalCompleted }

        return DamumedWorkplaceDetailReport(
            generatedAt = Instant.now(),
            periodLabel = processedView.periodText,
            sourceUploadId = latestUpload.id,
            workplaces = workplaceItems,
        )
    }
}
