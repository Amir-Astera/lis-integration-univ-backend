package lab.dev.med.univ.feature.reagents.presentation.rest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import lab.dev.med.univ.feature.reagents.domain.models.ServiceNormSource
import lab.dev.med.univ.feature.reagents.domain.usecases.CreateServiceReagentNormUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.CreateServiceToAnalyzerMappingUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.DeleteServiceReagentNormUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.DeleteServiceToAnalyzerMappingUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.FindMatchingAnalyzerForServiceUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetAllServiceReagentNormsUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetAllServiceToAnalyzerMappingsUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetServiceReagentNormByIdUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetServiceReagentNormsByCategoryUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetServiceReagentNormsByServiceUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetServiceToAnalyzerMappingByIdUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.UpdateServiceReagentNormUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.UpdateServiceToAnalyzerMappingUseCase
import lab.dev.med.univ.feature.reagents.presentation.dto.CreateServiceReagentNormRequestDto
import lab.dev.med.univ.feature.reagents.presentation.dto.CreateServiceToAnalyzerMappingRequestDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ServiceReagentNormDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ServiceToAnalyzerMappingDto
import lab.dev.med.univ.feature.reagents.presentation.dto.toDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

/**
 * REST controller for service reagent consumption norms.
 * Manages the mapping of laboratory services to reagents/consumables.
 */
@RestController
@RequestMapping("/api/reagents/service-norms")
class ServiceReagentNormController(
    private val getAllNorms: GetAllServiceReagentNormsUseCase,
    private val getNormById: GetServiceReagentNormByIdUseCase,
    private val getNormsByService: GetServiceReagentNormsByServiceUseCase,
    private val getNormsByCategory: GetServiceReagentNormsByCategoryUseCase,
    private val createNorm: CreateServiceReagentNormUseCase,
    private val updateNorm: UpdateServiceReagentNormUseCase,
    private val deleteNorm: DeleteServiceReagentNormUseCase,
) {

    @GetMapping
    fun getAll(): Flow<ServiceReagentNormDto> = flow {
        getAllNorms().forEach { emit(it.toDto()) }
    }

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: String): ResponseEntity<ServiceReagentNormDto> {
        val norm = getNormById(id)
        return ResponseEntity.ok(norm.toDto())
    }

    @GetMapping("/by-service")
    fun getByService(@RequestParam serviceName: String): Flow<ServiceReagentNormDto> = flow {
        getNormsByService(serviceName).forEach { emit(it.toDto()) }
    }

    @GetMapping("/by-category")
    fun getByCategory(@RequestParam category: String): Flow<ServiceReagentNormDto> = flow {
        getNormsByCategory(category).forEach { emit(it.toDto()) }
    }

    @PostMapping
    suspend fun create(@RequestBody request: CreateServiceReagentNormRequestDto): ResponseEntity<ServiceReagentNormDto> {
        val created = createNorm(
            serviceName = request.serviceName,
            serviceCategory = request.serviceCategory,
            analyzerId = request.analyzerId,
            reagentName = request.reagentName,
            consumableId = request.consumableId,
            quantityPerService = BigDecimal(request.quantityPerService),
            unitType = request.unitType,
            source = request.source ?: ServiceNormSource.MANUAL,
            sourceDocument = request.sourceDocument,
            notes = request.notes,
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toDto())
    }

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: String,
        @RequestBody request: CreateServiceReagentNormRequestDto,
    ): ResponseEntity<ServiceReagentNormDto> {
        val updated = updateNorm(
            id = id,
            serviceName = request.serviceName,
            serviceCategory = request.serviceCategory,
            analyzerId = request.analyzerId,
            reagentName = request.reagentName,
            consumableId = request.consumableId,
            quantityPerService = request.quantityPerService?.let { BigDecimal(it) },
            unitType = request.unitType,
            source = request.source,
            sourceDocument = request.sourceDocument,
            notes = request.notes,
        )
        return ResponseEntity.ok(updated.toDto())
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable id: String): ResponseEntity<Void> {
        deleteNorm(id)
        return ResponseEntity.noContent().build()
    }
}

/**
 * REST controller for service-to-analyzer mappings.
 * Manages auto-detection rules for which analyzer performs a given service.
 */
@RestController
@RequestMapping("/api/reagents/service-mappings")
class ServiceToAnalyzerMappingController(
    private val getAllMappings: GetAllServiceToAnalyzerMappingsUseCase,
    private val getMappingById: GetServiceToAnalyzerMappingByIdUseCase,
    private val findMatchingAnalyzer: FindMatchingAnalyzerForServiceUseCase,
    private val createMapping: CreateServiceToAnalyzerMappingUseCase,
    private val updateMapping: UpdateServiceToAnalyzerMappingUseCase,
    private val deleteMapping: DeleteServiceToAnalyzerMappingUseCase,
) {

    @GetMapping
    fun getAll(): Flow<ServiceToAnalyzerMappingDto> = flow {
        getAllMappings().forEach { emit(it.toDto()) }
    }

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: String): ResponseEntity<ServiceToAnalyzerMappingDto> {
        val mapping = getMappingById(id)
        return ResponseEntity.ok(mapping.toDto())
    }

    @GetMapping("/match")
    suspend fun findMatch(
        @RequestParam serviceName: String,
        @RequestParam(required = false) category: String?,
    ): ResponseEntity<ServiceToAnalyzerMappingDto?> {
        val match = findMatchingAnalyzer(serviceName, category)
        return if (match != null) {
            ResponseEntity.ok(match.toDto())
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @PostMapping
    suspend fun create(@RequestBody request: CreateServiceToAnalyzerMappingRequestDto): ResponseEntity<ServiceToAnalyzerMappingDto> {
        val created = createMapping(
            serviceNamePattern = request.serviceNamePattern,
            serviceCategory = request.serviceCategory,
            analyzerId = request.analyzerId,
            priority = request.priority ?: 100,
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toDto())
    }

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: String,
        @RequestBody request: CreateServiceToAnalyzerMappingRequestDto,
    ): ResponseEntity<ServiceToAnalyzerMappingDto> {
        val updated = updateMapping(
            id = id,
            serviceNamePattern = request.serviceNamePattern,
            serviceCategory = request.serviceCategory,
            analyzerId = request.analyzerId,
            priority = request.priority,
        )
        return ResponseEntity.ok(updated.toDto())
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable id: String): ResponseEntity<Void> {
        deleteMapping(id)
        return ResponseEntity.noContent().build()
    }
}
