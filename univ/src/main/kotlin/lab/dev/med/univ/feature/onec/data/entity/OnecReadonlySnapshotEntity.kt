package lab.dev.med.univ.feature.onec.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table("onec_readonly_snapshots")
data class OnecReadonlySnapshotEntity(
    @Id
    val id: String,
    val sourceName: String,
    val sourceKind: String,
    val sourceChecksum: String,
    val snapshotAt: LocalDateTime? = null,
    val periodFrom: LocalDate? = null,
    val periodTo: LocalDate? = null,
    val payloadJson: String,
    val metadataJson: String? = null,
    val importedAt: LocalDateTime = LocalDateTime.now(),
    val importedBy: String? = null,
    @Version
    val version: Long? = null,
)
