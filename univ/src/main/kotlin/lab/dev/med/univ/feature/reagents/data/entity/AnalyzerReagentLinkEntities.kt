package lab.dev.med.univ.feature.reagents.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("analyzer_reagent_links")
data class AnalyzerReagentLinkEntity(
    @Id val id: String,
    val analyzerId: String,
    val reagentInventoryId: String,
    val usageRole: String = "MAIN",
    val normReagentName: String? = null,
    val estimatedDailyMl: Double? = null,
    val estimatedDailyUnits: Int? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String? = null,
    @Version
    val version: Long? = null,
)
