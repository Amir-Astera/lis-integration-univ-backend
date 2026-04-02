package project.gigienist_reports.feature.files.data

import project.gigienist_reports.feature.files.domain.models.FileDirectory
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "files")
data class FileEntity(
        @Id
        val id: String,
        val directory: FileDirectory,
        val format: String,
        val url: String,
        @Version
        val version: Long?,
        val createdAt: LocalDateTime = LocalDateTime.now()
)