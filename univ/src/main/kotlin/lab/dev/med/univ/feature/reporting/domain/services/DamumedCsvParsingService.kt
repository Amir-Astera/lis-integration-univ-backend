package lab.dev.med.univ.feature.reporting.domain.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedSectionEntity
import lab.dev.med.univ.feature.reporting.data.entity.toEntity
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedBatchRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedSectionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportAxisType
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportNormalizationStatus
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportParseStatus
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSchemaCatalog
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDateTime

/**
 * Describes how CSV columns (by zero-based position) map to semantic axis keys
 * for a given report kind. Generic Damumed headers like "Text25" are positional,
 * so we map by index rather than by header name.
 */
data class CsvColumnMapping(
    val reportKind: DamumedLabReportKind,
    /** Maps column index → axis key from DamumedReportSchemaCatalog */
    val axisColumns: Map<Int, String>,
    /** Column index that holds the row sequence number (skipped for normalization) */
    val sequenceColumn: Int = 0,
    /** Expected minimum column count; rows with fewer columns are skipped */
    val minColumns: Int,
)

object DamumedCsvColumnMappings {

    /**
     * ЖУРНАЛ регистрации лабораторных направлений
     * 18 columns, ';'-separated, Windows-1251
     * Col 0  = row seq
     * Col 1  = referral_number
     * Col 2  = patient_iin
     * Col 3  = patient_name
     * Col 4  = birth_date
     * Col 5  = referring_organization
     * Col 6  = patient_department
     * Col 7  = funding_source
     * Col 8  = medical_record_number (история болезни)
     * Col 9  = (амб. карта — duplicate, skip)
     * Col 10 = diagnosis
     * Col 11 = service
     * Col 12 = material
     * Col 13 = emergency_flag
     * Col 14 = referral_status
     * Col 15 = service_cost
     * Col 16 = received_at
     * Col 17 = completed_at
     */
    val REFERRAL_REGISTRATION_JOURNAL = CsvColumnMapping(
        reportKind = DamumedLabReportKind.REFERRAL_REGISTRATION_JOURNAL,
        axisColumns = mapOf(
            1 to "referral_number",
            2 to "patient_iin",
            3 to "patient_name",
            4 to "birth_date",
            5 to "referring_organization",
            6 to "patient_department",
            7 to "funding_source",
            8 to "medical_record_number",
            10 to "diagnosis",
            11 to "service",
            12 to "material",
            13 to "emergency_flag",
            14 to "referral_status",
            15 to "service_cost",
            16 to "received_at",
            17 to "completed_at",
        ),
        sequenceColumn = 0,
        minColumns = 16,
    )

    /**
     * ЖУРНАЛ регистрации выполненных лабораторных исследований
     * 26 columns, ';'-separated, Windows-1251
     * Col 0  = row seq
     * Col 1  = referral_status
     * Col 2  = patient_iin
     * Col 3  = patient_name
     * Col 4  = patient_rpn_id
     * Col 5  = birth_date
     * Col 6  = sample_collected_at
     * Col 7  = emergency_flag
     * Col 8  = organization
     * Col 9  = referring_employee
     * Col 10 = department
     * Col 11 = medical_record_number
     * Col 12 = (unused)
     * Col 13 = diagnosis
     * Col 14 = service_category
     * Col 15 = service
     * Col 16 = referral_number
     * Col 17 = completed_at
     * Col 18 = (sub-type text, skip)
     * Col 19 = result_text
     * Col 20 = (status duplicate, skip)
     * Col 21 = performer
     * Col 22 = funding_source
     * Col 23 = service_price
     * Col 24 = (quantity, not in schema)
     * Col 25 = service_total_cost (sum)
     */
    val COMPLETED_LAB_STUDIES_JOURNAL = CsvColumnMapping(
        reportKind = DamumedLabReportKind.COMPLETED_LAB_STUDIES_JOURNAL,
        axisColumns = mapOf(
            1 to "referral_status",
            2 to "patient_iin",
            3 to "patient_name",
            4 to "patient_rpn_id",
            5 to "birth_date",
            6 to "sample_collected_at",
            7 to "emergency_flag",
            8 to "organization",
            9 to "referring_employee",
            10 to "department",
            11 to "medical_record_number",
            13 to "diagnosis",
            14 to "service_category",
            15 to "service",
            16 to "referral_number",
            17 to "completed_at",
            19 to "result_text",
            21 to "performer",
            22 to "funding_source",
            23 to "service_price",
        ),
        sequenceColumn = 0,
        minColumns = 24,
    )

