package lab.dev.med.univ.feature.director.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.director.data.entity.DirectorAssistantMessageEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface DirectorAssistantMessageRepository : CoroutineCrudRepository<DirectorAssistantMessageEntity, String> {
    fun findAllByUserLoginOrderByCreatedAtDesc(userLogin: String): Flow<DirectorAssistantMessageEntity>

    @Query("DELETE FROM director_assistant_messages WHERE user_login = :userLogin")
    suspend fun deleteAllByUserLogin(userLogin: String): Int
}
