package project.gigienist_reports.feature.users.domain.usecases

import project.gigienist_reports.feature.users.domain.models.UserAggregate
import project.gigienist_reports.feature.users.domain.services.UserAggregateService
import org.springframework.stereotype.Service

interface GetUserUseCase {
    suspend operator fun invoke(userId: String): UserAggregate
}

@Service
internal class GetUserUseCaseImpl(
    private val service: UserAggregateService
) : GetUserUseCase {
    override suspend fun invoke(userId: String): UserAggregate = service.get(userId)
}