    fun forKind(kind: DamumedLabReportKind): CsvColumnMapping? = when (kind) {
        DamumedLabReportKind.REFERRAL_REGISTRATION_JOURNAL -> REFERRAL_REGISTRATION_JOURNAL
        DamumedLabReportKind.COMPLETED_LAB_STUDIES_JOURNAL -> COMPLETED_LAB_STUDIES_JOURNAL
        else -> null
    }
}

interface DamumedCsvParsingService {
    suspend fun parseAndPersist(upload: DamumedReportUpload, bytes: ByteArray): DamumedReportUpload
    fun supportsKind(kind: DamumedLabReportKind): Boolean
}

@Service
class DamumedCsvParsingServiceImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val normalizedSectionRepository: DamumedNormalizedSectionRepository,
    private val normalizedDimensionRepository: DamumedNormalizedDimensionRepository,
    private val normalizedFactRepository: DamumedNormalizedFactRepository,
    private val batchRepository: DamumedNormalizedBatchRepository,
) : DamumedCsvParsingService {

    companion object {
        private val WIN1251 = Charset.forName("windows-1251")
        private const val VIRTUAL_SHEET_ID = "csv-sheet-0"
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    override fun supportsKind(kind: DamumedLabReportKind): Boolean =
        DamumedCsvColumnMappings.forKind(kind) != null

    override suspend fun parseAndPersist(upload: DamumedReportUpload, bytes: ByteArray): DamumedReportUpload {
        val mapping = DamumedCsvColumnMappings.forKind(upload.reportKind)
            ?: throw DamumedReportValidationException(
                "CSV parsing is not supported for report kind '${upload.reportKind}'."
            )

        val profile = DamumedReportSchemaCatalog.profileFor(upload.reportKind)

        val started = upload.copy(
            parseStatus = DamumedReportParseStatus.PROCESSING,
            parseStartedAt = LocalDateTime.now(),
            parseCompletedAt = null,
            parseErrorMessage = null,
            normalizationStatus = DamumedReportNormalizationStatus.PROCESSING,
            normalizationStartedAt = LocalDateTime.now(),
        ).persistUpload()

        return try {
            val csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setDelimiter(';')
                .setQuote('"')
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(false)
                .build()

            val records = withContext(Dispatchers.IO) {
                val reader = InputStreamReader(ByteArrayInputStream(bytes), WIN1251)
                CSVParser.parse(reader, csvFormat).records
            }

            cleanupExistingNormalizedData(upload.id)

            val sections = mutableListOf<DamumedNormalizedSectionEntity>()
            val dimensions = mutableListOf<DamumedNormalizedDimensionEntity>()
            val facts = mutableListOf<DamumedNormalizedFactEntity>()
            val factDimensions = mutableListOf<DamumedNormalizedFactDimensionEntity>()

            val dimensionIndex = linkedMapOf<String, String>()
            var dimSeq = 0
            var factSeq = 0
            var sectionSeq = 0
            var parsedRowCount = 0

            val metric = profile.metrics.firstOrNull()
                ?: throw DamumedReportValidationException("No metric configured for '${upload.reportKind}'.")

            val sheetId = "${upload.id}:$VIRTUAL_SHEET_ID"

            val sectionId = "${upload.id}:section:${++sectionSeq}"
            sections += DamumedNormalizedSectionEntity(
                entityId = sectionId,
                uploadId = upload.id,
                reportKind = upload.reportKind,
                sheetId = sheetId,
                sectionKey = "${upload.reportKind.name}:all-rows",
                sectionName = upload.reportKind.displayName,
                semanticRole = profile.semanticRole.name,
                anchorAxisKey = "referral_number",
                anchorAxisValue = upload.reportKind.displayName,
                rowStartIndex = 1,
                rowEndIndex = null,
                columnStartIndex = 0,
                columnEndIndex = mapping.axisColumns.keys.maxOrNull(),
            )

            for (record in records) {
                val colCount = record.size()
                if (colCount < mapping.minColumns) continue

                val seqVal = record[mapping.sequenceColumn].trim()
                if (seqVal.isBlank() || seqVal.equals("Text25", ignoreCase = true)) continue

                val rowIndex = seqVal.toIntOrNull() ?: continue
                parsedRowCount++

                val rowId = "${sheetId}:row:$rowIndex"
                val factId = "${upload.id}:fact:${++factSeq}"

                val axisValues = mutableListOf<Triple<String, DamumedReportAxisType, String>>()
                mapping.axisColumns.forEach { (colIdx, axisKey) ->
                    if (colIdx < colCount) {
                        val raw = record[colIdx].trim()
                        if (raw.isNotBlank() && raw != "Нет данных" && raw != "Нет") {
                            val axisProfile = profile.dimensions.firstOrNull { it.key == axisKey }
                            val axisType = axisProfile?.type ?: DamumedReportAxisType.IDENTIFIER
                            axisValues += Triple(axisKey, axisType, raw)
                        }
                    }
                }

                facts += DamumedNormalizedFactEntity(
                    entityId = factId,
                    uploadId = upload.id,
                    reportKind = upload.reportKind,
                    sheetId = sheetId,
                    sectionId = sectionId,
                    rowId = rowId,
                    cellId = null,
                    metricKey = metric.key,
                    metricLabel = metric.aliases.firstOrNull() ?: metric.key,
                    numericValue = 1.0,
                    valueText = axisValues.firstOrNull { it.first == "referral_number" }?.third
                        ?: seqVal,
                    formulaText = null,
                    periodText = upload.detectedPeriodText,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = 0,
                )

                axisValues.forEach { (axisKey, axisType, rawValue) ->
                    val normalizedValue = normalizeText(rawValue)
                    val dimKey = "$axisKey:$normalizedValue"
                    val dimId = dimensionIndex.getOrPut(dimKey) {
                        val newId = "${upload.id}:dim:${++dimSeq}"
                        dimensions += DamumedNormalizedDimensionEntity(
                            entityId = newId,
                            uploadId = upload.id,
                            reportKind = upload.reportKind,
                            axisKey = axisKey,
                            axisType = axisType.name,
                            rawValue = rawValue,
                            normalizedValue = normalizedValue,
                            displayValue = rawValue,
                            sourceSheetId = sheetId,
                            sourceRowIndex = rowIndex,
                            sourceColumnIndex = mapping.axisColumns.entries
                                .firstOrNull { it.value == axisKey }?.key,
                        )
                        newId
                    }
                    factDimensions += DamumedNormalizedFactDimensionEntity(
                        entityId = "$factId:$axisKey",
                        factId = factId,
                        axisKey = axisKey,
                        dimensionId = dimId,
                        rawValue = rawValue,
                        normalizedValue = normalizedValue,
                        sourceScope = "row",
                    )
                }
            }

            batchRepository.batchInsertSections(sections)
            batchRepository.batchInsertDimensions(dimensions)
            batchRepository.batchInsertFacts(facts)
            batchRepository.batchInsertFactDimensions(factDimensions)

            started.copy(
                parseStatus = DamumedReportParseStatus.PARSED,
                parseCompletedAt = LocalDateTime.now(),
                parsedSheetCount = 1,
                parsedRowCount = parsedRowCount,
                parsedCellCount = parsedRowCount * mapping.axisColumns.size,
                parsedMergedRegionCount = 0,
                detectedReportTitle = upload.reportKind.displayName,
                normalizationStatus = DamumedReportNormalizationStatus.NORMALIZED,
                normalizationCompletedAt = LocalDateTime.now(),
                normalizedSectionCount = sections.size,
                normalizedFactCount = facts.size,
                normalizedDimensionCount = dimensions.size,
            ).persistUpload()
        } catch (ex: Exception) {
            cleanupExistingNormalizedData(upload.id)
            started.copy(
                parseStatus = DamumedReportParseStatus.FAILED,
                parseCompletedAt = LocalDateTime.now(),
                parseErrorMessage = ex.message?.take(4000),
                normalizationStatus = DamumedReportNormalizationStatus.FAILED,
                normalizationCompletedAt = LocalDateTime.now(),
                normalizationErrorMessage = ex.message?.take(4000),
            ).persistUpload()
        }
    }

    private suspend fun cleanupExistingNormalizedData(uploadId: String) {
        batchRepository.deleteFactDimensionsByUploadId(uploadId)
        normalizedFactRepository.deleteAllByUploadId(uploadId)
        normalizedDimensionRepository.deleteAllByUploadId(uploadId)
        normalizedSectionRepository.deleteAllByUploadId(uploadId)
    }

    private suspend fun DamumedReportUpload.persistUpload(): DamumedReportUpload {
        val entity = this.toEntity()
        return uploadRepository.save(entity).let { saved ->
            this.copy(version = saved.version)
        }
    }

    private fun normalizeText(value: String): String =
        value.trim().lowercase().replace(WHITESPACE_REGEX, " ")
}
