package project.gigienist_reports.feature.authority.presentation.rest

import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.core.config.api.CreateApiResponses
import project.gigienist_reports.core.config.api.CreateResponseDto
import project.gigienist_reports.core.config.api.OkApiResponses
import project.gigienist_reports.feature.authority.domain.models.Authority
import project.gigienist_reports.feature.authority.domain.usecases.AddAuthorityUseCase
import project.gigienist_reports.feature.authority.domain.usecases.DeleteAuthorityUseCase
import project.gigienist_reports.feature.authority.domain.usecases.GetAllAuthorityInfoUseCase
import project.gigienist_reports.feature.authority.domain.usecases.UpdateAuthorityUseCase
import project.gigienist_reports.feature.authority.presentation.dto.CreateAuthorityDto
import project.gigienist_reports.feature.authority.presentation.dto.UpdateAuthorityDto
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import project.gigienist_reports.core.security.SessionUser
import project.gigienist_reports.core.security.firebase.FirebaseSecurityUtils
import project.gigienist_reports.feature.users.domain.services.UserAggregateService

@RestController
@RequestMapping("/api/authorities")
@Tag(name = "authorities", description = "The Authorities API")
@SecurityRequirement(name = "security_auth")
class AuthorityController(
    logger: Logger,
    private val addAuthorityUseCase: AddAuthorityUseCase,
    private val updateAuthorityUseCase: UpdateAuthorityUseCase,
    private val getAllAuthorityInfoUseCase: GetAllAuthorityInfoUseCase,
    private val deleteAuthorityUseCase: DeleteAuthorityUseCase,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    @CreateApiResponses
    @PostMapping
    suspend fun create(
        @RequestBody createAuthority: CreateAuthorityDto,
        @Parameter(hidden = true) request: ServerHttpRequest,
        @Parameter(hidden = true) exchange: ServerWebExchange,
    ): ResponseEntity<CreateResponseDto> {
        try {
            ensureAdmin(exchange)
            val response = addAuthorityUseCase(createAuthority)
            return HttpStatus.CREATED.response(response, "${request.uri}/${response.id}")
        } catch (ex: Exception) {
            val (code: HttpStatus, message: String?) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @GetMapping
    suspend fun getAll(): ResponseEntity<Collection<Authority>> {
        return try {
            HttpStatus.OK.response(getAllAuthorityInfoUseCase())
        } catch (ex: Exception){
            val (code,message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @OkApiResponses
    @PutMapping("/{id}")
    suspend fun updateAuthority(
        @PathVariable id: String,
        @RequestBody updateAuthorityDto: UpdateAuthorityDto,
        @Parameter(hidden = true) exchange: ServerWebExchange,
    ): ResponseEntity<Void> {
        try {
            ensureAdmin(exchange)
            updateAuthorityUseCase(id, updateAuthorityDto)
            return HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @OkApiResponses
    @DeleteMapping("{id}")
    suspend fun delete(
        @PathVariable id: String,
        @Parameter(hidden = true) exchange: ServerWebExchange,
    ): ResponseEntity<Void> {
        try {
            ensureAdmin(exchange)
            deleteAuthorityUseCase(id)
            return HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    private suspend fun getSessionUser(exchange: ServerWebExchange): SessionUser {
        return FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
    }

    private suspend fun ensureAdmin(exchange: ServerWebExchange) {
        userAggregateService.checkAdminPrivilegesBySession(getSessionUser(exchange))
    }

}
