package project.gigienist_reports.feature.authority.domain.usecases

import project.gigienist_reports.feature.authority.domain.models.Authority
import project.gigienist_reports.feature.authority.domain.services.AuthorityAggregateService
import org.springframework.stereotype.Service

interface GetAllAuthorityInfoUseCase {
    suspend operator fun invoke(): Collection<Authority>
}

@Service
internal class GetAllAuthorityInfoUseCaseImpl(
    private val service: AuthorityAggregateService
) : GetAllAuthorityInfoUseCase {
    override suspend fun invoke(): Collection<Authority> = service.getAll()
}