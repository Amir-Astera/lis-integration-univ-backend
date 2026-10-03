package lab.dev.med.univ.feature.onec.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("onec_lims_item_mappings")
data class OnecLimsItemMappingEntity(
    @Id
    val id: String,
    val snapshotId: String,
    val onecItemRef: String? = null,
    val onecItemCode: String? = null,
    val onecItemName: String,
    val targetKind: String,
    val targetName: String,
    val targetUnit: String,
    val conversionFactor: BigDecimal = BigDecimal.ONE,
    val mappingStatus: String = "SUGGESTED",
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val updatedBy: String? = null,
    @Version
    val version: Long? = null,
)
