package lab.dev.med.univ.feature.director.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.director.data.entity.DirectorAssistantMessageEntity
import lab.dev.med.univ.feature.director.data.repository.DirectorAssistantMessageRepository
import lab.dev.med.univ.feature.director.presentation.dto.DirectorAssistantAnswerDto
import lab.dev.med.univ.feature.director.presentation.dto.DirectorAssistantEvidenceSnapshotDto
import lab.dev.med.univ.feature.director.presentation.dto.DirectorAssistantHistoryMessageDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@Service
class DirectorAssistantHistoryService(
    private val repository: DirectorAssistantMessageRepository,
    private val objectMapper: ObjectMapper,
) {
    suspend fun appendExchange(
        userLogin: String,
        question: String,
        answer: DirectorAssistantAnswerDto,
    ) {
        val now = LocalDateTime.now()
        repository.save(
            DirectorAssistantMessageEntity(
                id = UUID.randomUUID().toString(),
                userLogin = userLogin,
                messageRole = USER_ROLE,
                content = question,
                createdAt = now,
            ),
        )
        repository.save(
            DirectorAssistantMessageEntity(
                id = UUID.randomUUID().toString(),
                userLogin = userLogin,
                messageRole = ASSISTANT_ROLE,
                content = answer.answer,
                model = answer.model,
                evidenceGeneratedAt = OffsetDateTime.parse(answer.evidenceGeneratedAt).toLocalDateTime(),
                evidenceScopeJson = objectMapper.writeValueAsString(answer.evidenceScope),
                evidenceSnapshotJson = objectMapper.writeValueAsString(answer.evidenceSnapshot),
                createdAt = LocalDateTime.now(),
            ),
        )
    }

    suspend fun history(userLogin: String, limit: Int): List<DirectorAssistantHistoryMessageDto> =
        repository.findAllByUserLoginOrderByCreatedAtDesc(userLogin)
            .toList()
            .take(limit.coerceIn(1, MAX_HISTORY_MESSAGES))
            .asReversed()
            .map(::toDto)

    suspend fun clear(userLogin: String) {
        repository.deleteAllByUserLogin(userLogin)
    }

    private fun toDto(entity: DirectorAssistantMessageEntity): DirectorAssistantHistoryMessageDto =
        DirectorAssistantHistoryMessageDto(
            id = entity.id,
            role = entity.messageRole,
            content = entity.content,
            model = entity.model,
            createdAt = entity.createdAt.toString(),
            evidenceGeneratedAt = entity.evidenceGeneratedAt?.toString(),
            evidenceScope = entity.evidenceScopeJson
                ?.let { runCatching { objectMapper.readValue(it, Array<String>::class.java).toList() }.getOrDefault(emptyList()) }
                .orEmpty(),
            evidenceSnapshot = entity.evidenceSnapshotJson
                ?.let { runCatching { objectMapper.readValue(it, DirectorAssistantEvidenceSnapshotDto::class.java) }.getOrNull() },
        )

    private companion object {
        const val USER_ROLE = "USER"
        const val ASSISTANT_ROLE = "ASSISTANT"
        const val MAX_HISTORY_MESSAGES = 100
    }
}
