package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reagents.data.entity.toEntity
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerReagentLinkRepository
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerRepository
import lab.dev.med.univ.feature.reagents.data.repository.ReagentInventoryRepository
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerReagentLink
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerReagentLinkView
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerReagentSummary
import lab.dev.med.univ.feature.reagents.domain.models.CreateAnalyzerReagentLinkRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

interface AnalyzerReagentLinkService {
    suspend fun getLinksForAnalyzer(analyzerId: String, activeOnly: Boolean = false): List<AnalyzerReagentLinkView>
    suspend fun getLinksForReagent(reagentInventoryId: String): List<AnalyzerReagentLink>
    suspend fun getAnalyzerSummary(analyzerId: String): AnalyzerReagentSummary?
    suspend fun createLink(request: CreateAnalyzerReagentLinkRequest, createdBy: String?): AnalyzerReagentLink
    suspend fun deleteLink(linkId: String)
    suspend fun toggleActive(linkId: String): AnalyzerReagentLink
    /** Auto-populate links from service_reagent_consumption_norms for an analyzer. */
    suspend fun autoPopulateFromNorms(analyzerId: String, createdBy: String?): Int
}

@Service
class AnalyzerReagentLinkServiceImpl(
    private val linkRepository: AnalyzerReagentLinkRepository,
    private val analyzerRepository: AnalyzerRepository,
    private val reagentInventoryRepository: ReagentInventoryRepository,
) : AnalyzerReagentLinkService {

    override suspend fun getLinksForAnalyzer(analyzerId: String, activeOnly: Boolean): List<AnalyzerReagentLinkView> {
        val links = if (activeOnly) {
            linkRepository.findAllByAnalyzerIdAndIsActiveTrueOrderByUsageRoleAsc(analyzerId).toList()
        } else {
            linkRepository.findAllByAnalyzerIdOrderByUsageRoleAscCreatedAtAsc(analyzerId).toList()
        }.map { it.toModel() }

        val analyzer = analyzerRepository.findById(analyzerId) ?: return emptyList()
        val reagentIds = links.map { it.reagentInventoryId }.toSet()
        val reagents = reagentIds.mapNotNull { id -> reagentInventoryRepository.findById(id) }
            .associateBy { it.id }

        return links.map { link ->
            val reagent = reagents[link.reagentInventoryId]
            AnalyzerReagentLinkView(
                link = link,
                analyzerName = analyzer.name,
                reagentName = reagent?.reagentName ?: link.normReagentName ?: "Неизвестный реагент",
                reagentLotNumber = reagent?.lotNumber,
                reagentStatus = reagent?.status?.name,
                totalVolumeMl = reagent?.totalVolumeMl,
                totalUnits = reagent?.totalUnits,
                unitType = reagent?.unitType?.name ?: "UNITS",
            )
        }
    }

    override suspend fun getLinksForReagent(reagentInventoryId: String): List<AnalyzerReagentLink> {
        return linkRepository.findAllByReagentInventoryIdOrderByCreatedAtAsc(reagentInventoryId)
            .toList()
            .map { it.toModel() }
    }

    override suspend fun getAnalyzerSummary(analyzerId: String): AnalyzerReagentSummary? {
        val analyzer = analyzerRepository.findById(analyzerId) ?: return null
        val links = getLinksForAnalyzer(analyzerId, activeOnly = false)
        return AnalyzerReagentSummary(
            analyzerId = analyzerId,
            analyzerName = analyzer.name,
            links = links,
            totalActiveLinks = links.count { it.link.isActive },
        )
    }

    override suspend fun createLink(request: CreateAnalyzerReagentLinkRequest, createdBy: String?): AnalyzerReagentLink {
        val existing = linkRepository.findByAnalyzerIdAndReagentInventoryIdAndUsageRole(
            request.analyzerId,
            request.reagentInventoryId,
            request.usageRole.name,
        )
        if (existing != null) {
            // Reactivate if it was disabled
            val updated = existing.toModel().copy(isActive = true, updatedAt = LocalDateTime.now())
            return linkRepository.save(updated.toEntity()).toModel()
        }

        val link = AnalyzerReagentLink(
            id = UUID.randomUUID().toString(),
            analyzerId = request.analyzerId,
            reagentInventoryId = request.reagentInventoryId,
            usageRole = request.usageRole,
            normReagentName = request.normReagentName,
            estimatedDailyMl = request.estimatedDailyMl,
            estimatedDailyUnits = request.estimatedDailyUnits,
            isActive = true,
            notes = request.notes,
            createdBy = createdBy,
        )
        return linkRepository.save(link.toEntity()).toModel()
    }

    override suspend fun deleteLink(linkId: String) {
        linkRepository.deleteById(linkId)
    }

    override suspend fun toggleActive(linkId: String): AnalyzerReagentLink {
        val entity = linkRepository.findById(linkId)
            ?: error("AnalyzerReagentLink not found: $linkId")
        val updated = entity.toModel().copy(isActive = !entity.isActive, updatedAt = LocalDateTime.now())
        return linkRepository.save(updated.toEntity()).toModel()
    }

    override suspend fun autoPopulateFromNorms(analyzerId: String, createdBy: String?): Int {
        // Find all reagent inventory items that are assigned to this analyzer
        val reagents = reagentInventoryRepository.findAllByAnalyzerIdOrderByReceivedAtDescCreatedAtDesc(analyzerId).toList()
        var created = 0
        for (reagent in reagents) {
            val existing = linkRepository.findByAnalyzerIdAndReagentInventoryIdAndUsageRole(
                analyzerId, reagent.id, "MAIN"
            )
            if (existing == null) {
                val link = AnalyzerReagentLink(
                    id = UUID.randomUUID().toString(),
                    analyzerId = analyzerId,
                    reagentInventoryId = reagent.id,
                    normReagentName = reagent.reagentName,
                    createdBy = createdBy,
                )
                linkRepository.save(link.toEntity())
                created++
            }
        }
        return created
    }
}
