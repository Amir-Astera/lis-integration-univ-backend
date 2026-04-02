package lab.dev.med.univ.feature.reagents.domain.errors

import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogSourceType

class AnalyzerNotFoundException(analyzerId: String) : RuntimeException(
    "Analyzer '$analyzerId' was not found.",
)

class AnalyzerReagentRateNotFoundException(rateId: String) : RuntimeException(
    "Analyzer reagent rate '$rateId' was not found.",
)

class ReagentInventoryNotFoundException(inventoryId: String) : RuntimeException(
    "Reagent inventory '$inventoryId' was not found.",
)

class ConsumableInventoryNotFoundException(inventoryId: String) : RuntimeException(
    "Consumable inventory '$inventoryId' was not found.",
)

class ReagentConsumptionReportNotFoundException(reportId: String) : RuntimeException(
    "Reagent consumption report '$reportId' was not found.",
)

class ReagentModuleValidationException(message: String) : RuntimeException(message)

class AnalyzerLogValidationException(message: String) : RuntimeException(message)

class AnalyzerLogUploadNotFoundException(uploadId: String) : RuntimeException(
    "Analyzer log upload '$uploadId' was not found.",
)

class AnalyzerLogParseUnsupportedException(sourceType: AnalyzerLogSourceType) : RuntimeException(
    "Parsing persistence pipeline for source type '$sourceType' is not enabled yet.",
)

class ServiceReagentNormNotFoundException(normId: String) : RuntimeException(
    "Service reagent consumption norm '$normId' was not found.",
)

class ServiceReagentNormValidationException(message: String) : RuntimeException(message)
