package lab.dev.med.univ.feature.reagents.domain.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import lab.dev.med.univ.feature.reagents.domain.models.ParsedAnalyzerSample
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import org.springframework.stereotype.Service
import java.io.StringReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayDeque
import java.util.UUID
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants

interface ApplogsParserService {
    fun parse(logUploadId: String, analyzerId: String?, content: String): ParsedAnalyzerLogPayload
}

interface ErrorsXmlParserService {
    fun parse(content: String): ParsedErrorsXmlPayload
}

@Service
internal class ApplogsParserServiceImpl(
    private val objectMapper: ObjectMapper,
) : ApplogsParserService {

    override fun parse(logUploadId: String, analyzerId: String?, content: String): ParsedAnalyzerLogPayload {
        val rawLines = content.split(Regex("\r?\n"))
        val events = normalizeEvents(rawLines)
        val hl7QueueByBarcode = mutableMapOf<String, ArrayDeque<Hl7SampleSnapshot>>()
        val samples = mutableListOf<SampleBuilder>()
        var activeSample: SampleBuilder? = null

        events.forEach { event ->
            if (event.timestamp != null && event.messageText.startsWith("Received bytes(encoded):")) {
                parseHl7Payload(event.messageText)
                    ?.let { snapshot ->
                        hl7QueueByBarcode.getOrPut(snapshot.barcode) { ArrayDeque() }.add(snapshot)
                    }
            }

            val sampleMatch = SAMPLE_REGEX.find(event.messageText)
            if (sampleMatch != null) {
                val barcode = sampleMatch.groupValues[1]
                val builder = SampleBuilder(
                    id = UUID.randomUUID().toString(),
                    logUploadId = logUploadId,
                    analyzerId = analyzerId,
                    sampleTimestamp = event.timestamp,
                    barcode = barcode,
                )
                pollFirstSnapshot(hl7QueueByBarcode[barcode])?.let { snapshot ->
                    builder.deviceSystemName = snapshot.deviceSystemName
                    builder.testMode = snapshot.testMode
                    builder.bloodMode = snapshot.bloodMode
                    builder.takeMode = snapshot.takeMode
                    builder.wbcValue = snapshot.wbcValue
                    builder.rbcValue = snapshot.rbcValue
                    builder.hgbValue = snapshot.hgbValue
                    builder.pltValue = snapshot.pltValue
                }
                samples.add(builder)
                activeSample = builder
                return@forEach
            }

            val current = activeSample ?: return@forEach

            THERE_IS_NO_SR_REGEX.find(event.messageText)?.let {
                current.hasLisOrder = false
                current.classificationReason = event.messageText.trim()
                current.explicitUnauthorized = true
            }

            SAMPLE_REQUEST_COUNT_REGEX.find(event.messageText)?.let {
                current.sampleRequestCount = it.groupValues[1].toIntOrNull() ?: 0
                if (current.sampleRequestCount > 0) {
                    current.hasLisOrder = true
                }
            }

            if (event.level == "Error") {
                current.errorMessages.add(event.messageText.trim())
            }
            if (event.level == "Warning") {
                current.warningMessages.add(event.messageText.trim())
            }

            if (event.messageText.startsWith("Save string:")) {
                extractPayloadBody(event.messageText, "Save string:")
                    ?.let { parseSavedJson(current, it) }
            } else if (event.messageText.startsWith("Result object to save:")) {
                extractPayloadBody(event.messageText, "Result object to save:")
                    ?.let { parseDraftJson(current, it) }
            }
        }

        val parsedSamples = samples.map { it.toModel() }
        return ParsedAnalyzerLogPayload(
            totalLinesParsed = rawLines.size,
            totalEventsParsed = events.size,
            logPeriodStart = events.mapNotNull { it.timestamp }.minOrNull(),
            logPeriodEnd = events.mapNotNull { it.timestamp }.maxOrNull(),
            samples = parsedSamples,
        )
    }

    private fun normalizeEvents(rawLines: List<String>): List<NormalizedLogEvent> {
        val events = mutableListOf<NormalizedLogEvent>()
        var current: NormalizedLogEvent? = null

        rawLines.forEachIndexed { index, rawLine ->
            val match = PREFIXED_LOG_REGEX.matchEntire(rawLine)
            if (match != null) {
                current?.let(events::add)
                current = NormalizedLogEvent(
                    lineNumber = index + 1,
                    timestamp = parseTimestamp(match.groupValues[1]),
                    level = match.groupValues[2].removeSuffix(":").trim(),
                    messageBuilder = StringBuilder(match.groupValues[3]),
                )
            } else {
                val cur = current
                if (cur == null) {
                    current = NormalizedLogEvent(
                        lineNumber = index + 1,
                        timestamp = null,
                        level = "Raw",
                        messageBuilder = StringBuilder(rawLine),
                    )
                } else {
                    if (cur.messageBuilder.isNotEmpty()) {
                        cur.messageBuilder.append('\n')
                    }
                    cur.messageBuilder.append(rawLine)
                }
            }
        }

        current?.let(events::add)
        return events
    }

    private fun parseTimestamp(value: String): LocalDateTime? {
        return runCatching { LocalDateTime.parse(value, TIMESTAMP_FORMATTER) }.getOrNull()
    }

    private fun parseHl7Payload(message: String): Hl7SampleSnapshot? {
        val payload = extractPayloadBody(message, "Received bytes(encoded):") ?: return null
        val cleaned = payload
            .replace('\u000b', ' ')
            .replace('\u001c', ' ')
            .replace('\u0000', ' ')
            .trim()
        val segments = cleaned.split('\r').map { it.trim() }.filter { it.isNotBlank() }
        var barcode: String? = null
        var deviceSystemName: String? = null
        var testMode: String? = null
        var bloodMode: String? = null
        var takeMode: String? = null
        var wbcValue: Double? = null
        var rbcValue: Double? = null
        var hgbValue: Double? = null
        var pltValue: Double? = null

        segments.forEach { segment ->
            val normalized = segment.trimStart { it <= ' ' }
            val fields = normalized.split('|')
            when {
                normalized.startsWith("OBR|") -> {
                    barcode = fields.getOrNull(3)?.takeIf { it.isNotBlank() }
                }
                normalized.startsWith("OBX|") -> {
                    val identifier = fields.getOrNull(3).orEmpty()
                    val identifierText = identifier.split('^').getOrNull(1).orEmpty()
                    val value = fields.getOrNull(5)
                    when (identifierText) {
                        "Test Mode" -> testMode = value?.takeIf { it.isNotBlank() }
                        "Blood Mode" -> bloodMode = value?.takeIf { it.isNotBlank() }
                        "Take Mode" -> takeMode = value?.takeIf { it.isNotBlank() }
                        "WBC" -> wbcValue = parseNumeric(value)
                        "RBC" -> rbcValue = parseNumeric(value)
                        "HGB" -> hgbValue = parseNumeric(value)
                        "PLT" -> pltValue = parseNumeric(value)
                    }
                }
            }
        }

        if (barcode == null) {
            return null
        }

        return Hl7SampleSnapshot(
            barcode = barcode!!,
            deviceSystemName = deviceSystemName,
            testMode = testMode,
            bloodMode = bloodMode,
            takeMode = takeMode,
            wbcValue = wbcValue,
            rbcValue = rbcValue,
            hgbValue = hgbValue,
            pltValue = pltValue,
        )
    }

    private fun parseSavedJson(builder: SampleBuilder, json: String) {
        val node = parseJson(json) ?: return
        builder.orderResearchId = node.path("OrderResearchID").longValue().takeIf { it > 0 }
        builder.deviceSystemName = builder.deviceSystemName ?: node.path("WorkPlaceID").asText(null)
        builder.lisAnalyzerId = builder.lisAnalyzerId ?: node.path("AnalyzerId").intValue().takeIf { it > 0 }

        val orderResearch = node.path("OrderResearch")
        if (!orderResearch.isMissingNode && !orderResearch.isNull) {
            builder.orderId = orderResearch.path("OrderID").longValue().takeIf { it > 0 }
            val serviceMo = orderResearch.path("ServiceMo")
            if (!serviceMo.isMissingNode && !serviceMo.isNull) {
                builder.serviceId = serviceMo.path("ServiceID").intValue().takeIf { it > 0 }
                builder.serviceName = serviceMo.path("NameRU").asText(null)
            }
        }

        val parameterNode = node.path("Parameter")
        val parameterName = sequenceOf(
            parameterNode.path("ShortNameRU").asText(null),
            parameterNode.path("NameRU").asText(null),
            parameterNode.path("ShortName").asText(null),
            parameterNode.path("Name").asText(null),
        ).firstOrNull { !it.isNullOrBlank() }

        val resultText = node.path("ResultText").asText(null)
        val sourceResultText = node.path("SourceResultText").asText(null)
        applyParameterValue(builder, parameterName, sourceResultText ?: resultText)

        if (builder.orderResearchId != null || builder.orderId != null || builder.serviceId != null) {
            builder.hasLisOrder = true
        }
    }

    private fun parseDraftJson(builder: SampleBuilder, json: String) {
        val node = parseJson(json) ?: return
        builder.deviceSystemName = builder.deviceSystemName ?: node.path("WorkPlaceID").asText(null)
        builder.lisAnalyzerId = builder.lisAnalyzerId ?: node.path("AnalyzerId").intValue().takeIf { it > 0 }
    }

    private fun parseJson(json: String): JsonNode? {
        return runCatching { objectMapper.readTree(json) }.getOrNull()
    }

    private fun applyParameterValue(builder: SampleBuilder, parameterName: String?, value: String?) {
        val normalizedName = parameterName?.lowercase()?.trim() ?: return
        val numericValue = parseNumeric(value) ?: return
        when {
            normalizedName.contains("лейкоц") || normalizedName.contains("wbc") -> builder.wbcValue = builder.wbcValue ?: numericValue
            normalizedName.contains("эритроц") || normalizedName.contains("rbc") -> builder.rbcValue = builder.rbcValue ?: numericValue
            normalizedName.contains("гемоглоб") || normalizedName.contains("hgb") -> builder.hgbValue = builder.hgbValue ?: numericValue
            normalizedName.contains("тромбоц") || normalizedName.contains("plt") -> builder.pltValue = builder.pltValue ?: numericValue
        }
    }

    private fun parseNumeric(value: String?): Double? {
        val normalized = value
            ?.trim()
            ?.replace(',', '.')
            ?.takeIf { it.isNotBlank() && it != "*****" }
            ?: return null
        return normalized.toDoubleOrNull()
    }

    private fun extractPayloadBody(message: String, prefix: String): String? {
        val body = message.removePrefix(prefix).trim()
        if (body.isNotBlank()) {
            return body
        }
        val newlineIndex = message.indexOf('\n')
        if (newlineIndex >= 0 && newlineIndex + 1 < message.length) {
            val multilineBody = message.substring(newlineIndex + 1).trim()
            if (multilineBody.isNotBlank()) {
                return multilineBody
            }
        }
        return null
    }

    private fun pollFirstSnapshot(queue: ArrayDeque<Hl7SampleSnapshot>?): Hl7SampleSnapshot? {
        if (queue == null || queue.isEmpty()) {
            return null
        }
        return queue.removeFirst()
    }

    private data class NormalizedLogEvent(
        val lineNumber: Int,
        val timestamp: LocalDateTime?,
        val level: String,
        val messageBuilder: StringBuilder,
    ) {
        val messageText: String
            get() = messageBuilder.toString()
    }

    private data class Hl7SampleSnapshot(
        val barcode: String,
        val deviceSystemName: String?,
        val testMode: String?,
        val bloodMode: String?,
        val takeMode: String?,
        val wbcValue: Double?,
        val rbcValue: Double?,
        val hgbValue: Double?,
        val pltValue: Double?,
    )

    private class SampleBuilder(
        val id: String,
        val logUploadId: String,
        val analyzerId: String?,
        sampleTimestamp: LocalDateTime?,
        val barcode: String,
    ) {
        val sampleTimestamp: LocalDateTime = sampleTimestamp ?: LocalDateTime.now()
        var deviceSystemName: String? = null
        var deviceName: String? = null
        var lisAnalyzerId: Int? = null
        var testMode: String? = null
        var bloodMode: String? = null
        var takeMode: String? = null
        var orderResearchId: Long? = null
        var orderId: Long? = null
        var serviceId: Int? = null
        var serviceName: String? = null
        var hasLisOrder: Boolean = false
        var sampleRequestCount: Int = 0
        var wbcValue: Double? = null
        var rbcValue: Double? = null
        var hgbValue: Double? = null
        var pltValue: Double? = null
        var classificationReason: String? = null
        var explicitUnauthorized: Boolean = false
        val errorMessages: MutableList<String> = mutableListOf()
        val warningMessages: MutableList<String> = mutableListOf()

        fun toModel(): ParsedAnalyzerSample {
            val classification = when {
                hasLisOrder || sampleRequestCount > 0 || orderResearchId != null -> SampleClassification.LEGITIMATE
                explicitUnauthorized && isNearZero() -> SampleClassification.WASH_TEST
                explicitUnauthorized -> SampleClassification.SUSPICIOUS
                errorMessages.isNotEmpty() -> SampleClassification.ERROR
                else -> SampleClassification.ERROR
            }
            val finalReason = classificationReason
                ?: errorMessages.firstOrNull()
                ?: warningMessages.firstOrNull()
                ?: when (classification) {
                    SampleClassification.LEGITIMATE -> "LIS order matched"
                    SampleClassification.WASH_TEST -> "Near-zero analyzer values without LIS order"
                    SampleClassification.SUSPICIOUS -> "No LIS order for sample"
                    else -> "Unable to classify sample confidently"
                }

            return ParsedAnalyzerSample(
                id = id,
                logUploadId = logUploadId,
                analyzerId = analyzerId,
                sampleTimestamp = sampleTimestamp,
                barcode = barcode,
                deviceSystemName = deviceSystemName,
                deviceName = deviceName,
                lisAnalyzerId = lisAnalyzerId,
                testMode = testMode,
                bloodMode = bloodMode,
                takeMode = takeMode,
                orderResearchId = orderResearchId,
                orderId = orderId,
                serviceId = serviceId,
                serviceName = serviceName,
                hasLisOrder = hasLisOrder,
                sampleRequestCount = sampleRequestCount,
                wbcValue = wbcValue,
                rbcValue = rbcValue,
                hgbValue = hgbValue,
                pltValue = pltValue,
                classification = classification,
                classificationReason = finalReason,
            )
        }

        private fun isNearZero(): Boolean {
            return (wbcValue != null && wbcValue!! <= 0.1) &&
                (rbcValue != null && rbcValue!! <= 0.1) &&
                (hgbValue != null && hgbValue!! <= 1.0)
        }
    }

    private companion object {
        val TIMESTAMP_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm:ss")
        val PREFIXED_LOG_REGEX = Regex("^(\\d{2}\\.\\d{2}\\.\\d{4} \\d{1,2}:\\d{2}:\\d{2})\\t([A-Za-z]+:)\\t?(.*)$")
        val SAMPLE_REGEX = Regex("SAMPLE!\\s+([^\\s]+)")
        val THERE_IS_NO_SR_REGEX = Regex("THERE IS NO SR FOR Barcode\\s+(.+)$")
        val SAMPLE_REQUEST_COUNT_REGEX = Regex("Sample request count:\\s*(\\d+)")
    }
}

