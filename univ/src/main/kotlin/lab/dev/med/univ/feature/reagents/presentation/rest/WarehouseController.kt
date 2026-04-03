package lab.dev.med.univ.feature.reagents.presentation.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.reagents.domain.models.CreateWarehouseMovementRequest
import lab.dev.med.univ.feature.reagents.domain.models.WarehouseDailySnapshot
import lab.dev.med.univ.feature.reagents.domain.models.WarehouseMovement
import lab.dev.med.univ.feature.reagents.domain.models.WarehouseSummary
import lab.dev.med.univ.feature.reagents.domain.services.WarehouseService
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.core.security.firebase.FirebaseSecurityUtils
import project.gigienist_reports.feature.users.domain.services.UserAggregateService
import java.time.LocalDate

@RestController
@RequestMapping("/api/reagents/warehouse")
@Tag(name = "reagents-warehouse", description = "Warehouse movements and stock management API")
@SecurityRequirement(name = "security_auth")
class WarehouseController(
    logger: Logger,
    private val warehouseService: WarehouseService,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    @GetMapping("/summary")
    @Operation(summary = "Get warehouse summary (low stock, expiry warnings, recent movements)")
    suspend fun getSummary(exchange: ServerWebExchange): ResponseEntity<WarehouseSummary> {
        return try {
            ResponseEntity.ok(warehouseService.getSummary())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/movements")
    @Operation(summary = "List warehouse movements for a date range")
    suspend fun getMovements(
        @RequestParam(required = false) from: LocalDate?,
        @RequestParam(required = false) to: LocalDate?,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<WarehouseMovement>> {
        return try {
            val end = to ?: LocalDate.now()
            val start = from ?: end.minusDays(30)
            ResponseEntity.ok(warehouseService.getMovements(start, end))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/movements/reagent/{reagentId}")
    @Operation(summary = "List movements for a specific reagent")
    suspend fun getMovementsByReagent(
        @PathVariable reagentId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<WarehouseMovement>> {
        return try {
            ResponseEntity.ok(warehouseService.getMovementsByReagent(reagentId))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/movements/consumable/{consumableId}")
    @Operation(summary = "List movements for a specific consumable")
    suspend fun getMovementsByConsumable(
        @PathVariable consumableId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<WarehouseMovement>> {
        return try {
            ResponseEntity.ok(warehouseService.getMovementsByConsumable(consumableId))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/alerts/low-stock")
    @Operation(summary = "Get items with low stock flag")
    suspend fun getLowStockItems(exchange: ServerWebExchange): ResponseEntity<List<WarehouseDailySnapshot>> {
        return try {
            ResponseEntity.ok(warehouseService.getLowStockItems())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/alerts/expiry")
    @Operation(summary = "Get items with expiry warnings")
    suspend fun getExpiryWarningItems(exchange: ServerWebExchange): ResponseEntity<List<WarehouseDailySnapshot>> {
        return try {
            ResponseEntity.ok(warehouseService.getExpiryWarningItems())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PostMapping("/movements")
    @Operation(summary = "Record a new warehouse movement (admin)")
    suspend fun createMovement(
        @RequestBody request: CreateWarehouseMovementRequest,
        exchange: ServerWebExchange,
    ): ResponseEntity<WarehouseMovement> {
        return try {
            ensureAdmin(exchange)
            val sessionUser = FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
            val movement = warehouseService.createMovement(request, sessionUser.login)
            ResponseEntity.status(HttpStatus.CREATED).body(movement)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @DeleteMapping("/movements/{id}")
    @Operation(summary = "Delete a warehouse movement (admin)")
    suspend fun deleteMovement(
        @PathVariable id: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<Map<String, String>> {
        return try {
            ensureAdmin(exchange)
            warehouseService.deleteMovement(id)
            ResponseEntity.ok(mapOf("status" to "deleted", "id" to id))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    private suspend fun ensureAdmin(exchange: ServerWebExchange) {
        val sessionUser = FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
        userAggregateService.checkAdminPrivilegesBySession(sessionUser)
    }
}
