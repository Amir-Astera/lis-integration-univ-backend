package project.gigienist_reports.feature.users.domain.usecases

import project.gigienist_reports.core.config.api.CreateResponseDto
import project.gigienist_reports.feature.authority.domain.errors.AuthorityNotFoundException
import project.gigienist_reports.feature.authority.domain.services.AuthorityAggregateService
import project.gigienist_reports.feature.users.domain.errors.UserDuplicateLoginException
import project.gigienist_reports.feature.users.domain.models.UserAggregate
import project.gigienist_reports.feature.users.domain.services.UserIdentityProvisioningService
import project.gigienist_reports.feature.users.domain.services.UserAggregateService
import org.springframework.stereotype.Service
import project.gigienist_reports.feature.users.presentation.dto.CreateUserDto

interface AddUserUseCase {
    suspend operator fun invoke(dto: CreateUserDto): CreateResponseDto
}

@Service
internal class AddUserUseCaseImpl(
    private val userService: UserAggregateService,
    private val authorityService: AuthorityAggregateService,
    private val userIdentityProvisioningService: UserIdentityProvisioningService
) : AddUserUseCase {
    override suspend fun invoke(dto: CreateUserDto): CreateResponseDto {

        if (userService.existsWithLogin(dto.login) || userService.existsWithPhone(dto.phone) || userService.existsWithEmail(dto.email)) {
            throw UserDuplicateLoginException()
        }
        val user = UserAggregate(
            name = dto.name,
            login = dto.login ?: (dto.email ?: dto.phone ?: ""),
            surname = dto.surname,
            email = dto.email,
            phone = dto.phone
        )
        val authorityIds = dto.authorityIds?.map { it } ?: emptyList()
        if (authorityIds.isNotEmpty()) {
            val authorities = authorityService.getAllByIds(authorityIds)
            if (authorities.size != authorityIds.size) {
                throw AuthorityNotFoundException()
            }
            authorityIds.forEach { user.addAuthority(it) }
        }
        userIdentityProvisioningService.createUser(user.id, user.email, dto.password)
        userService.save(user)
        return CreateResponseDto(user.id)
    }
}
