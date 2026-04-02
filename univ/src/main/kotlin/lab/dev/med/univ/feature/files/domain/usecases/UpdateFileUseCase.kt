package project.gigienist_reports.feature.files.domain.usecases

import project.gigienist_reports.feature.files.domain.models.File
import project.gigienist_reports.feature.files.domain.models.FileDirectory
import project.gigienist_reports.feature.files.domain.services.FileService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

interface UpdateFileUseCase {
    suspend operator fun invoke(id: String, directory: FileDirectory?, part: FilePart?): File
}

@Service
internal class UpdateFileUseCaseImpl(
        private val fileService: FileService
): UpdateFileUseCase {
    override suspend fun invoke(id: String, directory: FileDirectory?, part: FilePart?): File {
        return fileService.updateFile(id, directory, part)
    }

}