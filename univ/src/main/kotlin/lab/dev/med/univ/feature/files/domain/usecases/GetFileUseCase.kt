package project.gigienist_reports.feature.files.domain.usecases

import project.gigienist_reports.feature.files.domain.models.FileDirectory
import project.gigienist_reports.feature.files.domain.services.FileService
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

interface GetFileUseCase {
    suspend operator fun invoke(directory: FileDirectory, id: String, format: String): Resource
}

@Service
internal class GetFileUseCaseImpl(
        private val fileService: FileService
): GetFileUseCase {
    override suspend fun invoke(directory: FileDirectory, id: String, format: String): Resource {
        return fileService.getFile(directory, id, format)
    }

}