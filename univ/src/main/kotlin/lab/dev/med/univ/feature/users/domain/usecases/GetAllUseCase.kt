package project.gigienist_reports.feature.users.domain.usecases

import project.gigienist_reports.feature.users.domain.services.UserAggregateService
import org.springframework.stereotype.Service

interface GetAllUseCase {
    suspend operator fun invoke(email: String?, page: Int, size: Int): Map<String, Any>
}

@Service
internal class GetAllUseCaseImpl(
        private val userService: UserAggregateService
): GetAllUseCase {
    override suspend fun invoke(email: String?, page: Int, size: Int): Map<String, Any> {
       return userService.getAll(email, page, size)
    }
}