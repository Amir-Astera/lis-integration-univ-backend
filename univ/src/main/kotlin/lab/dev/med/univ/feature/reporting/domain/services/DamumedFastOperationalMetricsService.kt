package lab.dev.med.univ.feature.reporting.domain.services

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalDailyStat
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalStatusItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalTatItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReferralRegistrationSummary
import lab.dev.med.univ.feature.reporting.domain.models.ReferralDailyRegistrationStat
import lab.dev.med.univ.feature.reporting.domain.models.ReferralDepartmentStat
import lab.dev.med.univ.feature.reporting.domain.models.ReferralFundingSourceStat
import lab.dev.med.univ.feature.reporting.domain.models.ReferralRegistrationPeriodSummary
import lab.dev.med.univ.feature.reporting.domain.models.ReferralServiceStat
import lab.dev.med.univ.feature.reporting.domain.models.ReferralStatusStat
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.roundToInt

data class DamumedFastPeriodKpi(
    val period: String,
    val referenceDate: LocalDate?,
    val periodFrom: LocalDate?,
    val periodTo: LocalDate?,
    val periodLabel: String,
    val researchCount: Int,
    val patientCount: Int,
    val departmentCount: Int,
    val sentResultsCount: Int,
    val materialsCount: Int,
    val serviceCostTotal: Double,
)

data class DamumedFastReferralRow(
    val referralNumber: String,
    val service: String,
    val status: String,
    val patientKey: String?,
    val department: String?,
    val material: String?,
    val emergency: String?,
    val referralAt: LocalDateTime?,
    val receivedAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val serviceCost: Double?,
    val fundingSource: String?,
)

interface DamumedFastOperationalMetricsService {
    suspend fun kpi(period: String): DamumedFastPeriodKpi
    suspend fun dailyStats(period: String = "month"): List<DamumedOperationalDailyStat>
    suspend fun tat(period: String, limit: Int? = 20): List<DamumedOperationalTatItem>
    suspend fun departmentLoads(period: String = "month"): List<DamumedOperationalStatusItem>
    suspend fun referralRegistrationSummary(): DamumedReferralRegistrationSummary
}

/**
 * Bounded operational query path for the dashboard.
 *
 * It pivots only the axes required by operations directly in PostgreSQL and
 * returns one lightweight row per normalized referral fact. It never loads all
 * fact dimensions into the JVM, avoiding the multi-million-row OOM/504 path.
 */
