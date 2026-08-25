package lab.dev.med.univ.feature.reagents.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.ServiceCatalogRepository
import lab.dev.med.univ.feature.reagents.domain.models.ServiceCatalogEntry
import lab.dev.med.univ.feature.reagents.domain.models.ServiceMatchConfidence
import lab.dev.med.univ.feature.reagents.domain.models.ServiceMatchResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Multi-level service name matching between LIS report names and analyzer log names.
 *
 * Match levels (in priority order):
 *  1. BY_SERVICE_ID  — analyzer service_id (from HL7) matches catalog analyzerServiceIds list
 *  2. BY_EXACT_NAME  — input == catalogEntry.canonicalName (case-insensitive)
 *  3. BY_LIS_ALIAS   — any alias in catalogEntry.lisAliases is contained in input,
 *                      or input is contained in the alias (bidirectional substring)
 *  4. BY_FUZZY       — Jaro-Winkler similarity ≥ 0.88 between input and canonicalName
 *
 * The catalog is loaded once and cached in-memory. Call [reload] after catalog edits.
 */
interface ServiceMatchingService {
    /** Match by free-text service name (from LIS report or analyzer log). */
    fun matchByName(input: String): ServiceMatchResult

    /** Match by numeric service_id from HL7 analyzer payload. */
    fun matchByServiceId(serviceId: Int): ServiceMatchResult

    /** Match by name — returns list of all entries, best match first, filtered by [minConfidence]. */
    fun matchByNameAll(input: String, minConfidence: ServiceMatchConfidence = ServiceMatchConfidence.BY_FUZZY): List<ServiceMatchResult>

    /** Return all active catalog entries (for admin UI / seeding). */
    fun allEntries(): List<ServiceCatalogEntry>

    /** Force reload from DB (e.g. after admin saves a new entry). */
    suspend fun reload()
}

