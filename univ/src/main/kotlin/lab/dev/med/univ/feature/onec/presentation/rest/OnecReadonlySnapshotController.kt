package lab.dev.med.univ.feature.onec.presentation.rest

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitSingle
import lab.dev.med.univ.feature.onec.domain.services.OnecStockPlanningService
import lab.dev.med.univ.feature.onec.domain.services.OnecReadonlySnapshotService
import lab.dev.med.univ.feature.onec.presentation.dto.OnecLimsItemMappingDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecReadonlySnapshotDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecReadonlySnapshotCollectionPageDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecReadonlySnapshotImportResponseDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecReadonlySnapshotSummaryDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecStockCoverageDto
import lab.dev.med.univ.feature.onec.presentation.dto.UpsertOnecLimsItemMappingRequestDto
import org.slf4j.Logger
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.core.security.firebase.FirebaseSecurityUtils
import project.gigienist_reports.feature.users.domain.services.UserAggregateService

@RestController
@RequestMapping("/api/onec")
@Tag(name = "onec-readonly", description = "Sanitized read-only 1C snapshot ingestion and retrieval")
@SecurityRequirement(name = "security_auth")
class OnecReadonlySnapshotController(
    logger: Logger,
    private val snapshotService: OnecReadonlySnapshotService,
    private val stockPlanningService: OnecStockPlanningService,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    @GetMapping("/snapshot")
    suspend fun getSnapshot(): ResponseEntity<OnecReadonlySnapshotDto> =
        ResponseEntity.ok(snapshotService.getLatest() ?: OnecReadonlySnapshotDto(status = "disconnected"))

    @GetMapping("/summary")
    suspend fun getSnapshotSummary(): ResponseEntity<OnecReadonlySnapshotSummaryDto> =
        ResponseEntity.ok(snapshotService.getLatestSummary())

    @GetMapping("/collections/{collection}")
    suspend fun getSnapshotCollection(
        @PathVariable collection: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "100") size: Int,
        @RequestParam(required = false) query: String?,
    ): ResponseEntity<OnecReadonlySnapshotCollectionPageDto> =
        try {
            ResponseEntity.ok(snapshotService.getCollection(collection, page, size, query))
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message, ex)
        }

    @PostMapping(
        "/snapshots/read-only",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    suspend fun uploadReadonlySnapshot(
        @RequestPart("file") file: FilePart,
        @RequestParam(required = false) sourceName: String?,
        exchange: ServerWebExchange,
    ): ResponseEntity<OnecReadonlySnapshotImportResponseDto> {
        return try {
            val user = FirebaseSecurityUtils.getUserFromRequest(exchange).awaitSingle()
            userAggregateService.checkAdminPrivilegesBySession(user)
            val result = snapshotService.importSnapshot(
                bytes = file.readBytes(),
                sourceName = sourceName?.trim().takeUnless { it.isNullOrBlank() } ?: file.filename(),
                importedBy = user.login,
            )
            ResponseEntity.status(if (result.idempotent) HttpStatus.OK else HttpStatus.CREATED).body(result)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/mappings")
    suspend fun getMappings(): ResponseEntity<List<OnecLimsItemMappingDto>> =
        ResponseEntity.ok(stockPlanningService.listMappings())

    @PutMapping("/mappings/{id}")
    suspend fun upsertMapping(
        @PathVariable id: String,
        @RequestBody request: UpsertOnecLimsItemMappingRequestDto,
        exchange: ServerWebExchange,
    ): ResponseEntity<OnecLimsItemMappingDto> {
        return try {
            val user = FirebaseSecurityUtils.getUserFromRequest(exchange).awaitSingle()
            userAggregateService.checkAdminPrivilegesBySession(user)
            ResponseEntity.ok(stockPlanningService.upsertMapping(id, request, user.login))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PostMapping("/mappings")
    suspend fun createMapping(
        @RequestBody request: UpsertOnecLimsItemMappingRequestDto,
        exchange: ServerWebExchange,
    ): ResponseEntity<OnecLimsItemMappingDto> {
        return try {
            val user = FirebaseSecurityUtils.getUserFromRequest(exchange).awaitSingle()
            userAggregateService.checkAdminPrivilegesBySession(user)
            ResponseEntity.status(HttpStatus.CREATED).body(stockPlanningService.createMapping(request, user.login))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/stock-coverage")
    suspend fun getStockCoverage(
        @RequestParam(required = false, defaultValue = "30") planningDays: Int,
        @RequestParam(required = false) damumedUploadId: String?,
    ): ResponseEntity<OnecStockCoverageDto> =
        ResponseEntity.ok(stockPlanningService.stockCoverage(planningDays, damumedUploadId))

    private suspend fun FilePart.readBytes(): ByteArray {
        val buffer = DataBufferUtils.join(content()).awaitSingle()
        return try {
            ByteArray(buffer.readableByteCount()).also(buffer::read)
        } finally {
            DataBufferUtils.release(buffer)
        }
    }
}