@Service
class DamumedFastOperationalMetricsServiceImpl(
    private val databaseClient: DatabaseClient,
) : DamumedFastOperationalMetricsService {
    private val cacheMutex = Mutex()
    @Volatile
    private var cache: CachedRows? = null

    override suspend fun kpi(period: String): DamumedFastPeriodKpi {
        val periodRows = loadPeriodRows(period)
        val rows = periodRows.rows
        return DamumedFastPeriodKpi(
            period = periodRows.period,
            referenceDate = periodRows.referenceDate,
            periodFrom = periodRows.from,
            periodTo = periodRows.to,
            periodLabel = periodRows.label,
            researchCount = rows.map { "${it.referralNumber}::${it.service}" }.toSet().size,
            patientCount = rows.mapNotNull { it.patientKey }.toSet().size,
            departmentCount = rows.mapNotNull { it.department }.toSet().size,
            sentResultsCount = rows.count { it.status.equals("Результат отправлен", ignoreCase = true) },
            materialsCount = rows.count { !it.material.isNullOrBlank() },
            serviceCostTotal = rows.sumOf { it.serviceCost ?: 0.0 },
        )
    }

    override suspend fun dailyStats(period: String): List<DamumedOperationalDailyStat> =
        loadPeriodRows(period)
            .rows
            .mapNotNull { row -> businessDate(row)?.let { it to row } }
            .groupBy { it.first }
            .map { (date, grouped) -> DamumedOperationalDailyStat(date = date.toString(), count = grouped.size) }
            .sortedBy { it.date }

    override suspend fun tat(period: String, limit: Int?): List<DamumedOperationalTatItem> {
        val items = loadPeriodRows(period)
            .rows
            .mapNotNull { row ->
                if (!row.status.equals("Результат отправлен", ignoreCase = true)) return@mapNotNull null
                val started = row.receivedAt ?: row.referralAt ?: return@mapNotNull null
                val finished = row.completedAt ?: return@mapNotNull null
                val minutes = Duration.between(started, finished).toMinutes()
                if (minutes < 0) return@mapNotNull null
                row.service to minutes
            }
            .groupBy({ it.first }, { it.second })
            .map { (service, durations) ->
                val sorted = durations.sorted()
                val average = sorted.average().roundToInt()
                DamumedOperationalTatItem(
                    service = service,
                    averageMinutes = average,
                    averageDurationText = formatDuration(average),
                    count = sorted.size,
                    medianMinutes = percentile(sorted, 0.50),
                    p90Minutes = percentile(sorted, 0.90),
                    definition = "registration_or_receipt_to_result",
                )
            }
            .sortedWith(
                compareBy<DamumedOperationalTatItem> { !it.reliableForTrend }
                    .thenBy { it.outlierSensitive }
                    .thenByDescending { it.p90Minutes }
                    .thenByDescending { it.count },
            )
        return if (limit == null || limit <= 0) items else items.take(limit)
    }

    override suspend fun departmentLoads(period: String): List<DamumedOperationalStatusItem> =
        loadPeriodRows(period)
            .rows
            .groupBy { it.department?.takeIf(String::isNotBlank) ?: "Не указано" }
            .map { (department, grouped) ->
                DamumedOperationalStatusItem(
                    name = department,
                    secondaryText = "Зарегистрировано услуг: ${grouped.size}",
                    status = "Активно",
                    numericValue = grouped.size.toDouble(),
                )
            }
            .sortedByDescending { it.numericValue }

    override suspend fun referralRegistrationSummary(): DamumedReferralRegistrationSummary {
        val rows = loadRows()
        val completed = rows.filter { it.status.equals("Результат отправлен", ignoreCase = true) }
        val completionMinutes = completed.mapNotNull { row ->
            val started = row.receivedAt ?: row.referralAt ?: return@mapNotNull null
            val finished = row.completedAt ?: return@mapNotNull null
            Duration.between(started, finished).toMinutes().takeIf { it >= 0 }
        }

        return DamumedReferralRegistrationSummary(
            generatedAt = Instant.now(),
            periodLabel = "Последняя нормализованная выгрузка",
            sourceUploadId = latestUploadId().orEmpty(),
            summary = ReferralRegistrationPeriodSummary(
                label = "За период выгрузки",
                researchCount = rows.map { "${it.referralNumber}::${it.service}" }.toSet().size,
                patientCount = rows.mapNotNull { it.patientKey }.toSet().size,
                departmentCount = rows.mapNotNull { it.department }.toSet().size,
                sentResultsCount = completed.size,
                pendingCount = (rows.size - completed.size).coerceAtLeast(0),
                materialsCount = rows.count { !it.material.isNullOrBlank() },
                avgCompletionMinutes = completionMinutes.takeIf { it.isNotEmpty() }?.average(),
                emergencyCount = rows.count { it.emergency?.equals("да", ignoreCase = true) == true },
            ),
            departmentStats = rows
                .groupBy { it.department?.takeIf(String::isNotBlank) ?: "Не указано" }
                .map { (department, items) ->
                    val done = items.count { it.status.equals("Результат отправлен", ignoreCase = true) }
                    val inProgress = items.count {
                        it.status.contains("в работе", ignoreCase = true) ||
                            it.status.contains("выполняется", ignoreCase = true)
                    }
                    ReferralDepartmentStat(
                        department = department,
                        total = items.size,
                        completed = done,
                        pending = (items.size - done - inProgress).coerceAtLeast(0),
                        inProgress = inProgress,
                    )
                }
                .sortedByDescending { it.total }
                .take(20),
            statusStats = rows
                .groupBy { it.status.ifBlank { "Не указан" } }
                .map { (status, items) -> ReferralStatusStat(status = status, count = items.size) }
                .sortedByDescending { it.count },
            serviceStats = rows
                .groupBy { it.service }
                .map { (service, items) ->
                    val done = items.count { it.status.equals("Результат отправлен", ignoreCase = true) }
                    ReferralServiceStat(
                        service = service,
                        total = items.size,
                        completed = done,
                        pending = (items.size - done).coerceAtLeast(0),
                    )
                }
                .sortedByDescending { it.total }
                .take(30),
            fundingSourceStats = rows
                .groupBy { it.fundingSource?.takeIf(String::isNotBlank) ?: "Не указано" }
                .map { (fundingSource, items) -> ReferralFundingSourceStat(fundingSource = fundingSource, count = items.size) }
                .sortedByDescending { it.count },
            dailyRegistrationStats = rows
                .mapNotNull { row -> businessDate(row)?.let { it to row } }
                .groupBy { it.first }
                .map { (date, items) ->
                    ReferralDailyRegistrationStat(
                        date = date.toString(),
                        registered = items.size,
                        completed = items.count { it.second.status.equals("Результат отправлен", ignoreCase = true) },
                    )
                }
                .sortedBy { it.date },
        )
    }

    private suspend fun loadPeriodRows(period: String): PeriodRows {
        val all = loadRows()
        val normalizedPeriod = normalizePeriod(period)
        val reference = all.mapNotNull(::businessDate).maxOrNull()
            ?: return PeriodRows(
                period = normalizedPeriod,
                referenceDate = null,
                from = null,
                to = null,
                label = "нет нормализованных дат",
                rows = emptyList(),
            )
        val (from, to) = when (normalizedPeriod) {
            "day" -> reference to reference
            "week" -> {
                val monday = reference.minusDays((reference.dayOfWeek.value - 1).toLong())
                monday to reference
            }
            else -> reference.withDayOfMonth(1) to reference
        }
        val rows = all.filter { row ->
            val date = businessDate(row) ?: return@filter false
            date in from..to
        }
        return PeriodRows(
            period = normalizedPeriod,
            referenceDate = reference,
            from = from,
            to = to,
            label = formatPeriodLabel(from, to),
            rows = rows,
        )
    }

    private suspend fun loadRows(): List<DamumedFastReferralRow> {
        val cached = cache
        if (cached != null && Duration.between(cached.loadedAt, LocalDateTime.now()) < Duration.ofMinutes(2)) {
            return cached.rows
        }
        return cacheMutex.withLock {
            val inside = cache
            if (inside != null && Duration.between(inside.loadedAt, LocalDateTime.now()) < Duration.ofMinutes(2)) {
                return@withLock inside.rows
            }
            val query = """
                WITH latest_upload AS (
                    SELECT id
                    FROM damumed_report_uploads
                    WHERE report_kind = 'REFERRAL_REGISTRATION_JOURNAL'
                      AND normalization_status = 'NORMALIZED'
                    ORDER BY uploaded_at DESC
                    LIMIT 1
                )
                SELECT
                    MAX(CASE WHEN d.axis_key = 'referral_number' THEN d.raw_value END) AS referral_number,
                    MAX(CASE WHEN d.axis_key = 'service' THEN d.raw_value END) AS service,
                    MAX(CASE WHEN d.axis_key = 'referral_status' THEN d.raw_value END) AS referral_status,
                    MAX(CASE WHEN d.axis_key = 'patient_iin' THEN d.raw_value END) AS patient_iin,
                    MAX(CASE WHEN d.axis_key = 'patient_name' THEN d.raw_value END) AS patient_name,
                    MAX(CASE WHEN d.axis_key = 'patient_department' THEN d.raw_value END) AS patient_department,
                    MAX(CASE WHEN d.axis_key = 'material' THEN d.raw_value END) AS material,
                    MAX(CASE WHEN d.axis_key = 'emergency_flag' THEN d.raw_value END) AS emergency_flag,
                    MAX(CASE WHEN d.axis_key IN ('referral_date', 'registration_date') THEN d.raw_value END) AS referral_at,
                    MAX(CASE WHEN d.axis_key = 'received_at' THEN d.raw_value END) AS received_at,
                    MAX(CASE WHEN d.axis_key = 'completed_at' THEN d.raw_value END) AS completed_at,
                    MAX(CASE WHEN d.axis_key = 'service_cost' THEN d.raw_value END) AS service_cost,
                    MAX(CASE WHEN d.axis_key = 'funding_source' THEN d.raw_value END) AS funding_source
                FROM damumed_report_normalized_facts f
                INNER JOIN latest_upload u ON u.id = f.upload_id
                LEFT JOIN damumed_report_normalized_fact_dimensions d
                    ON d.fact_id = f.id
                   AND d.axis_key IN (
                       'referral_number', 'service', 'referral_status', 'patient_iin', 'patient_name',
                       'patient_department', 'material', 'emergency_flag', 'referral_date',
                       'registration_date', 'received_at', 'completed_at', 'service_cost', 'funding_source'
                   )
                GROUP BY f.id
            """.trimIndent()
            val rows = databaseClient.sql(query)
                .map { row, _ ->
                    val referral = row.get("referral_number", String::class.java)?.trim().orEmpty()
                    val service = row.get("service", String::class.java)?.trim().orEmpty()
                    DamumedFastReferralRow(
                        referralNumber = referral,
                        service = service,
                        status = row.get("referral_status", String::class.java)?.trim().orEmpty(),
                        patientKey = row.get("patient_iin", String::class.java)?.trim()?.takeIf(String::isNotBlank)
                            ?: row.get("patient_name", String::class.java)?.trim()?.takeIf(String::isNotBlank),
                        department = row.get("patient_department", String::class.java)?.trim()?.takeIf(String::isNotBlank),
                        material = row.get("material", String::class.java)?.trim()?.takeIf(String::isNotBlank),
                        emergency = row.get("emergency_flag", String::class.java)?.trim(),
                        referralAt = parseDateTime(row.get("referral_at", String::class.java)),
                        receivedAt = parseDateTime(row.get("received_at", String::class.java)),
                        completedAt = parseDateTime(row.get("completed_at", String::class.java)),
                        serviceCost = row.get("service_cost", String::class.java)?.let(::parseNumber),
                        fundingSource = row.get("funding_source", String::class.java)?.trim()?.takeIf(String::isNotBlank),
                    )
                }
                .all()
                .collectList()
                .awaitSingle()
                .filter { it.referralNumber.isNotBlank() && it.service.isNotBlank() }
            cache = CachedRows(rows = rows, loadedAt = LocalDateTime.now())
            rows
        }
    }

    private fun businessDate(row: DamumedFastReferralRow): LocalDate? =
        row.completedAt?.toLocalDate() ?: row.receivedAt?.toLocalDate() ?: row.referralAt?.toLocalDate()

    private suspend fun latestUploadId(): String? =
        databaseClient.sql(
            """
            SELECT id
            FROM damumed_report_uploads
            WHERE report_kind = 'REFERRAL_REGISTRATION_JOURNAL'
              AND normalization_status = 'NORMALIZED'
            ORDER BY uploaded_at DESC
            LIMIT 1
            """.trimIndent(),
        )
            .map { row, _ -> row.get("id", String::class.java) }
            .one()
            .awaitSingleOrNull()

    private fun parseDateTime(raw: String?): LocalDateTime? {
        val value = raw?.trim()?.takeIf(String::isNotBlank) ?: return null
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        )
        return formatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDateTime.parse(value, formatter) }.getOrNull()
        } ?: runCatching {
            LocalDate.parse(value.take(10), DateTimeFormatter.ofPattern("dd.MM.yyyy")).atStartOfDay()
        }.getOrNull()
    }

    private fun parseNumber(raw: String): Double? =
        raw.replace(" ", "").replace(",", ".").toDoubleOrNull()

    private fun percentile(sorted: List<Long>, p: Double): Int {
        if (sorted.isEmpty()) return 0
        val index = ceil(p * sorted.size).toInt().coerceIn(1, sorted.size) - 1
        return sorted[index].toInt()
    }

    private fun formatDuration(minutes: Int): String = when {
        minutes >= 24 * 60 -> "${(minutes / (24.0 * 60)).roundToInt()}д"
        minutes >= 60 -> "${(minutes / 60.0).roundToInt()}ч"
        else -> "${minutes}м"
    }

    private fun normalizePeriod(period: String): String =
        period.trim().lowercase().takeIf { it in setOf("day", "week", "month") } ?: "month"

    private fun formatPeriodLabel(from: LocalDate, to: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return if (from == to) from.format(formatter) else "${from.format(formatter)} — ${to.format(formatter)}"
    }

    private data class PeriodRows(
        val period: String,
        val referenceDate: LocalDate?,
        val from: LocalDate?,
        val to: LocalDate?,
        val label: String,
        val rows: List<DamumedFastReferralRow>,
    )

    private data class CachedRows(
        val rows: List<DamumedFastReferralRow>,
        val loadedAt: LocalDateTime,
    )
}