@Service
class ServiceMatchingServiceImpl(
    private val catalogRepository: ServiceCatalogRepository,
    private val objectMapper: ObjectMapper,
) : ServiceMatchingService {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        try {
            runBlocking { reload() }
            log.info("Service catalog loaded: {} active entries", catalog.size)
        } catch (ex: Exception) {
            log.warn("Failed to load service catalog on startup — matching will return NO_MATCH until reload(): {}", ex.message)
        }
    }

    // Volatile so reads from other coroutines/threads see latest reload
    @Volatile
    private var catalog: List<ServiceCatalogEntry> = emptyList()

    // Pre-built index: serviceId → entry (for O(1) lookup)
    @Volatile
    private var serviceIdIndex: Map<Int, ServiceCatalogEntry> = emptyMap()

    // Pre-built index: lowercased alias → entry (for O(1) alias lookup)
    @Volatile
    private var aliasIndex: Map<String, ServiceCatalogEntry> = emptyMap()

    override suspend fun reload() {
        val entries = catalogRepository.findAllByIsActiveTrueOrderByCanonicalNameAsc()
            .toList()
            .map { it.toModel(objectMapper) }

        catalog = entries

        serviceIdIndex = buildMap {
            entries.forEach { entry ->
                entry.analyzerServiceIds.forEach { id -> putIfAbsent(id, entry) }
            }
        }

        aliasIndex = buildMap {
            entries.forEach { entry ->
                entry.lisAliases.forEach { alias ->
                    val key = alias.trim().lowercase()
                    if (key.isNotEmpty()) putIfAbsent(key, entry)
                }
                // Also index canonical name itself
                putIfAbsent(entry.canonicalName.trim().lowercase(), entry)
            }
        }
    }

    override fun allEntries(): List<ServiceCatalogEntry> = catalog

    override fun matchByServiceId(serviceId: Int): ServiceMatchResult {
        val entry = serviceIdIndex[serviceId]
        return if (entry != null) {
            ServiceMatchResult(entry = entry, confidence = ServiceMatchConfidence.BY_SERVICE_ID)
        } else {
            ServiceMatchResult(entry = null, confidence = ServiceMatchConfidence.NO_MATCH)
        }
    }

    override fun matchByName(input: String): ServiceMatchResult =
        matchByNameAll(input, ServiceMatchConfidence.BY_FUZZY).firstOrNull()
            ?: ServiceMatchResult(entry = null, confidence = ServiceMatchConfidence.NO_MATCH)

    override fun matchByNameAll(
        input: String,
        minConfidence: ServiceMatchConfidence,
    ): List<ServiceMatchResult> {
        if (input.isBlank()) return emptyList()

        val normalized = input.trim().lowercase()
        val results = mutableListOf<ServiceMatchResult>()

        // Level 2: exact canonical name
        val exactEntry = aliasIndex[normalized]
        if (exactEntry != null) {
            val isExact = exactEntry.canonicalName.trim().lowercase() == normalized
            val conf = if (isExact) ServiceMatchConfidence.BY_EXACT_NAME else ServiceMatchConfidence.BY_LIS_ALIAS
            results += ServiceMatchResult(entry = exactEntry, confidence = conf, matchedAlias = normalized)
        }

        // Level 3: bidirectional alias substring (entries not already found)
        val alreadyMatched = results.mapNotNull { it.entry?.id }.toHashSet()
        for (entry in catalog) {
            if (entry.id in alreadyMatched) continue
            val matchedAlias = findAliasMatch(normalized, entry)
            if (matchedAlias != null) {
                results += ServiceMatchResult(
                    entry        = entry,
                    confidence   = ServiceMatchConfidence.BY_LIS_ALIAS,
                    matchedAlias = matchedAlias,
                )
                alreadyMatched += entry.id
            }
        }

        // Level 4: Jaro-Winkler fuzzy (only if we need that confidence level)
        if (minConfidence <= ServiceMatchConfidence.BY_FUZZY) {
            for (entry in catalog) {
                if (entry.id in alreadyMatched) continue
                val similarity = jaroWinkler(normalized, entry.canonicalName.trim().lowercase())
                if (similarity >= FUZZY_THRESHOLD) {
                    results += ServiceMatchResult(
                        entry           = entry,
                        confidence      = ServiceMatchConfidence.BY_FUZZY,
                        similarityScore = similarity,
                    )
                    alreadyMatched += entry.id
                }
            }
        }

        // Sort by confidence (STRONGEST first = LOWEST ordinal), then by similarity desc.
        // Filter keeps results at least as strong as the caller-requested minConfidence.
        return results
            .filter { it.confidence.isAtLeastAsStrongAs(minConfidence) }
            .sortedWith(compareBy<ServiceMatchResult> { it.confidence.ordinal }
                .thenByDescending { it.similarityScore ?: 1.0 })
    }

    // ─── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Returns the first alias (or canonical name) from [entry] that has a substring
     * relationship with [normalizedInput]:
     *  - alias is contained in input, OR
     *  - input is contained in alias
     * Only considers aliases with length ≥ MIN_ALIAS_LENGTH to avoid spurious matches.
     */
    private fun findAliasMatch(normalizedInput: String, entry: ServiceCatalogEntry): String? {
        val candidates = entry.lisAliases.map { it.trim().lowercase() } +
                listOf(entry.canonicalName.trim().lowercase())

        for (alias in candidates) {
            if (alias.length < MIN_ALIAS_LENGTH) continue
            if (normalizedInput.contains(alias) || alias.contains(normalizedInput)) {
                return alias
            }
        }
        return null
    }

    /**
     * Jaro-Winkler similarity. Range [0, 1]. 1 = identical.
     * Implementation follows the standard algorithm (no external libraries needed).
     */
    private fun jaroWinkler(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val matchDistance = maxOf(s1.length, s2.length) / 2 - 1
        if (matchDistance < 0) return 0.0

        val s1Matches = BooleanArray(s1.length)
        val s2Matches = BooleanArray(s2.length)
        var matches = 0
        var transpositions = 0

        for (i in s1.indices) {
            val start = maxOf(0, i - matchDistance)
            val end   = minOf(i + matchDistance + 1, s2.length)
            for (j in start until end) {
                if (s2Matches[j] || s1[i] != s2[j]) continue
                s1Matches[i] = true
                s2Matches[j] = true
                matches++
                break
            }
        }

        if (matches == 0) return 0.0

        var k = 0
        for (i in s1.indices) {
            if (!s1Matches[i]) continue
            while (!s2Matches[k]) k++
            if (s1[i] != s2[k]) transpositions++
            k++
        }

        val jaro = (matches.toDouble() / s1.length +
                matches.toDouble() / s2.length +
                (matches - transpositions / 2.0) / matches) / 3.0

        // Winkler prefix bonus (up to first 4 common chars)
        var prefix = 0
        for (i in 0 until minOf(4, s1.length, s2.length)) {
            if (s1[i] == s2[i]) prefix++ else break
        }

        return jaro + prefix * WINKLER_SCALING * (1 - jaro)
    }

    companion object {
        private const val FUZZY_THRESHOLD   = 0.88
        private const val WINKLER_SCALING   = 0.1
        private const val MIN_ALIAS_LENGTH  = 4
    }
}
