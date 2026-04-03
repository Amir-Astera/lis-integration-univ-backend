package lab.dev.med.univ.feature.reagents.presentation.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.reagents.domain.services.AnalyzerReagentLinkService
import lab.dev.med.univ.feature.reagents.presentation.dto.AnalyzerReagentLinkViewDto
import lab.dev.med.univ.feature.reagents.presentation.dto.AnalyzerReagentSummaryDto
import lab.dev.med.univ.feature.reagents.presentation.dto.CreateAnalyzerReagentLinkRequestDto
import lab.dev.med.univ.feature.reagents.presentation.dto.toDto
import lab.dev.med.univ.feature.reagents.presentation.dto.toDomain
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

@RestController
@RequestMapping("/api/reagents/analyzers/{analyzerId}/reagent-links")
@Tag(name = "reagents-analyzer-links", description = "Analyzer to Reagent Inventory linking API")
@SecurityRequirement(name = "security_auth")
class AnalyzerReagentLinkController(
    logger: Logger,
    private val analyzerReagentLinkService: AnalyzerReagentLinkService,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    @GetMapping
    @Operation(summary = "Get all reagent links for an analyzer")
    suspend fun getLinks(
        @PathVariable analyzerId: String,
        @RequestParam(required = false, defaultValue = "false") activeOnly: Boolean,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<AnalyzerReagentLinkViewDto>> {
        return try {
            val links = analyzerReagentLinkService.getLinksForAnalyzer(analyzerId, activeOnly)
            ResponseEntity.ok(links.map { it.toDto() })
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/summary")
    @Operation(summary = "Get analyzer reagent summary")
    suspend fun getSummary(
        @PathVariable analyzerId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<AnalyzerReagentSummaryDto> {
        return try {
            val summary = analyzerReagentLinkService.getAnalyzerSummary(analyzerId)
                ?: return ResponseEntity.notFound().build()
            ResponseEntity.ok(summary.toDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PostMapping
    @Operation(summary = "Add a reagent link to an analyzer (admin)")
    suspend fun createLink(
        @PathVariable analyzerId: String,
        @RequestBody request: CreateAnalyzerReagentLinkRequestDto,
        exchange: ServerWebExchange,
    ): ResponseEntity<Any> {
        return try {
            ensureAdmin(exchange)
            val user = getSessionUser(exchange)
            val link = analyzerReagentLinkService.createLink(request.toDomain(analyzerId), user.login)
            ResponseEntity.status(HttpStatus.CREATED).body(link.toDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @DeleteMapping("/{linkId}")
    @Operation(summary = "Remove a reagent link from an analyzer (admin)")
    suspend fun deleteLink(
        @PathVariable analyzerId: String,
        @PathVariable linkId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<Unit> {
        return try {
            ensureAdmin(exchange)
            analyzerReagentLinkService.deleteLink(linkId)
            ResponseEntity.noContent().build()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PostMapping("/{linkId}/toggle")
    @Operation(summary = "Toggle active/inactive status for a reagent link (admin)")
    suspend fun toggleActive(
        @PathVariable analyzerId: String,
        @PathVariable linkId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<Any> {
        return try {
            ensureAdmin(exchange)
            val link = analyzerReagentLinkService.toggleActive(linkId)
            ResponseEntity.ok(link.toDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PostMapping("/auto-populate")
    @Operation(summary = "Auto-populate links from inventory assigned to this analyzer (admin)")
    suspend fun autoPopulate(
        @PathVariable analyzerId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<Map<String, Any>> {
        return try {
            ensureAdmin(exchange)
            val user = getSessionUser(exchange)
            val created = analyzerReagentLinkService.autoPopulateFromNorms(analyzerId, user.login)
            ResponseEntity.ok(mapOf("analyzerId" to analyzerId, "linksCreated" to created))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    private suspend fun getSessionUser(exchange: ServerWebExchange) =
        FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()

    private suspend fun ensureAdmin(exchange: ServerWebExchange) {
        userAggregateService.checkAdminPrivilegesBySession(getSessionUser(exchange))
    }
}
