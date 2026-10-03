package lab.dev.med.univ.feature.director.presentation.rest

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.director.domain.services.DirectorEvidenceAssistantService
import lab.dev.med.univ.feature.director.domain.services.DirectorAssistantHistoryService
import lab.dev.med.univ.feature.director.presentation.dto.DirectorAssistantAnswerDto
import lab.dev.med.univ.feature.director.presentation.dto.DirectorAssistantHistoryMessageDto
import lab.dev.med.univ.feature.director.presentation.dto.DirectorAssistantQuestionDto
import org.slf4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
@RequestMapping("/api/director-assistant")
@Tag(name = "director-assistant", description = "Evidence-only operational assistant for directors")
@SecurityRequirement(name = "security_auth")
class DirectorAssistantController(
    logger: Logger,
    private val directorEvidenceAssistantService: DirectorEvidenceAssistantService,
    private val historyService: DirectorAssistantHistoryService,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    @PostMapping("/ask")
    suspend fun ask(
        @RequestBody request: DirectorAssistantQuestionDto,
        exchange: ServerWebExchange,
    ): ResponseEntity<DirectorAssistantAnswerDto> {
        return try {
            val user = FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
            userAggregateService.checkAdminPrivilegesBySession(user)
            val answer = directorEvidenceAssistantService.answer(request.question, request.model)
            historyService.appendExchange(user.login, request.question.trim(), answer)
            ResponseEntity.ok(answer)
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, ex.message, ex)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/history")
    suspend fun history(
        @RequestParam(required = false, defaultValue = "60") limit: Int,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<DirectorAssistantHistoryMessageDto>> {
        return try {
            val user = FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
            userAggregateService.checkAdminPrivilegesBySession(user)
            ResponseEntity.ok(historyService.history(user.login, limit))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @DeleteMapping("/history")
    suspend fun clearHistory(exchange: ServerWebExchange): ResponseEntity<Void> {
        return try {
            val user = FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
            userAggregateService.checkAdminPrivilegesBySession(user)
            historyService.clear(user.login)
            ResponseEntity.noContent().build()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }
}
