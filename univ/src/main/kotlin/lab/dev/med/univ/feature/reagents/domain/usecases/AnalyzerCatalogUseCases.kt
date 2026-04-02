package lab.dev.med.univ.feature.reagents.domain.usecases

import lab.dev.med.univ.feature.reagents.domain.models.Analyzer
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerReagentRate
import lab.dev.med.univ.feature.reagents.domain.services.AnalyzerCatalogCommandService
import lab.dev.med.univ.feature.reagents.domain.services.AnalyzerCatalogQueryService
import org.springframework.stereotype.Service

interface GetAnalyzersUseCase {
    suspend operator fun invoke(activeOnly: Boolean = false): List<Analyzer>
}

interface GetAnalyzerUseCase {
    suspend operator fun invoke(analyzerId: String): Analyzer
}

interface GetAnalyzerRatesUseCase {
    suspend operator fun invoke(analyzerId: String): List<AnalyzerReagentRate>
}

interface UpsertAnalyzerUseCase {
    suspend operator fun invoke(analyzerId: String, analyzer: Analyzer): Analyzer
}

interface UpsertAnalyzerRateUseCase {
    suspend operator fun invoke(analyzerId: String, rateId: String, rate: AnalyzerReagentRate): AnalyzerReagentRate
}

interface DeleteAnalyzerRateUseCase {
    suspend operator fun invoke(analyzerId: String, rateId: String)
}

interface DeleteAnalyzerUseCase {
    suspend operator fun invoke(analyzerId: String)
}

@Service
internal class GetAnalyzersUseCaseImpl(
    private val queryService: AnalyzerCatalogQueryService,
) : GetAnalyzersUseCase {
    override suspend fun invoke(activeOnly: Boolean): List<Analyzer> = queryService.getAnalyzers(activeOnly)
}

@Service
internal class GetAnalyzerUseCaseImpl(
    private val queryService: AnalyzerCatalogQueryService,
) : GetAnalyzerUseCase {
    override suspend fun invoke(analyzerId: String): Analyzer = queryService.getAnalyzer(analyzerId)
}

@Service
internal class GetAnalyzerRatesUseCaseImpl(
    private val queryService: AnalyzerCatalogQueryService,
) : GetAnalyzerRatesUseCase {
    override suspend fun invoke(analyzerId: String): List<AnalyzerReagentRate> = queryService.getAnalyzerRates(analyzerId)
}

@Service
internal class UpsertAnalyzerUseCaseImpl(
    private val commandService: AnalyzerCatalogCommandService,
) : UpsertAnalyzerUseCase {
    override suspend fun invoke(analyzerId: String, analyzer: Analyzer): Analyzer = commandService.upsertAnalyzer(analyzerId, analyzer)
}

@Service
internal class UpsertAnalyzerRateUseCaseImpl(
    private val commandService: AnalyzerCatalogCommandService,
) : UpsertAnalyzerRateUseCase {
    override suspend fun invoke(analyzerId: String, rateId: String, rate: AnalyzerReagentRate): AnalyzerReagentRate {
        return commandService.upsertAnalyzerRate(analyzerId, rateId, rate)
    }
}

@Service
internal class DeleteAnalyzerRateUseCaseImpl(
    private val commandService: AnalyzerCatalogCommandService,
) : DeleteAnalyzerRateUseCase {
    override suspend fun invoke(analyzerId: String, rateId: String) = commandService.deleteAnalyzerRate(analyzerId, rateId)
}

@Service
internal class DeleteAnalyzerUseCaseImpl(
    private val commandService: AnalyzerCatalogCommandService,
) : DeleteAnalyzerUseCase {
    override suspend fun invoke(analyzerId: String) = commandService.deleteAnalyzer(analyzerId)
}
