package lab.dev.med.univ.feature.director.domain.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingle
import lab.dev.med.univ.feature.director.presentation.dto.DirectorAssistantAnswerDto
import lab.dev.med.univ.feature.director.presentation.dto.DirectorAssistantEvidenceSnapshotDto
import lab.dev.med.univ.feature.director.presentation.dto.DirectorAssistantTatInsightDto
import lab.dev.med.univ.feature.onec.domain.services.OnecReadonlySnapshotService
import lab.dev.med.univ.feature.onec.domain.services.OnecStockPlanningService
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerLogUploadRepository
import lab.dev.med.univ.feature.reagents.domain.services.ReconciliationSummaryService
import lab.dev.med.univ.feature.reporting.domain.services.DamumedFastOperationalMetricsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.time.OffsetDateTime

interface DirectorEvidenceAssistantService {
    suspend fun answer(question: String, model: String): DirectorAssistantAnswerDto
}

/**
 * An evidence-first director assistant.
 *
 * The Azure request contains only aggregate operational facts assembled here.
 * It never receives patient names, IINs, referral numbers, raw results,
 * inventory document contents, or the full 1C snapshot.
 */
@Service
class DirectorEvidenceAssistantServiceImpl(
    private val webClientBuilder: WebClient.Builder,
    private val objectMapper: ObjectMapper,
    private val operationalMetrics: DamumedFastOperationalMetricsService,
    private val reconciliationSummaryService: ReconciliationSummaryService,
    private val onecSnapshotService: OnecReadonlySnapshotService,
    private val onecStockPlanningService: OnecStockPlanningService,
    private val analyzerLogUploadRepository: AnalyzerLogUploadRepository,
    @Value("\${openai.base-url:}") private val baseUrl: String,
    @Value("\${openai.api-key:}") private val apiKey: String,
    @Value("\${luna.deployment:}") private val lunaDeployment: String,
    @Value("\${terra.deployment:}") private val terraDeployment: String,
) : DirectorEvidenceAssistantService {

    override suspend fun answer(question: String, model: String): DirectorAssistantAnswerDto {
        val normalizedQuestion = validateQuestion(question)
        require(baseUrl.isNotBlank() && apiKey.isNotBlank()) { "Azure AI не настроен." }

        val selectedModel = when (model.trim().lowercase()) {
            "luna" -> lunaDeployment
            "terra" -> terraDeployment
            else -> throw IllegalArgumentException("Доступны только модели luna и terra.")
        }.takeIf(String::isNotBlank) ?: throw IllegalStateException("Azure deployment не настроен.")

        val evidence = buildEvidencePacket()
        val response = webClientBuilder
            .baseUrl(baseUrl.trimEnd('/'))
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .build()
            .post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "model" to selectedModel,
                    "messages" to listOf(
                        mapOf("role" to "system", "content" to SYSTEM_PROMPT),
                        mapOf("role" to "user", "content" to "Вопрос директора:\n$normalizedQuestion\n\nДоказательства:\n${evidence.json}"),
                    ),
                    // GPT-5 deployments may use part of the budget for
                    // internal reasoning before emitting a textual answer.
                    "max_completion_tokens" to 2_048,
                ),
            )
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .awaitSingle()

        val answer = response.path("choices").path(0).path("message").path("content").asText()
            .trim()
            .takeIf(String::isNotBlank)
            ?: throw IllegalStateException("Azure AI не вернул текстовый ответ.")

        return DirectorAssistantAnswerDto(
            answer = answer,
            model = model.trim().lowercase(),
            evidenceGeneratedAt = OffsetDateTime.now().toString(),
            evidenceScope = EVIDENCE_SCOPE,
            evidenceSnapshot = evidence.snapshot,
        )
    }

    private suspend fun buildEvidencePacket(): EvidencePacket {
        val operationalKpi = operationalMetrics.kpi("month")
        val reliableTat = operationalMetrics.tat("month", limit = 0)
            .filter { it.reliableForTrend && !it.outlierSensitive }
            .take(5)
        val departments = operationalMetrics.departmentLoads("month").take(12)
        val operationalPeriodTo = operationalKpi.periodTo ?: LocalDate.now()
        val latestAnalyzerDate = analyzerLogUploadRepository.findLatestParsedApplogsEventAt()?.toLocalDate()
        val sourceCoverageTo = listOfNotNull(operationalPeriodTo, latestAnalyzerDate).maxOrNull() ?: LocalDate.now()
        val sourceCoverageFrom = sourceCoverageTo.minusDays(30)
        val quality = reconciliationSummaryService.getKpiSummary(
            dateFrom = sourceCoverageFrom,
            dateTo = sourceCoverageTo,
            analyzerId = null,
        )
        val sourceCoverage = reconciliationSummaryService.getCoverage(sourceCoverageFrom, sourceCoverageTo, analyzerId = null)
        val comparableDays = sourceCoverage
            .filter { it.comparisonStatus == "COMPARABLE" }
            .map { it.date }
            .toSet()
            .size
        val waitingForDamumedDays = sourceCoverage
            .filter { it.comparisonStatus == "NO_DAMUMED_REPORT" }
            .map { it.date }
            .toSet()
            .size
        val missingAnalyzerDays = sourceCoverage
            .filter { it.comparisonStatus == "NO_ANALYZER_REPORT" }
            .map { it.date }
            .toSet()
            .size
        val onecSummary = onecSnapshotService.getLatestSummary()
        val mappings = onecStockPlanningService.listMappings()
        val sourceStatus = when {
            waitingForDamumedDays > 0 && latestAnalyzerDate?.isAfter(operationalPeriodTo) == true ->
                "WAITING_FOR_MONTHLY_DAMUMED_JOURNAL"
            comparableDays > 0 -> "COMPARABLE_DATES_AVAILABLE"
            waitingForDamumedDays > 0 -> "WAITING_FOR_MONTHLY_DAMUMED_JOURNAL"
            missingAnalyzerDays > 0 -> "WAITING_FOR_ANALYZER_EVENTS"
            else -> "NO_COMPARABLE_SOURCE_DATES"
        }
        val sourceStatusText = when (sourceStatus) {
            "COMPARABLE_DATES_AVAILABLE" ->
                "Есть даты с обоими источниками; только они участвуют в сверке."
            "WAITING_FOR_MONTHLY_DAMUMED_JOURNAL" ->
                "Новые события анализаторов ожидают ручную ежемесячную загрузку журнала Damumed; это не расхождение."
            "WAITING_FOR_ANALYZER_EVENTS" ->
                "Журнал Damumed есть, но за этот период нет датированных событий анализатора."
            else ->
                "За выбранный период пока нет дат, где одновременно есть журнал Damumed и событийный лог анализатора."
        }
        val snapshot = DirectorAssistantEvidenceSnapshotDto(
            periodLabel = operationalKpi.periodLabel,
            periodFrom = operationalKpi.periodFrom?.toString(),
            periodTo = operationalKpi.periodTo?.toString(),
            registeredServices = operationalKpi.researchCount,
            sentResults = operationalKpi.sentResultsCount,
            pendingServices = (operationalKpi.researchCount - operationalKpi.sentResultsCount).coerceAtLeast(0),
            comparableDays = comparableDays,
            waitingForDamumedDays = waitingForDamumedDays,
            sourceStatus = sourceStatus,
            sourceStatusText = sourceStatusText,
            sourceCoverageLabel = "${sourceCoverageFrom} — ${sourceCoverageTo}",
            discrepancyCandidates = if (comparableDays > 0) quality.totalDiscrepancyCount else 0,
            confirmedMappings = mappings.count { it.mappingStatus == "CONFIRMED" },
            suggestedMappings = mappings.count { it.mappingStatus == "SUGGESTED" },
            tat = reliableTat.map {
                DirectorAssistantTatInsightDto(
                    service = it.service,
                    medianMinutes = it.medianMinutes,
                    p90Minutes = it.p90Minutes,
                    count = it.count,
                    outlierSensitive = it.outlierSensitive,
                )
            },
        )
        val qualityEvidence = if (sourceStatus == "WAITING_FOR_MONTHLY_DAMUMED_JOURNAL") {
            mapOf(
                "availability" to "WAITING_FOR_MONTHLY_DAMUMED_JOURNAL",
                "comparableDays" to comparableDays,
                "note" to "Не выводить число несопоставимых событий как проблему: новые логи ожидают ручную загрузку Damumed.",
            )
        } else {
            mapOf(
                "analyzerEvents" to quality.totalLogsCount,
                "reconciled" to quality.totalReconciledCount,
                "discrepancyCandidatesOnComparableDates" to if (comparableDays > 0) quality.totalDiscrepancyCount else 0,
                "noComparableEvidence" to quality.totalNoComparableEvidenceCount,
                "exactDatedReferralLinks" to quality.exactDatedReferralLinkCount,
            )
        }

        val packet = mapOf(
            "operationalPeriod" to mapOf(
                "label" to operationalKpi.periodLabel,
                "from" to operationalKpi.periodFrom?.toString(),
                "to" to operationalKpi.periodTo?.toString(),
                "registeredServices" to operationalKpi.researchCount,
                "sentResults" to operationalKpi.sentResultsCount,
                "pendingServices" to (operationalKpi.researchCount - operationalKpi.sentResultsCount).coerceAtLeast(0),
                "departments" to operationalKpi.departmentCount,
                "materials" to operationalKpi.materialsCount,
            ),
            "tatReliableTrend" to reliableTat.map {
                mapOf(
                    "service" to it.service,
                    "medianMinutes" to it.medianMinutes,
                    "p90Minutes" to it.p90Minutes,
                    "count" to it.count,
                    "definition" to it.definition,
                )
            },
            "departmentLoad" to departments.map {
                mapOf("department" to it.name, "count" to it.numericValue, "status" to it.status)
            },
            "sourceCoverage" to mapOf(
                "damumedIngestion" to "MANUAL_MONTHLY",
                "comparisonStatus" to sourceStatus,
                "comparisonStatusText" to sourceStatusText,
                "comparableDays" to comparableDays,
                "waitingForDamumedDays" to waitingForDamumedDays,
                "waitingForAnalyzerDays" to missingAnalyzerDays,
            ),
            "quality" to qualityEvidence,
            "onecReadonly" to mapOf(
                "snapshotAt" to onecSummary.snapshotAt?.toString(),
                "nomenclatureCount" to onecSummary.nomenclatureCount,
                "inventoryRows" to onecSummary.inventoryCount,
                "confirmedMappings" to mappings.count { it.mappingStatus == "CONFIRMED" },
                "suggestedMappings" to mappings.count { it.mappingStatus == "SUGGESTED" },
            ),
            "evidenceLimits" to listOf(
                "errors.xml is a contextual snapshot and is not a dated analyzer event.",
                "Only exact barcode-referral-date matches prove an analyzer-to-Damumed referral link.",
                "A multi-service referral is not attributed to one analyzer service without a service identifier.",
                "Damumed journals are loaded manually at month end; analyzer events before that upload wait for comparison and are not discrepancies.",
                "Suggested 1C mappings are excluded from stock coverage arithmetic.",
                "1C stock is an aggregate accounting snapshot, not a patient-level or lot-level write-off.",
            ),
        )
        return EvidencePacket(
            json = objectMapper.writeValueAsString(packet),
            snapshot = snapshot,
        )
    }

    private fun validateQuestion(question: String): String {
        val normalized = question.trim()
        require(normalized.length in 3..2_000) { "Вопрос должен содержать от 3 до 2000 символов." }
        require(!IIN_PATTERN.containsMatchIn(normalized)) {
            "Не указывайте ИИН или персональные идентификаторы в вопросе. Помощник работает только с агрегированными данными."
        }
        require(!PERSONAL_DATA_MARKER.containsMatchIn(normalized)) {
            "Не указывайте данные пациента, номера направлений или штрих-коды. Сформулируйте агрегированный операционный вопрос."
        }
        return normalized
    }

    private data class EvidencePacket(
        val json: String,
        val snapshot: DirectorAssistantEvidenceSnapshotDto,
    )

    private companion object {
        val EVIDENCE_SCOPE = listOf(
            "Агрегированные операции Damumed",
            "Робастный TAT по услугам",
            "Покрытие источников и сверка",
            "Метаданные read-only 1С",
            "Количество связей 1С ↔ ЛИС",
        )

        const val SYSTEM_PROMPT = """
            Ты — помощник директора госпитальной лаборатории.
            Отвечай только на русском языке и только на основании JSON-доказательств в сообщении пользователя.
            Не используй знания, которых нет в evidence packet, и не придумывай значения, причины или связи.
            Не ставь диагнозы, не интерпретируй результаты пациента, не давай клинических рекомендаций.
            Не утверждай, что расход или списание конкретной партии фактический, если evidence packet говорит только о норме или mapping.
            Не предлагай закупку, safety stock или обязательное действие.
            Damumed загружается вручную раз в месяц. Если sourceCoverage сообщает WAITING_FOR_MONTHLY_DAMUMED_JOURNAL
            или comparableDays=0, не называй события анализаторов проблемой, потерей, расхождением или зоной внимания.
            Скажи кратко, что сверка ожидает журнал Damumed за те же даты.
            В этом режиме не цитируй число noComparableEvidence, analyzerEvents, reconciled или нулевые связи:
            это техническое следствие разной периодичности источников, а не управленческий показатель.
            Не сопоставляй итог последней полной выгрузки с выбранным операционным периодом.
            TAT разрешено комментировать только по массиву tatReliableTrend. Не употребляй «максимальный TAT»,
            не делай вывод по единичному случаю и опирайся на медиану, p90 и число наблюдений.
            Если tatReliableTrend пуст, прямо скажи, что устойчивого вывода по TAT недостаточно.
            Начинай с короткого блока «Кратко» (не более трёх пунктов), затем при необходимости используй
            «Динамика», «Сверка источников», «TAT». Не превращай ответ в длинный перечень всех показателей.
            Если данных недостаточно, прямо скажи это и укажи, какого источника или поля не хватает.
            В конце каждого ответа добавь раздел «Основание» с 2–5 краткими пунктами из evidence packet
            и раздел «Ограничения» с релевантными границами доказательств.
        """

        val IIN_PATTERN = Regex("""(?<!\d)\d{12}(?!\d)""")
        val PERSONAL_DATA_MARKER = Regex(
            """\b(иин|пациент|фио|истори[яи]|номер\s+направлен|штрих[\s-]?код|barcode)\b""",
            RegexOption.IGNORE_CASE,
        )
    }
}
