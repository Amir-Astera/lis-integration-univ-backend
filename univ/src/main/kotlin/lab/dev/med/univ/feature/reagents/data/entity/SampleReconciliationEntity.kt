package lab.dev.med.univ.feature.reagents.data.entity

import lab.dev.med.univ.feature.reagents.domain.models.SampleReconciliation
import lab.dev.med.univ.feature.reagents.domain.models.SampleReconciliationStatus
import lab.dev.med.univ.feature.reagents.domain.models.ServiceMatchConfidence
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Table("sample_reconciliation")
data class SampleReconciliationEntity(
    @Id val id: String,
    val parsedSampleId: String,
    val sampleDate: LocalDate,
    val analyzerId: String? = null,
    val serviceCatalogId: String? = null,
    val serviceNameRaw: String? = null,
    val serviceNameCanonical: String? = null,
    val category: String? = null,
    val reconciliationStatus: String,
    val reason: String? = null,
    val graceHours: Int = 0,
    val graceDeadlineAt: LocalDateTime? = null,
    val matchConfidence: String? = null,
    val lisPricePerTestTenge: BigDecimal? = null,
    val estimatedWasteTenge: BigDecimal = BigDecimal.ZERO,
    val lisReferralKey: String? = null,
    val reconciledAt: LocalDateTime = LocalDateTime.now(),
    @Version val version: Long? = null,
)

fun SampleReconciliationEntity.toModel() = SampleReconciliation(
    id                    = id,
    parsedSampleId        = parsedSampleId,
    sampleDate            = sampleDate,
    analyzerId            = analyzerId,
    serviceCatalogId      = serviceCatalogId,
    serviceNameRaw        = serviceNameRaw,
    serviceNameCanonical  = serviceNameCanonical,
    category              = category,
    reconciliationStatus  = runCatching { SampleReconciliationStatus.valueOf(reconciliationStatus) }
        .getOrDefault(SampleReconciliationStatus.DISCREPANCY),
    reason                = reason,
    graceHours            = graceHours,
    graceDeadlineAt       = graceDeadlineAt,
    matchConfidence       = matchConfidence?.let {
        runCatching { ServiceMatchConfidence.valueOf(it) }.getOrNull()
    },
    lisPricePerTestTenge  = lisPricePerTestTenge,
    estimatedWasteTenge   = estimatedWasteTenge,
    lisReferralKey        = lisReferralKey,
    reconciledAt          = reconciledAt,
    version               = version,
)

fun SampleReconciliation.toEntity() = SampleReconciliationEntity(
    id                    = id,
    parsedSampleId        = parsedSampleId,
    sampleDate            = sampleDate,
    analyzerId            = analyzerId,
    serviceCatalogId      = serviceCatalogId,
    serviceNameRaw        = serviceNameRaw,
    serviceNameCanonical  = serviceNameCanonical,
    category              = category,
    reconciliationStatus  = reconciliationStatus.name,
    reason                = reason,
    graceHours            = graceHours,
    graceDeadlineAt       = graceDeadlineAt,
    matchConfidence       = matchConfidence?.name,
    lisPricePerTestTenge  = lisPricePerTestTenge,
    estimatedWasteTenge   = estimatedWasteTenge,
    lisReferralKey        = lisReferralKey,
    reconciledAt          = reconciledAt,
    version               = version,
)
