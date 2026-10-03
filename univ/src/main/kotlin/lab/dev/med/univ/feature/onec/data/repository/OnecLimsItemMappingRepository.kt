package lab.dev.med.univ.feature.onec.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.onec.data.entity.OnecLimsItemMappingEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OnecLimsItemMappingRepository : CoroutineCrudRepository<OnecLimsItemMappingEntity, String> {
    fun findAllBySnapshotIdOrderByOnecItemNameAsc(snapshotId: String): Flow<OnecLimsItemMappingEntity>
    fun findAllBySnapshotIdAndMappingStatusOrderByOnecItemNameAsc(
        snapshotId: String,
        mappingStatus: String,
    ): Flow<OnecLimsItemMappingEntity>
}
