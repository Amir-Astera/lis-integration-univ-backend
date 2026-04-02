package project.gigienist_reports.feature.authority.domain.usecases

import project.gigienist_reports.feature.authority.domain.errors.AuthorityDuplicateNameException
import project.gigienist_reports.feature.authority.domain.services.AuthorityAggregateService
import project.gigienist_reports.feature.authority.presentation.dto.UpdateAuthorityDto
import org.springframework.stereotype.Service

interface UpdateAuthorityUseCase {
    suspend operator fun invoke(id: String, dto: UpdateAuthorityDto): String
}

@Service
internal class UpdateAuthorityUseCaseImpl(
    private val service: AuthorityAggregateService,
) : UpdateAuthorityUseCase {
    override suspend fun invoke(id: String, dto: UpdateAuthorityDto): String {
        val authority = service.get(id)
        val foundAuthority = service.findByName(dto.name)
        if (foundAuthority != null && foundAuthority.id != authority.id){
            throw AuthorityDuplicateNameException()
        }
        authority.update(dto.name, dto.description)
        service.save(authority)
        return authority.id
    }
}