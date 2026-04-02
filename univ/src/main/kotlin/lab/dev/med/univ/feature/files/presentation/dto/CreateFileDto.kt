package project.gigienist_reports.feature.files.presentation.dto

import project.gigienist_reports.feature.files.domain.models.FileDirectory
import org.springframework.http.codec.multipart.FilePart

data class CreateFileDto(
    val directory: FileDirectory,
    val part: FilePart
)
