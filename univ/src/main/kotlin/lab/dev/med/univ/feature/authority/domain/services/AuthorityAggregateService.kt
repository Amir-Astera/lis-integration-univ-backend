package project.gigienist_reports.feature.authority.domain.services

import project.gigienist_reports.feature.authority.domain.errors.AuthorityNotFoundException
import project.gigienist_reports.feature.authority.domain.models.Authority
import project.gigienist_reports.feature.repositories.AuthorityRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import project.gigienist_reports.core.extension.toEntity
import project.gigienist_reports.core.extension.toModel

interface AuthorityAggregateService {
    suspend fun get(id: String): Authority
    suspend fun save(aggregate: Authority)
    suspend fun delete(id: String)
    suspend fun findByName(name: String): Authority?
    suspend fun getAll(): Collection<Authority>
    suspend fun getAllByIds(authorityIds: List<String>): Collection<Authority>
}

@Service
internal class AuthorityAggregateServiceImpl(
//    private val authorityRepository: AuthorityRepository
): AuthorityAggregateService {
    lateinit var authorityRepository: AuthorityRepository

    @Autowired
    fun initialize(authorityRepository: AuthorityRepository) {
        this.authorityRepository = authorityRepository
    }

    override suspend fun get(id: String): Authority =
        authorityRepository.findById(id)?.run { toModel() } ?: throw AuthorityNotFoundException()

    override suspend fun save(aggregate: Authority) {
        authorityRepository.save(aggregate.toEntity())
    }

    override suspend fun delete(id: String) = authorityRepository.deleteById(id)

    override suspend fun findByName(name: String): Authority? =
            authorityRepository.findByName(name)?.run { toModel() }

    override suspend fun getAll() = authorityRepository.findAll()
            .map { it.toModel() }.toList()

    override suspend fun getAllByIds(authorityIds: List<String>): Collection<Authority> {
        return authorityRepository.findAllById(authorityIds).map {
            it.toModel()
        }.toList()
    }

}