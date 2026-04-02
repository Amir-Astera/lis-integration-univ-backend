package lab.dev.med.univ.feature.reporting.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedSectionEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DamumedNormalizedSectionRepository : CoroutineCrudRepository<DamumedNormalizedSectionEntity, String> {
    fun findAllByUploadIdOrderBySheetIdAscRowStartIndexAsc(uploadId: String): Flow<DamumedNormalizedSectionEntity>

    suspend fun deleteAllByUploadId(uploadId: String)
}

@Repository
interface DamumedNormalizedDimensionRepository : CoroutineCrudRepository<DamumedNormalizedDimensionEntity, String> {
    fun findAllByUploadIdOrderByAxisKeyAscDisplayValueAsc(uploadId: String): Flow<DamumedNormalizedDimensionEntity>

    suspend fun deleteAllByUploadId(uploadId: String)
}

@Repository
interface DamumedNormalizedFactRepository : CoroutineCrudRepository<DamumedNormalizedFactEntity, String> {
    fun findAllByUploadIdOrderBySheetIdAscSourceRowIndexAscSourceColumnIndexAsc(uploadId: String): Flow<DamumedNormalizedFactEntity>

    suspend fun deleteAllByUploadId(uploadId: String)
}

@Repository
interface DamumedNormalizedFactDimensionRepository : CoroutineCrudRepository<DamumedNormalizedFactDimensionEntity, String> {
    fun findAllByFactIdOrderByAxisKeyAsc(factId: String): Flow<DamumedNormalizedFactDimensionEntity>

    suspend fun deleteAllByFactId(factId: String)
}
