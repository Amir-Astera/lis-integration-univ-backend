package project.gigienist_reports.feature.users.domain.usecases

import project.gigienist_reports.feature.authority.domain.errors.AuthorityNotFoundException
import project.gigienist_reports.feature.authority.domain.services.AuthorityAggregateService
import project.gigienist_reports.feature.users.domain.errors.UserDuplicateLoginException
import project.gigienist_reports.feature.users.domain.services.UserAggregateService
import project.gigienist_reports.feature.users.presentation.dto.UpdateUserDto
import org.springframework.stereotype.Service

interface UpdateUserUseCase {
    suspend operator fun invoke(id: String, dto: UpdateUserDto): String
}

@Service
internal class UpdateUserUseCaseImpl(
    private val userService: UserAggregateService,
    private val authorityService: AuthorityAggregateService,
) : UpdateUserUseCase {
    override suspend fun invoke(id: String, dto: UpdateUserDto): String {

        if (userService.existsWithPhone(dto.phone) || userService.existsWithEmail(dto.email)) {
            throw UserDuplicateLoginException()
        }

        val user = userService.get(id)

        user.update(dto)

        val authorityIds = dto.authorityIds?.map { it } ?: emptyList()
        if (authorityIds.isNotEmpty()) {
            val authorities = authorityService.getAllByIds(authorityIds)
            if (authorities.size != authorityIds.size) {
                throw AuthorityNotFoundException()
            }
            user.updateAuthorities(authorityIds)
        }
        userService.save(user)
        return user.id
    }
}