package lab.dev.med.univ.feature.reagents.domain.usecases

import lab.dev.med.univ.feature.reagents.domain.models.CalculateDamumedConsumptionRequest
import lab.dev.med.univ.feature.reagents.domain.models.DamumedConsumptionCalculationResult
import lab.dev.med.univ.feature.reagents.domain.models.DamumedReportReagentConsumption
import lab.dev.med.univ.feature.reagents.domain.services.DamumedReagentConsumptionCalculator
import org.springframework.stereotype.Service

/**
 * Use case for calculating reagent consumption from a Damumed report.
 */
interface CalculateDamumedReagentConsumptionUseCase {
    suspend operator fun invoke(request: CalculateDamumedConsumptionRequest): DamumedConsumptionCalculationResult
}

/**
 * Use case for retrieving already calculated consumption for a Damumed report.
 */
interface GetDamumedReagentConsumptionUseCase {
    suspend operator fun invoke(uploadId: String): List<DamumedReportReagentConsumption>
}

/**
 * Use case for recalculating consumption (deletes old, calculates new).
 */
interface RecalculateDamumedReagentConsumptionUseCase {
    suspend operator fun invoke(uploadId: String): DamumedConsumptionCalculationResult
}

@Service
internal class CalculateDamumedReagentConsumptionUseCaseImpl(
    private val calculator: DamumedReagentConsumptionCalculator,
) : CalculateDamumedReagentConsumptionUseCase {
    override suspend fun invoke(request: CalculateDamumedConsumptionRequest): DamumedConsumptionCalculationResult {
        return calculator.calculate(request)
    }
}

@Service
internal class GetDamumedReagentConsumptionUseCaseImpl(
    private val calculator: DamumedReagentConsumptionCalculator,
) : GetDamumedReagentConsumptionUseCase {
    override suspend fun invoke(uploadId: String): List<DamumedReportReagentConsumption> {
        return calculator.getCalculatedConsumption(uploadId)
    }
}

@Service
internal class RecalculateDamumedReagentConsumptionUseCaseImpl(
    private val calculator: DamumedReagentConsumptionCalculator,
) : RecalculateDamumedReagentConsumptionUseCase {
    override suspend fun invoke(uploadId: String): DamumedConsumptionCalculationResult {
        return calculator.recalculate(uploadId)
    }
}
