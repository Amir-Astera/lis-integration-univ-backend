package lab.dev.med.univ.feature.reagents.domain.services

import lab.dev.med.univ.feature.reagents.domain.models.ParsedAnalyzerSample
import java.time.LocalDateTime

data class ParsedAnalyzerLogPayload(
    val totalLinesParsed: Int,
    val totalEventsParsed: Int,
    val logPeriodStart: LocalDateTime?,
    val logPeriodEnd: LocalDateTime?,
    val samples: List<ParsedAnalyzerSample>,
)

data class ParsedErrorsXmlPayload(
    val deviceCount: Int,
    val sampleCount: Int,
    val samples: List<ErrorsXmlSampleRecord>,
)

data class ErrorsXmlSampleRecord(
    val deviceSystemName: String,
    val deviceName: String,
    val barcode: String,
    val testMode: String? = null,
    val bloodMode: String? = null,
    val takeMode: String? = null,
    val refGroup: String? = null,
    val wbcValue: Double? = null,
    val rbcValue: Double? = null,
    val hgbValue: Double? = null,
    val pltValue: Double? = null,
    val results: Map<String, String>,
)
