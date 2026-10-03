package lab.dev.med.univ.feature.onec.data.repository

import lab.dev.med.univ.feature.onec.data.entity.OnecReadonlySnapshotEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OnecReadonlySnapshotRepository : CoroutineCrudRepository<OnecReadonlySnapshotEntity, String> {
    suspend fun findFirstByOrderByImportedAtDesc(): OnecReadonlySnapshotEntity?
    suspend fun findBySourceChecksum(sourceChecksum: String): OnecReadonlySnapshotEntity?
}
