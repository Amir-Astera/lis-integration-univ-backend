package lab.dev.med.univ.feature.reagents.presentation.dto

import lab.dev.med.univ.feature.reagents.domain.models.Analyzer
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerReagentRate
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerType
import lab.dev.med.univ.feature.reagents.domain.models.ReagentOperationType
import lab.dev.med.univ.feature.reagents.domain.models.ReagentUnitType
import java.time.LocalDateTime

data class AnalyzerResponseDto(
    val id: String,
    val name: String,
    val type: AnalyzerType,
    val workplaceName: String,
    val lisDeviceSystemName: String?,
    val lisAnalyzerId: Int?,
    val lisDeviceName: String?,
    val serialNumber: String?,
    val isActive: Boolean,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class UpsertAnalyzerRequestDto(
    val name: String,
    val type: AnalyzerType,
    val workplaceName: String,
    val lisDeviceSystemName: String? = null,
    val lisAnalyzerId: Int? = null,
    val lisDeviceName: String? = null,
    val serialNumber: String? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
)

data class AnalyzerReagentRateResponseDto(
    val id: String,
    val analyzerId: String,
    val reagentName: String,
    val operationType: ReagentOperationType,
    val testMode: String?,
    val volumePerOperationMl: Double?,
    val unitsPerOperation: Int?,
    val unitType: ReagentUnitType,
    val sourceDocument: String?,
    val notes: String?,
    val createdAt: LocalDateTime,
)

data class UpsertAnalyzerReagentRateRequestDto(
    val reagentName: String,
    val operationType: ReagentOperationType,
    val testMode: String? = null,
    val volumePerOperationMl: Double? = null,
    val unitsPerOperation: Int? = null,
    val unitType: ReagentUnitType,
    val sourceDocument: String? = null,
    val notes: String? = null,
)

fun Analyzer.toResponseDto() = AnalyzerResponseDto(
    id = id,
    name = name,
    type = type,
    workplaceName = workplaceName,
    lisDeviceSystemName = lisDeviceSystemName,
    lisAnalyzerId = lisAnalyzerId,
    lisDeviceName = lisDeviceName,
    serialNumber = serialNumber,
    isActive = isActive,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun UpsertAnalyzerRequestDto.toModel(id: String) = Analyzer(
    id = id,
    name = name,
    type = type,
    workplaceName = workplaceName,
    lisDeviceSystemName = lisDeviceSystemName,
    lisAnalyzerId = lisAnalyzerId,
    lisDeviceName = lisDeviceName,
    serialNumber = serialNumber,
    isActive = isActive,
    notes = notes,
)

fun AnalyzerReagentRate.toResponseDto() = AnalyzerReagentRateResponseDto(
    id = id,
    analyzerId = analyzerId,
    reagentName = reagentName,
    operationType = operationType,
    testMode = testMode,
    volumePerOperationMl = volumePerOperationMl,
    unitsPerOperation = unitsPerOperation,
    unitType = unitType,
    sourceDocument = sourceDocument,
    notes = notes,
    createdAt = createdAt,
)

fun UpsertAnalyzerReagentRateRequestDto.toModel(analyzerId: String, rateId: String) = AnalyzerReagentRate(
    id = rateId,
    analyzerId = analyzerId,
    reagentName = reagentName,
    operationType = operationType,
    testMode = testMode,
    volumePerOperationMl = volumePerOperationMl,
    unitsPerOperation = unitsPerOperation,
    unitType = unitType,
    sourceDocument = sourceDocument,
    notes = notes,
)
