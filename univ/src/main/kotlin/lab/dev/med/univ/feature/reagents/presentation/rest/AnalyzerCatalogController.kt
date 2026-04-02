package lab.dev.med.univ.feature.reagents.presentation.rest

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogValidationException
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerReagentRateNotFoundException
import lab.dev.med.univ.feature.reagents.domain.usecases.DeleteAnalyzerRateUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.DeleteAnalyzerUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetAnalyzerRatesUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetAnalyzerUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetAnalyzersUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.UpsertAnalyzerRateUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.UpsertAnalyzerUseCase
import lab.dev.med.univ.feature.reagents.presentation.dto.AnalyzerReagentRateResponseDto
import lab.dev.med.univ.feature.reagents.presentation.dto.AnalyzerResponseDto
import lab.dev.med.univ.feature.reagents.presentation.dto.UpsertAnalyzerReagentRateRequestDto
import lab.dev.med.univ.feature.reagents.presentation.dto.UpsertAnalyzerRequestDto
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
@RequestMapping("/api/reagents/analyzers")
@Tag(name = "reagents-analyzers", description = "Analyzer directory and reagent rate catalog API")
@SecurityRequirement(name = "security_auth")
class AnalyzerCatalogController(
    logger: Logger,
    private val getAnalyzersUseCase: GetAnalyzersUseCase,
    private val getAnalyzerUseCase: GetAnalyzerUseCase,
    private val getAnalyzerRatesUseCase: GetAnalyzerRatesUseCase,
    private val upsertAnalyzerUseCase: UpsertAnalyzerUseCase,
    private val upsertAnalyzerRateUseCase: UpsertAnalyzerRateUseCase,
    private val deleteAnalyzerRateUseCase: DeleteAnalyzerRateUseCase,
    private val deleteAnalyzerUseCase: DeleteAnalyzerUseCase,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    @GetMapping
    suspend fun getAnalyzers(
        @RequestParam(required = false, defaultValue = "false") activeOnly: Boolean,
    ): ResponseEntity<List<AnalyzerResponseDto>> {
        return try {
            logger.info("Getting analyzers with activeOnly: $activeOnly")
            val result = getAnalyzersUseCase(activeOnly).map { it.toResponseDto() }
            logger.info("Found ${result.size} analyzers")
            ResponseEntity.ok(result)
        } catch (ex: Exception) {
            logger.error("Error getting analyzers", ex)
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/{analyzerId}")
    suspend fun getAnalyzer(
        @PathVariable analyzerId: String,
    ): ResponseEntity<AnalyzerResponseDto> {
        return try {
            ResponseEntity.ok(getAnalyzerUseCase(analyzerId).toResponseDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/{analyzerId}/rates")
    suspend fun getAnalyzerRates(
        @PathVariable analyzerId: String,
    ): ResponseEntity<List<AnalyzerReagentRateResponseDto>> {
        return try {
            ResponseEntity.ok(getAnalyzerRatesUseCase(analyzerId).map { it.toResponseDto() })
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PutMapping("/{analyzerId}")
    suspend fun upsertAnalyzer(
        @PathVariable analyzerId: String,
        @RequestBody dto: UpsertAnalyzerRequestDto,
        exchange: ServerWebExchange,
    ): ResponseEntity<AnalyzerResponseDto> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(upsertAnalyzerUseCase(analyzerId, dto.toModel(analyzerId)).toResponseDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PutMapping("/{analyzerId}/rates/{rateId}")
    suspend fun upsertAnalyzerRate(
        @PathVariable analyzerId: String,
        @PathVariable rateId: String,
        @RequestBody dto: UpsertAnalyzerReagentRateRequestDto,
        exchange: ServerWebExchange,
    ): ResponseEntity<AnalyzerReagentRateResponseDto> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(
                upsertAnalyzerRateUseCase(analyzerId, rateId, dto.toModel(analyzerId, rateId)).toResponseDto(),
            )
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @DeleteMapping("/{analyzerId}")
    suspend fun deleteAnalyzer(
        @PathVariable analyzerId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<Void> {
        return try {
            ensureAdmin(exchange)
            deleteAnalyzerUseCase(analyzerId)
            HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @DeleteMapping("/{analyzerId}/rates/{rateId}")
    suspend fun deleteAnalyzerRate(
        @PathVariable analyzerId: String,
        @PathVariable rateId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<Void> {
        return try {
            ensureAdmin(exchange)
            deleteAnalyzerRateUseCase(analyzerId, rateId)
            HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    private suspend fun getSessionUser(exchange: ServerWebExchange): SessionUser {
        return FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
    }

    private suspend fun ensureAdmin(exchange: ServerWebExchange) {
        userAggregateService.checkAdminPrivilegesBySession(getSessionUser(exchange))
    }

    private fun mapException(ex: Exception): ResponseStatusException {
        return when (ex) {
            is AnalyzerLogValidationException -> ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message, ex)
            is AnalyzerNotFoundException -> ResponseStatusException(HttpStatus.NOT_FOUND, ex.message, ex)
            is AnalyzerReagentRateNotFoundException -> ResponseStatusException(HttpStatus.NOT_FOUND, ex.message, ex)
            else -> {
                val (code, message) = getError(ex)
                ResponseStatusException(code, message, ex)
            }
        }
    }
}
