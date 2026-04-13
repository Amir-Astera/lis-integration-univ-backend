package lab.dev.med.univ.feature.reporting.data.repository

import kotlinx.coroutines.reactive.awaitFirstOrNull
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedSectionEntity
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

/**
 * Batch operations for normalized report data to optimize large dataset processing.
 * Uses R2DBC DatabaseClient for efficient bulk inserts.
 */
@Component
class DamumedNormalizedBatchRepository(
    private val databaseClient: DatabaseClient,
) {

    companion object {
        const val BATCH_SIZE = 1000
    }

    /**
     * Batch insert sections
     */
    suspend fun batchInsertSections(sections: List<DamumedNormalizedSectionEntity>): Int {
        if (sections.isEmpty()) return 0
        
        var totalInserted = 0
        sections.chunked(BATCH_SIZE).forEach { batch ->
            val sql = buildString {
                append("INSERT INTO damumed_report_normalized_sections (")
                append("id, upload_id, report_kind, sheet_id, section_key, section_name,")
                append("semantic_role, anchor_axis_key, anchor_axis_value,")
                append("row_start_index, row_end_index, column_start_index, column_end_index")
                append(") VALUES ")
                
                batch.forEachIndexed { index, section ->
                    if (index > 0) append(",")
                    append("(")
                    append("'${escapeSql(section.entityId)}', ")
                    append("'${escapeSql(section.uploadId)}', ")
                    append("'${section.reportKind.name}', ")
                    append("'${escapeSql(section.sheetId)}', ")
                    append("'${escapeSql(section.sectionKey)}', ")
                    append("'${escapeSql(section.sectionName)}', ")
                    append("'${escapeSql(section.semanticRole)}', ")
                    append("'${escapeSql(section.anchorAxisKey)}', ")
                    append("'${escapeSql(section.anchorAxisValue)}', ")
                    append("${section.rowStartIndex ?: "NULL"}, ")
                    append("${section.rowEndIndex ?: "NULL"}, ")
                    append("${section.columnStartIndex ?: "NULL"}, ")
                    append("${section.columnEndIndex ?: "NULL"}")
                    append(")")
                }
                append(" ON CONFLICT (id) DO NOTHING")
            }
            
            val rowsUpdated = databaseClient.sql(sql)
                .fetch()
                .rowsUpdated()
                .awaitFirstOrNull() ?: 0
            totalInserted += rowsUpdated.toInt()
        }
        return totalInserted
    }

    /**
     * Batch insert facts
     */
    suspend fun batchInsertFacts(facts: List<DamumedNormalizedFactEntity>): Int {
        if (facts.isEmpty()) return 0
        
        var totalInserted = 0
        facts.chunked(BATCH_SIZE).forEach { batch ->
            val sql = buildString {
                append("INSERT INTO damumed_report_normalized_facts (")
                append("id, upload_id, report_kind, sheet_id, section_id,")
                append("row_id, cell_id, metric_key, metric_label,")
                append("numeric_value, value_text, formula_text, period_text,")
                append("source_row_index, source_column_index")
                append(") VALUES ")
                
                batch.forEachIndexed { index, fact ->
                    if (index > 0) append(",")
                    append("(")
                    append("'${escapeSql(fact.entityId)}', ")
                    append("'${escapeSql(fact.uploadId)}', ")
                    append("'${fact.reportKind.name}', ")
                    append("'${escapeSql(fact.sheetId)}', ")
                    append("'${escapeSql(fact.sectionId)}', ")
                    append("'${escapeSql(fact.rowId)}', ")
                    append("'${escapeSql(fact.cellId)}', ")
                    append("'${escapeSql(fact.metricKey)}', ")
                    append("'${escapeSql(fact.metricLabel)}', ")
                    append("${fact.numericValue ?: "NULL"}, ")
                    append("'${escapeSql(fact.valueText)}', ")
                    append("'${escapeSql(fact.formulaText)}', ")
                    append("'${escapeSql(fact.periodText)}', ")
                    append("${fact.sourceRowIndex}, ")
                    append("${fact.sourceColumnIndex}")
                    append(")")
                }
                append(" ON CONFLICT (id) DO NOTHING")
            }
            
            val rowsUpdated = databaseClient.sql(sql)
                .fetch()
                .rowsUpdated()
                .awaitFirstOrNull() ?: 0
            totalInserted += rowsUpdated.toInt()
        }
        return totalInserted
    }

    /**
     * Batch insert dimensions
     */
    suspend fun batchInsertDimensions(dimensions: List<DamumedNormalizedDimensionEntity>): Int {
        if (dimensions.isEmpty()) return 0
        
        var totalInserted = 0
        dimensions.chunked(BATCH_SIZE).forEach { batch ->
            val sql = buildString {
                append("INSERT INTO damumed_report_normalized_dimensions (")
                append("id, upload_id, report_kind, axis_key, axis_type,")
                append("raw_value, normalized_value, display_value,")
                append("source_sheet_id, source_row_index, source_column_index")
                append(") VALUES ")
                
                batch.forEachIndexed { index, dim ->
                    if (index > 0) append(",")
                    append("(")
                    append("'${escapeSql(dim.entityId)}', ")
                    append("'${escapeSql(dim.uploadId)}', ")
                    append("'${dim.reportKind.name}', ")
                    append("'${escapeSql(dim.axisKey)}', ")
                    append("'${escapeSql(dim.axisType)}', ")
                    append("'${escapeSql(dim.rawValue)}', ")
                    append("'${escapeSql(dim.normalizedValue)}', ")
                    append("'${escapeSql(dim.displayValue)}', ")
                    append("'${escapeSql(dim.sourceSheetId)}', ")
                    append("${dim.sourceRowIndex ?: "NULL"}, ")
                    append("${dim.sourceColumnIndex ?: "NULL"}")
                    append(")")
                }
                append(" ON CONFLICT (id) DO NOTHING")
            }
            
            val rowsUpdated = databaseClient.sql(sql)
                .fetch()
                .rowsUpdated()
                .awaitFirstOrNull() ?: 0
            totalInserted += rowsUpdated.toInt()
        }
        return totalInserted
    }

    /**
     * Batch insert fact-dimension links
     */
    suspend fun batchInsertFactDimensions(factDimensions: List<DamumedNormalizedFactDimensionEntity>): Int {
        if (factDimensions.isEmpty()) return 0
        
        var totalInserted = 0
        factDimensions.chunked(BATCH_SIZE).forEach { batch ->
            val sql = buildString {
                append("INSERT INTO damumed_report_normalized_fact_dimensions (")
                append("id, fact_id, axis_key, dimension_id, raw_value, normalized_value, source_scope")
                append(") VALUES ")
                
                batch.forEachIndexed { index, fd ->
                    if (index > 0) append(",")
                    append("(")
                    append("'${escapeSql(fd.entityId)}', ")
                    append("'${escapeSql(fd.factId)}', ")
                    append("'${escapeSql(fd.axisKey)}', ")
                    append("'${escapeSql(fd.dimensionId)}', ")
                    append("'${escapeSql(fd.rawValue)}', ")
                    append("'${escapeSql(fd.normalizedValue)}', ")
                    append("'${escapeSql(fd.sourceScope)}'")
                    append(")")
                }
                append(" ON CONFLICT (id) DO NOTHING")
            }
            
            val rowsUpdated = databaseClient.sql(sql)
                .fetch()
                .rowsUpdated()
                .awaitFirstOrNull() ?: 0
            totalInserted += rowsUpdated.toInt()
        }
        return totalInserted
    }

    /**
     * Delete all fact-dimension links for an upload in a single query.
     * fact_id format: "<uploadId>:fact:<seq>", so we match by prefix.
     */
    suspend fun deleteFactDimensionsByUploadId(uploadId: String): Int {
        val safeUploadId = uploadId.replace("'", "''")
        val sql = "DELETE FROM damumed_report_normalized_fact_dimensions WHERE fact_id LIKE '$safeUploadId:fact:%'"
        return databaseClient.sql(sql)
            .fetch()
            .rowsUpdated()
            .awaitFirstOrNull()?.toInt() ?: 0
    }

    private fun escapeSql(value: String?): String {
        return value?.replace("'", "''")?.replace("\\", "\\\\") ?: ""
    }
}
