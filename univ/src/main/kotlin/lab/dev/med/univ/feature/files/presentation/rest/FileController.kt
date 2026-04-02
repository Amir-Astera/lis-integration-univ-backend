package project.gigienist_reports.feature.files.presentation.rest

import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.core.config.api.CreateApiResponses
import project.gigienist_reports.feature.files.domain.models.FileDirectory
import project.gigienist_reports.feature.files.presentation.dto.UpdateFileDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import project.gigienist_reports.feature.files.domain.usecases.*

@RestController
@RequestMapping("/api/files")
@Tag(name = "files", description = "The Files API")
class FileController(
    logger: Logger,
    private val uploadFileUseCase: UploadFileUseCase,
    private val getFileUseCase: GetFileUseCase,
    private val updateFileUseCase: UpdateFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    private val getFileReportUseCase: GetFileReportUseCase
): Controller(logger) {
    @SecurityRequirement(name = "security_auth")
    @CreateApiResponses
    @PostMapping("/{directory}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun create(
        @PathVariable directory: FileDirectory,
        @RequestPart part: FilePart
    ): ResponseEntity<Any> {
        try {
            val response = uploadFileUseCase(directory, part)
            return HttpStatus.CREATED.response(response)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @SecurityRequirement(name = "security_auth")
    @GetMapping(
        "/{directory}/{id}.{format}",
        produces = [
            MediaType.ALL_VALUE
        ]
    )
    suspend fun getFile(
        @PathVariable directory: FileDirectory,
        @PathVariable id: String,
        @PathVariable format: String
    ): ResponseEntity<Any> {
        try {
            val response = getFileUseCase(directory, id, format)
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(response)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @SecurityRequirement(name = "security_auth")
    @GetMapping(
        "/reports/{directory}/{id}.{format}",
        produces = [
            MediaType.ALL_VALUE
        ]
    )
    suspend fun getFileReport(
        @PathVariable directory: FileDirectory,
        @PathVariable id: String,
        @PathVariable format: String
    ): ResponseEntity<Any> {
        try {
            val resource = getFileReportUseCase(directory, id, format)
            val asciiFallback = "$id.$format"

            val display = java.net.URLDecoder.decode(id, Charsets.UTF_8)
            val utf8Name = "$display.$format"


            val cd = "attachment; filename=\"${asciiFallback}\"; filename*=UTF-8''${java.net.URLEncoder.encode(utf8Name, "UTF-8")}"

            return ResponseEntity.ok()
                .header("Content-Disposition", cd)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @SecurityRequirement(name = "security_auth")
    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun updateFile(
            @PathVariable id: String,
            @RequestBody data: UpdateFileDto
    ): ResponseEntity<Void> {
        try {
            updateFileUseCase(id, data.directory, data.part)
            return HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @SecurityRequirement(name = "security_auth")
    @DeleteMapping("/{directory}/{id}.{format}", produces = [MediaType.ALL_VALUE])
    suspend fun deleteFile(
        @PathVariable id: String,
        @PathVariable directory: FileDirectory,
        @PathVariable format: String
    ): ResponseEntity<Void> {
        try {
            deleteFileUseCase(id, directory, format)
            return HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

}