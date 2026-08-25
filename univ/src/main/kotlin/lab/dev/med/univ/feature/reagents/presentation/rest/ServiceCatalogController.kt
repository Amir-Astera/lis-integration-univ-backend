package lab.dev.med.univ.feature.reagents.presentation.rest

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.reagents.data.entity.toEntity
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.ServiceCatalogRepository
import lab.dev.med.univ.feature.reagents.domain.services.ServiceMatchingService
import lab.dev.med.univ.feature.reagents.presentation.dto.ServiceCatalogEntryDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ServiceCatalogUpsertRequest
import lab.dev.med.univ.feature.reagents.presentation.dto.toDto
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.core.security.firebase.FirebaseSecurityUtils
import project.gigienist_reports.feature.users.domain.services.UserAggregateService

/**
 * CRUD admin API for the service catalog (bridges LIS names ↔ analyzer log names).
 *
 * Catalog changes trigger an in-memory reload of ServiceMatchingService so the next
 * reconciliation rebuild sees the updated rules without a restart.
 */
@RestController
@RequestMapping("/api/reagents/service-catalog")
@Tag(name = "reagents-service-catalog", description = "Service catalog CRUD for reconciliation")
@SecurityRequirement(name = "security_auth")
class ServiceCatalogController(
    logger: Logger,
    private val catalogRepository: ServiceCatalogRepository,
    private val matchingService: ServiceMatchingService,
    private val userAggregateService: UserAggregateService,
    private val objectMapper: ObjectMapper,
) : Controller(logger) {

    @GetMapping
    @Operation(summary = "List all service catalog entries")
    suspend fun list(
        @RequestParam(required = false, defaultValue = "false") includeInactive: Boolean,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<ServiceCatalogEntryDto>> = safely {
        val entities = if (includeInactive) {
            catalogRepository.findAllByOrderByCategoryAscCanonicalNameAsc().toList()
        } else {
            catalogRepository.findAllByIsActiveTrueOrderByCanonicalNameAsc().toList()
        }
        ResponseEntity.ok(entities.map { it.toModel(objectMapper).toDto() })
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single catalog entry by id")
    suspend fun get(@PathVariable id: String, exchange: ServerWebExchange): ResponseEntity<ServiceCatalogEntryDto> = safely {
        val entity = catalogRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Service catalog entry $id not found")
        ResponseEntity.ok(entity.toModel(objectMapper).toDto())
    }

    @PostMapping
    @Operation(summary = "Create a new catalog entry (admin)")
    suspend fun create(
        @RequestBody request: ServiceCatalogUpsertRequest,
        exchange: ServerWebExchange,
    ): ResponseEntity<ServiceCatalogEntryDto> = safely {
        ensureAdmin(exchange)
        require(request.canonicalName.isNotBlank()) { "canonicalName must not be blank" }

        val duplicate = catalogRepository.findByCanonicalNameIgnoreCase(request.canonicalName.trim())
        if (duplicate != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Service catalog entry with canonical name '${request.canonicalName}' already exists (id=${duplicate.id})",
            )
        }

        val domain = request.copy(id = null).toDomain()
        val saved = catalogRepository.save(domain.toEntity(objectMapper)).toModel(objectMapper)
        matchingService.reload()
        ResponseEntity.status(HttpStatus.CREATED).body(saved.toDto())
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing catalog entry (admin)")
    suspend fun update(
        @PathVariable id: String,
        @RequestBody request: ServiceCatalogUpsertRequest,
        exchange: ServerWebExchange,
    ): ResponseEntity<ServiceCatalogEntryDto> = safely {
        ensureAdmin(exchange)
        require(request.canonicalName.isNotBlank()) { "canonicalName must not be blank" }

        val existingEntity = catalogRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Service catalog entry $id not found")
        val existingDomain = existingEntity.toModel(objectMapper)

        val merged = request.copy(id = id).toDomain(existing = existingDomain)
        val saved = catalogRepository.save(merged.toEntity(objectMapper)).toModel(objectMapper)
        matchingService.reload()
        ResponseEntity.ok(saved.toDto())
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete (deactivate) a catalog entry (admin)")
    suspend fun deactivate(
        @PathVariable id: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<ServiceCatalogEntryDto> = safely {
        ensureAdmin(exchange)
        val entity = catalogRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Service catalog entry $id not found")
        val deactivated = entity.copy(isActive = false, updatedAt = java.time.LocalDateTime.now())
        val saved = catalogRepository.save(deactivated).toModel(objectMapper)
        matchingService.reload()
        ResponseEntity.ok(saved.toDto())
    }

    @PostMapping("/reload")
    @Operation(summary = "Force reload of in-memory matching cache (admin)")
    suspend fun reload(exchange: ServerWebExchange): ResponseEntity<Map<String, Any>> = safely {
        ensureAdmin(exchange)
        matchingService.reload()
        ResponseEntity.ok(mapOf("status" to "reloaded", "entries" to matchingService.allEntries().size))
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private suspend fun ensureAdmin(exchange: ServerWebExchange) {
        val user = FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
        userAggregateService.checkAdminPrivilegesBySession(user)
    }

    private inline fun <T> safely(block: () -> T): T = try {
        block()
    } catch (ex: ResponseStatusException) {
        throw ex
    } catch (ex: IllegalArgumentException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message, ex)
    } catch (ex: Exception) {
        val (code, message) = getError(ex)
        throw ResponseStatusException(code, message, ex)
    }
}
