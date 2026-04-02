package project.gigienist_reports.feature.repositories

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import project.gigienist_reports.feature.authority.data.entity.AuthorityEntity
import project.gigienist_reports.feature.authority.data.entity.UserAuthorityEntity
import project.gigienist_reports.feature.files.data.FileEntity
import project.gigienist_reports.feature.users.data.entity.UserEntity

interface AuthorityRepository : CoroutineCrudRepository<AuthorityEntity, String> {
    suspend fun findByName(name: String): AuthorityEntity?
}

interface UserRepository : CoroutineCrudRepository<UserEntity, String> {
    suspend fun findByLogin(login: String): UserEntity?

    suspend fun findByPhone(phone: String): UserEntity?

    suspend fun findByEmail(email: String): UserEntity?

    @Query(
        """
        SELECT *
        FROM users
        WHERE (:email IS NULL OR email ILIKE CONCAT('%', :email, '%'))
        ORDER BY created_at DESC
        LIMIT :size OFFSET :offset
        """
    )
    fun findUsersByEmailWithPagination(email: String?, size: Int, offset: Int): Flow<UserEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM users
        WHERE (:email IS NULL OR email ILIKE CONCAT('%', :email, '%'))
        """
    )
    suspend fun countUsersByEmail(email: String?): Long
}

interface UserAuthorityRepository : CoroutineCrudRepository<UserAuthorityEntity, String> {
    fun findAllByUserId(userId: String): Flow<UserAuthorityEntity>

    suspend fun deleteAllByUserId(userId: String)

    suspend fun deleteAllByUserIdAndAuthorityIdNotIn(userId: String, authorityIds: Collection<String>)
}

interface FileRepository : CoroutineCrudRepository<FileEntity, String>
