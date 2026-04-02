package lab.dev.med.univ.feature.reporting.data.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedWorkbookEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedSheetEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedRowEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedCellEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedMergedRegionEntity

@Repository
interface DamumedParsedWorkbookRepository : CoroutineCrudRepository<DamumedParsedWorkbookEntity, String>

@Repository
interface DamumedParsedSheetRepository : CoroutineCrudRepository<DamumedParsedSheetEntity, String> {
    fun findAllByUploadIdOrderBySheetIndexAsc(uploadId: String): Flow<DamumedParsedSheetEntity>

    suspend fun deleteAllByUploadId(uploadId: String)
}

@Repository
interface DamumedParsedRowRepository : CoroutineCrudRepository<DamumedParsedRowEntity, String> {
    fun findAllBySheetIdOrderByRowIndexAsc(sheetId: String): Flow<DamumedParsedRowEntity>

    fun findAllBySheetIdAndRowIndexLessThanEqualOrderByRowIndexAsc(sheetId: String, rowIndex: Int): Flow<DamumedParsedRowEntity>

    suspend fun deleteAllByUploadId(uploadId: String)
}

@Repository
interface DamumedParsedCellRepository : CoroutineCrudRepository<DamumedParsedCellEntity, String> {
    fun findAllBySheetIdOrderByRowIndexAscColumnIndexAsc(sheetId: String): Flow<DamumedParsedCellEntity>

    fun findAllBySheetIdAndRowIndexLessThanEqualOrderByRowIndexAscColumnIndexAsc(sheetId: String, rowIndex: Int): Flow<DamumedParsedCellEntity>

    suspend fun deleteAllByUploadId(uploadId: String)
}

@Repository
interface DamumedParsedMergedRegionRepository : CoroutineCrudRepository<DamumedParsedMergedRegionEntity, String> {
    fun findAllBySheetIdOrderByRegionIndexAsc(sheetId: String): Flow<DamumedParsedMergedRegionEntity>

    suspend fun deleteAllByUploadId(uploadId: String)
}
