package lab.dev.med.univ.feature.reagents.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table("reconciliation_source_coverage")
data class ReconciliationSourceCoverageEntity(
    @Id
    val id: String,
    val coverageDate: LocalDate,
    val analyzerId: String? = null,
    val sourceKind: String,
    val sourceUploadId: String? = null,
    val factCount: Int = 0,
    val coverageQuality: String,
    val coverageReason: String? = null,
    val observedAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
)
