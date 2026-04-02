package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reagents.data.entity.toEntity
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerReagentRateRepository
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerRepository
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerReagentRateNotFoundException
import lab.dev.med.univ.feature.reagents.domain.models.Analyzer
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerReagentRate
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface AnalyzerCatalogQueryService {
    suspend fun getAnalyzers(activeOnly: Boolean = false): List<Analyzer>
    suspend fun getAnalyzer(analyzerId: String): Analyzer
    suspend fun getAnalyzerRates(analyzerId: String): List<AnalyzerReagentRate>
}

interface AnalyzerCatalogCommandService {
    suspend fun upsertAnalyzer(analyzerId: String, analyzer: Analyzer): Analyzer
    suspend fun upsertAnalyzerRate(analyzerId: String, rateId: String, rate: AnalyzerReagentRate): AnalyzerReagentRate
    suspend fun deleteAnalyzerRate(analyzerId: String, rateId: String)
    suspend fun deleteAnalyzer(analyzerId: String)
}

@Service
internal class AnalyzerCatalogQueryServiceImpl(
    private val analyzerRepository: AnalyzerRepository,
    private val analyzerReagentRateRepository: AnalyzerReagentRateRepository,
) : AnalyzerCatalogQueryService {

    override suspend fun getAnalyzers(activeOnly: Boolean): List<Analyzer> {
        val flow = if (activeOnly) {
            analyzerRepository.findAllByIsActiveTrueOrderByNameAsc()
        } else {
            analyzerRepository.findAllByOrderByNameAsc()
        }
        return flow.toList().map { it.toModel() }
    }

    override suspend fun getAnalyzer(analyzerId: String): Analyzer {
        return analyzerRepository.findById(analyzerId)?.toModel()
            ?: throw AnalyzerNotFoundException(analyzerId)
    }

    override suspend fun getAnalyzerRates(analyzerId: String): List<AnalyzerReagentRate> {
        if (analyzerRepository.findById(analyzerId) == null) {
            throw AnalyzerNotFoundException(analyzerId)
        }
        return analyzerReagentRateRepository.findAllByAnalyzerIdOrderByReagentNameAscOperationTypeAsc(analyzerId)
            .toList()
            .map { it.toModel() }
    }
}

@Service
internal class AnalyzerCatalogCommandServiceImpl(
    private val analyzerRepository: AnalyzerRepository,
    private val analyzerReagentRateRepository: AnalyzerReagentRateRepository,
) : AnalyzerCatalogCommandService {

    override suspend fun upsertAnalyzer(analyzerId: String, analyzer: Analyzer): Analyzer {
        val existing = analyzerRepository.findById(analyzerId)?.toModel()
        val now = LocalDateTime.now()
        val target = if (existing == null) {
            analyzer.copy(
                id = analyzerId,
                createdAt = now,
                updatedAt = now,
                version = null,
            )
        } else {
            analyzer.copy(
                id = analyzerId,
                createdAt = existing.createdAt,
                updatedAt = now,
                version = existing.version,
            )
        }
        return analyzerRepository.save(target.toEntity()).toModel()
    }

    override suspend fun upsertAnalyzerRate(analyzerId: String, rateId: String, rate: AnalyzerReagentRate): AnalyzerReagentRate {
        val analyzer = analyzerRepository.findById(analyzerId)?.toModel()
            ?: throw AnalyzerNotFoundException(analyzerId)
        val existing = analyzerReagentRateRepository.findById(rateId)?.toModel()
        val target = if (existing == null) {
            rate.copy(
                id = rateId,
                analyzerId = analyzer.id,
                createdAt = LocalDateTime.now(),
                version = null,
            )
        } else {
            rate.copy(
                id = rateId,
                analyzerId = analyzer.id,
                createdAt = existing.createdAt,
                version = existing.version,
            )
        }
        return analyzerReagentRateRepository.save(target.toEntity()).toModel()
    }

    override suspend fun deleteAnalyzerRate(analyzerId: String, rateId: String) {
        if (analyzerRepository.findById(analyzerId) == null) {
            throw AnalyzerNotFoundException(analyzerId)
        }
        val existing = analyzerReagentRateRepository.findById(rateId)?.toModel()
            ?: throw AnalyzerReagentRateNotFoundException(rateId)
        if (existing.analyzerId != analyzerId) {
            throw AnalyzerReagentRateNotFoundException(rateId)
        }
        analyzerReagentRateRepository.deleteById(rateId)
    }

    override suspend fun deleteAnalyzer(analyzerId: String) {
        if (analyzerRepository.findById(analyzerId) == null) {
            throw AnalyzerNotFoundException(analyzerId)
        }
        analyzerReagentRateRepository.deleteAllByAnalyzerId(analyzerId)
        analyzerRepository.deleteById(analyzerId)
    }
}
