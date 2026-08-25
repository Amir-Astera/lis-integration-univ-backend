package lab.dev.med.univ.feature.reagents.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reagents.data.entity.ServiceCatalogEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ServiceCatalogRepository : CoroutineCrudRepository<ServiceCatalogEntity, String> {
    fun findAllByIsActiveTrueOrderByCanonicalNameAsc(): Flow<ServiceCatalogEntity>
    fun findAllByOrderByCategoryAscCanonicalNameAsc(): Flow<ServiceCatalogEntity>
    fun findAllByCategoryAndIsActiveTrueOrderByCanonicalNameAsc(category: String): Flow<ServiceCatalogEntity>
    suspend fun findByCanonicalNameIgnoreCase(canonicalName: String): ServiceCatalogEntity?
}
