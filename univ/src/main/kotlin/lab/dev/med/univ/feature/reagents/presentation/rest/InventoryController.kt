package lab.dev.med.univ.feature.reagents.presentation.rest

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.ConsumableInventoryNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.ReagentInventoryNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.ReagentModuleValidationException
import lab.dev.med.univ.feature.reagents.domain.models.ConsumableCategory
import lab.dev.med.univ.feature.reagents.domain.models.ReagentInventoryStatus
import lab.dev.med.univ.feature.reagents.domain.usecases.DeleteConsumableInventoryUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.DeleteReagentInventoryUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetConsumableInventoryItemUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetConsumableInventoryUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetReagentInventoryItemUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetReagentInventoryUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.UpsertConsumableInventoryUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.UpsertReagentInventoryUseCase
import lab.dev.med.univ.feature.reagents.presentation.dto.ConsumableInventoryResponseDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReagentInventoryResponseDto
import lab.dev.med.univ.feature.reagents.presentation.dto.UpsertConsumableInventoryRequestDto
import lab.dev.med.univ.feature.reagents.presentation.dto.UpsertReagentInventoryRequestDto
import lab.dev.med.univ.feature.reagents.presentation.dto.toModel
import lab.dev.med.univ.feature.reagents.presentation.dto.toResponseDto
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.core.security.SessionUser
import project.gigienist_reports.core.security.firebase.FirebaseSecurityUtils
import project.gigienist_reports.feature.users.domain.services.UserAggregateService

@RestController
@RequestMapping("/api/reagents/inventory")
@Tag(name = "reagents-inventory", description = "Reagent and consumable inventory API")
@SecurityRequirement(name = "security_auth")
class InventoryController(
    logger: Logger,
    private val getReagentInventoryUseCase: GetReagentInventoryUseCase,
    private val getReagentInventoryItemUseCase: GetReagentInventoryItemUseCase,
    private val upsertReagentInventoryUseCase: UpsertReagentInventoryUseCase,
    private val deleteReagentInventoryUseCase: DeleteReagentInventoryUseCase,
    private val getConsumableInventoryUseCase: GetConsumableInventoryUseCase,
    private val getConsumableInventoryItemUseCase: GetConsumableInventoryItemUseCase,
    private val upsertConsumableInventoryUseCase: UpsertConsumableInventoryUseCase,
    private val deleteConsumableInventoryUseCase: DeleteConsumableInventoryUseCase,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    @GetMapping("/reagents")
    suspend fun getReagentInventory(
        @RequestParam(required = false) analyzerId: String?,
        @RequestParam(required = false) status: ReagentInventoryStatus?,
    ): ResponseEntity<List<ReagentInventoryResponseDto>> {
        return try {
            ResponseEntity.ok(getReagentInventoryUseCase(analyzerId, status).map { it.toResponseDto() })
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/reagents/{inventoryId}")
    suspend fun getReagentInventoryItem(
        @PathVariable inventoryId: String,
    ): ResponseEntity<ReagentInventoryResponseDto> {
        return try {
            ResponseEntity.ok(getReagentInventoryItemUseCase(inventoryId).toResponseDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PutMapping("/reagents/{inventoryId}")
    suspend fun upsertReagentInventory(
        @PathVariable inventoryId: String,
        @RequestBody dto: UpsertReagentInventoryRequestDto,
        exchange: ServerWebExchange,
    ): ResponseEntity<ReagentInventoryResponseDto> {
        return try {
            val actor = ensureAdmin(exchange)
            ResponseEntity.ok(
                upsertReagentInventoryUseCase(inventoryId, dto.toModel(inventoryId, actor.login)).toResponseDto(),
            )
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @DeleteMapping("/reagents/{inventoryId}")
    suspend fun deleteReagentInventory(
        @PathVariable inventoryId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<Void> {
        return try {
            ensureAdmin(exchange)
            deleteReagentInventoryUseCase(inventoryId)
            HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/consumables")
    suspend fun getConsumableInventory(
        @RequestParam(required = false) category: ConsumableCategory?,
    ): ResponseEntity<List<ConsumableInventoryResponseDto>> {
        return try {
            logger.info("Getting consumable inventory with category: $category")
            val result = getConsumableInventoryUseCase(category).map { it.toResponseDto() }
            logger.info("Found ${result.size} consumable inventory items")
            ResponseEntity.ok(result)
        } catch (ex: Exception) {
            logger.error("Error getting consumable inventory", ex)
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/consumables/{inventoryId}")
    suspend fun getConsumableInventoryItem(
        @PathVariable inventoryId: String,
    ): ResponseEntity<ConsumableInventoryResponseDto> {
        return try {
            ResponseEntity.ok(getConsumableInventoryItemUseCase(inventoryId).toResponseDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PutMapping("/consumables/{inventoryId}")
    suspend fun upsertConsumableInventory(
        @PathVariable inventoryId: String,
        @RequestBody dto: UpsertConsumableInventoryRequestDto,
        exchange: ServerWebExchange,
    ): ResponseEntity<ConsumableInventoryResponseDto> {
        return try {
            val actor = ensureAdmin(exchange)
            ResponseEntity.ok(
                upsertConsumableInventoryUseCase(inventoryId, dto.toModel(inventoryId, actor.login)).toResponseDto(),
            )
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @DeleteMapping("/consumables/{inventoryId}")
    suspend fun deleteConsumableInventory(
        @PathVariable inventoryId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<Void> {
        return try {
            ensureAdmin(exchange)
            deleteConsumableInventoryUseCase(inventoryId)
            HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    private suspend fun getSessionUser(exchange: ServerWebExchange): SessionUser {
        return FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
    }

    private suspend fun ensureAdmin(exchange: ServerWebExchange): SessionUser {
        val user = getSessionUser(exchange)
        userAggregateService.checkAdminPrivilegesBySession(user)
        return user
    }

    private fun mapException(ex: Exception): ResponseStatusException {
        return when (ex) {
            is ReagentModuleValidationException -> ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message, ex)
            is AnalyzerNotFoundException -> ResponseStatusException(HttpStatus.NOT_FOUND, ex.message, ex)
            is ReagentInventoryNotFoundException -> ResponseStatusException(HttpStatus.NOT_FOUND, ex.message, ex)
            is ConsumableInventoryNotFoundException -> ResponseStatusException(HttpStatus.NOT_FOUND, ex.message, ex)
            else -> {
                val (code, message) = getError(ex)
                ResponseStatusException(code, message, ex)
            }
        }
    }
}
