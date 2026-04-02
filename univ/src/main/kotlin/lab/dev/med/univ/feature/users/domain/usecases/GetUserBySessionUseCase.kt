package project.gigienist_reports.feature.users.domain.usecases

import project.gigienist_reports.core.security.SessionUser
import project.gigienist_reports.feature.users.domain.errors.UserNotFoundException
import project.gigienist_reports.feature.users.domain.models.UserAggregate
import project.gigienist_reports.feature.users.domain.services.UserAggregateService
import org.springframework.stereotype.Service

interface GetUserBySessionUseCase {
	suspend operator fun invoke(sessionUser: SessionUser): UserAggregate
}

@Service
internal class GetUserBySessionUseCaseImpl(
	private val userService: UserAggregateService
) : GetUserBySessionUseCase {

	override suspend fun invoke(sessionUser: SessionUser): UserAggregate =
			userService.getByLogin(sessionUser.login) ?: throw UserNotFoundException()
}
