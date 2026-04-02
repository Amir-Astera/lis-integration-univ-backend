package project.gigienist_reports.feature.files.domain.usecases

import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import project.gigienist_reports.feature.files.domain.models.FileDirectory
import project.gigienist_reports.feature.files.domain.services.FileService

interface GetFileReportUseCase {
    suspend operator fun invoke(directory: FileDirectory, id: String, format: String): Resource
}

@Service
internal class GetFileReportUseCaseImpl(
    private val fileService: FileService
): GetFileReportUseCase {
    override suspend fun invoke(directory: FileDirectory, id: String, format: String): Resource {
        return fileService.getFileReports(directory, id, format)
    }

}