@Service
internal class ErrorsXmlParserServiceImpl : ErrorsXmlParserService {

    override fun parse(content: String): ParsedErrorsXmlPayload {
        val factory = XMLInputFactory.newInstance()
        val reader = factory.createXMLStreamReader(StringReader(content))

        var currentDeviceSystemName: String? = null
        var currentDeviceName: String? = null
        var currentSampleBarcode: String? = null
        var currentResults = linkedMapOf<String, String>()
        val deviceKeys = linkedSetOf<String>()
        val samples = mutableListOf<ErrorsXmlSampleRecord>()

        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> {
                    when (reader.localName) {
                        "Device" -> {
                            currentDeviceSystemName = reader.getAttributeValue(null, "SystemName")
                            currentDeviceName = reader.getAttributeValue(null, "Name")
                            if (currentDeviceSystemName != null || currentDeviceName != null) {
                                deviceKeys.add("${currentDeviceSystemName.orEmpty()}::${currentDeviceName.orEmpty()}")
                            }
                        }
                        "Sample" -> {
                            currentSampleBarcode = reader.getAttributeValue(null, "Code")
                            currentResults = linkedMapOf()
                        }
                        "Result" -> {
                            val testCode = reader.getAttributeValue(null, "TestCode")
                            val value = reader.getAttributeValue(null, "Value")
                            if (!testCode.isNullOrBlank()) {
                                currentResults[testCode] = value.orEmpty()
                            }
                        }
                    }
                }
                XMLStreamConstants.END_ELEMENT -> {
                    if (reader.localName == "Sample") {
                        val barcode = currentSampleBarcode
                        val deviceSystemName = currentDeviceSystemName
                        val deviceName = currentDeviceName
                        if (!barcode.isNullOrBlank() && !deviceSystemName.isNullOrBlank() && !deviceName.isNullOrBlank()) {
                            samples.add(
                                ErrorsXmlSampleRecord(
                                    deviceSystemName = deviceSystemName,
                                    deviceName = deviceName,
                                    barcode = barcode,
                                    testMode = currentResults["Test Mode"],
                                    bloodMode = currentResults["Blood Mode"],
                                    takeMode = currentResults["Take Mode"],
                                    refGroup = currentResults["Ref Group"],
                                    wbcValue = parseNumeric(currentResults["WBC"]),
                                    rbcValue = parseNumeric(currentResults["RBC"]),
                                    hgbValue = parseNumeric(currentResults["HGB"]),
                                    pltValue = parseNumeric(currentResults["PLT"]),
                                    results = currentResults.toMap(),
                                ),
                            )
                        }
                        currentSampleBarcode = null
                        currentResults = linkedMapOf()
                    }
                }
            }
        }

        reader.close()

        return ParsedErrorsXmlPayload(
            deviceCount = deviceKeys.size,
            sampleCount = samples.size,
            samples = samples,
        )
    }

    private fun parseNumeric(value: String?): Double? {
        val normalized = value
            ?.trim()
            ?.replace(',', '.')
            ?.takeIf { it.isNotBlank() && it != "*****" }
            ?: return null
        return normalized.toDoubleOrNull()
    }
}
