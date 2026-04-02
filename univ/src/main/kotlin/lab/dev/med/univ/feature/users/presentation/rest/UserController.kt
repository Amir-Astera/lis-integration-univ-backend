package project.gigienist_reports.feature.users.presentation.rest

import project.gigienist_reports.feature.users.presentation.dto.CreateUserDto
import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.core.config.api.CreateApiResponses
import project.gigienist_reports.core.config.api.CreateResponseDto
import project.gigienist_reports.core.config.api.OkApiResponses
import project.gigienist_reports.core.security.SessionUser
import project.gigienist_reports.core.security.firebase.FirebaseSecurityUtils
import project.gigienist_reports.feature.users.domain.models.UserAggregate
import project.gigienist_reports.feature.users.presentation.dto.AddAuthoritiesToUserDto
import project.gigienist_reports.feature.users.presentation.dto.DeleteAuthoritiesFromUserDto
import project.gigienist_reports.feature.users.presentation.dto.UpdateUserDto
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
import project.gigienist_reports.feature.users.domain.services.UserAggregateService
import project.gigienist_reports.feature.users.domain.usecases.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "users", description = "The Users API")
class UserController(
    logger: Logger,
    private val addUserUseCase: AddUserUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val addAuthoritiesToUserUseCase: AddAuthoritiesToUserUseCase,
    private val deleteAuthoritiesFromUserUseCase: DeleteAuthoritiesFromUserUseCase,
    private val getUserBySessionUseCase: GetUserBySessionUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val getAllUseCase: GetAllUseCase,
    private val userAggregateService: UserAggregateService,
//    private val getUserPatientUseCase: GetUserPatientUseCase
): Controller(logger) {

    @SecurityRequirement(name = "security_auth")
    @CreateApiResponses
    @PostMapping
    suspend fun create(
        @RequestBody createUser: CreateUserDto,
        @Parameter(hidden = true) request: ServerHttpRequest,
        @Parameter(hidden = true) exchange: ServerWebExchange,
    ): ResponseEntity<CreateResponseDto> {
        try {
            ensureAdmin(exchange)
            val response = addUserUseCase(createUser)
            return HttpStatus.CREATED.response(response, "${request.uri}/${response.id}")
        } catch (ex: Exception) {
            val (code: HttpStatus, message: String?) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

//    @SecurityRequirement(name = "security_auth")
    @ApiResponses(
            ApiResponse(responseCode = "200", description = "ok",
                    content = [Content(schema = Schema(implementation = UserAggregate::class))])
    )
    @GetMapping("/{userId}")
    suspend fun get(
        @PathVariable userId: String,
        @Parameter(hidden = true) exchange: ServerWebExchange,
    ): ResponseEntity<UserAggregate> {
        try {
            ensureAdmin(exchange)
            return HttpStatus.OK.response(getUserUseCase(userId))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @SecurityRequirement(name = "security_auth")
    @CreateApiResponses
    @PostMapping("/{userId}/authorities")
    suspend fun addAuthorities(
            @PathVariable userId: String,
            @RequestBody dto: AddAuthoritiesToUserDto,
            @Parameter(hidden = true) exchange: ServerWebExchange,
    ): ResponseEntity<CreateResponseDto> {
        try {
            ensureAdmin(exchange)
            addAuthoritiesToUserUseCase(userId, dto)
            return HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @SecurityRequirement(name = "security_auth")
    @OkApiResponses
    @PutMapping("/{id}")
    suspend fun update(
            @PathVariable id: String,
            @RequestBody dto: UpdateUserDto,
            @Parameter(hidden = true) exchange: ServerWebExchange,
    ): ResponseEntity<Void> {
        try {
            ensureAdmin(exchange)
            updateUserUseCase(id, dto)
            return HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @SecurityRequirement(name = "security_auth")
    @OkApiResponses
    @DeleteMapping("/{id}")
    suspend fun delete(
        @PathVariable id: String,
        @Parameter(hidden = true) exchange: ServerWebExchange,
    ): ResponseEntity<Void> {
        try {
            ensureAdmin(exchange)
            deleteUserUseCase(id)
            return HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @SecurityRequirement(name = "security_auth")
    @OkApiResponses
    @DeleteMapping("/{userId}/authorities")
    suspend fun deleteAuthorities(
            @PathVariable userId: String,
            @RequestBody dto: DeleteAuthoritiesFromUserDto,
            @Parameter(hidden = true) exchange: ServerWebExchange,
    ): ResponseEntity<Void> {
        try {
            ensureAdmin(exchange)
            deleteAuthoritiesFromUserUseCase(userId, dto)
            return HttpStatus.OK.response()
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @SecurityRequirement(name = "security_auth")
    @GetMapping("/current")
    suspend fun getSession(
            @Parameter(hidden = true) exchange: ServerWebExchange
    ): ResponseEntity<UserAggregate> {
        val user = getSessionUser(exchange)
        try {
            return HttpStatus.OK.response(getUserBySessionUseCase(user))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

    @SecurityRequirement(name = "security_auth")
    @GetMapping("/getAll")
    suspend fun getAll(
            @RequestParam(required = false)
            email: String?,
            @RequestParam(required = false, defaultValue = "0")
            page: Int,
            @RequestParam(required = false, defaultValue = "10")
            size: Int,
            @Parameter(hidden = true) exchange: ServerWebExchange
    ): ResponseEntity<Map<String, Any>> {
        try {
            ensureAdmin(exchange)
            return HttpStatus.OK.response(getAllUseCase(email, page, size))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message)
        }
    }

//    @GetMapping("/byPhone/{phone}")
//    suspend fun getByPhone(@PathVariable phone: String): ResponseEntity<Map<String, Any>> {
//        try {
//            return HttpStatus.OK.response(getUserPatientUseCase(phone))
//        } catch (ex: Exception) {
//            val (code, message) = getError(ex)
//            throw ResponseStatusException(code, message)
//        }
//    }

    private suspend fun getSessionUser(exchange: ServerWebExchange): SessionUser {
        return FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
    }

    private suspend fun ensureAdmin(exchange: ServerWebExchange) {
        userAggregateService.checkAdminPrivilegesBySession(getSessionUser(exchange))
    }
}