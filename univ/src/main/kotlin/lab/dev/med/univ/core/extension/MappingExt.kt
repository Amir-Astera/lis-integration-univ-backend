package project.gigienist_reports.core.extension

import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.ApiFutures
import com.google.common.util.concurrent.MoreExecutors
import project.gigienist_reports.feature.authority.data.entity.AuthorityEntity
import project.gigienist_reports.feature.authority.domain.models.Authority
import project.gigienist_reports.feature.city.data.CityEntity
import project.gigienist_reports.feature.city.data.CityOfficeEntity
import project.gigienist_reports.feature.city.domain.models.City
import project.gigienist_reports.feature.city.domain.models.CityOffice
import project.gigienist_reports.feature.clinic.data.ClinicEntity
import project.gigienist_reports.feature.clinic.data.PriceItemEntity
import project.gigienist_reports.feature.clinic.domain.models.Clinic
import project.gigienist_reports.feature.clinic.domain.models.PriceItem
import project.gigienist_reports.feature.files.data.FileEntity
import project.gigienist_reports.feature.files.domain.models.File
import project.gigienist_reports.feature.push.data.PushSubscriptionEntity
import project.gigienist_reports.feature.push.domain.models.PushSubscription
import project.gigienist_reports.feature.report.data.ReportEntity
import project.gigienist_reports.feature.report.domain.models.Report
import project.gigienist_reports.feature.users.data.entity.UserEntity
import project.gigienist_reports.feature.users.domain.models.UserAggregate
import reactor.core.publisher.Mono
import java.time.LocalDateTime

fun UserEntity.toModel(
        authorities: Collection<Authority>,
): UserAggregate {
    return UserAggregate(
            id = id,
            name = name,
            surname = surname,
            email = email,
            phone = phone,
            login = login,
            authorities = authorities,
            logoUrl = logo,
            version = version,
            createdAt = createdAt,
            updatedAt = updatedAt
    )
}

fun AuthorityEntity.toModel(
        version: Long? = null,
        createdAt: LocalDateTime? = null
): Authority {
    return Authority(
            id = id,
            name = name,
            description = description,
            version = version ?: this.version,
            createdAt = createdAt ?: this.createdAt,
            updatedAt = updatedAt
    )
}

fun Authority.toEntity(): AuthorityEntity {
    return AuthorityEntity(
            id = id,
            name = name,
            description = description,
            version = version,
            createdAt = createdAt,
            updatedAt = updatedAt
    )
}

fun UserAggregate.toEntity(): UserEntity {
    return UserEntity(
            id = id,
            name = name,
            surname = surname,
            email = email,
            phone = phone,
            login = login,
            logo = logoUrl,
            version = version,
            createdAt = createdAt,
            updatedAt = updatedAt
    )
}

fun FileEntity.toModel(): File {
    return File(
            id = id,
            directory = directory,
            format = format,
            url = url
    )
}

fun File.toEntity(): FileEntity {
    return FileEntity(
            id = id,
            directory = directory,
            format = format,
            url = url,
            version = version,
            createdAt = LocalDateTime.now()
    )
}

fun CityEntity.toModel(): City {
    return City(
            id = id,
            name = name,
            version = version,
            createdAt = createdAt,
            updatedAt = updatedAt
    )
}

fun City.toEntity(): CityEntity {
    return CityEntity(
            id = id,
            name = name,
            version = version,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
    )
}
//
//fun PatientEntity.toModel(): Patient {
//    return Patient(
//            id = id,
//            internalId = internalId,
//            iin = iin,
//            firstName = firstName,
//            secondName = secondName,
//            lastName = lastName,
//            birthDay = birthDay,
//            sex = sex,
//            numDoc = numDoc
//    )
//}
//
//fun Patient.toEntity(): PatientEntity {
//    return PatientEntity(
//            id = id,
//            internalId = internalId!!,
//            iin = iin,
//            firstName = firstName,
//            secondName = secondName,
//            lastName = lastName,
//            birthDay = birthDay,
//            sex = sex,
//            numDoc = numDoc,
//            version = null,
//            createdAt = LocalDateTime.now(),
//            updatedAt = LocalDateTime.now()
//    )
//}
//
//fun OrderEntity.toModel(
//        patient: Patient,
//        city: City,
//        cityOffice: CityOffice
//): Order {
//     return Order(
//             id = id,
//             patient = patient,
//             internalId = internalId,
//             totalPrice = totalPrice,
//             totalCount = totalCount,
//             status = status,
//             city = city,
//             cityOffice = cityOffice,
//             version = version,
//             createdAt = updatedAt,
//             updatedAt = updatedAt
//     )
//}
//
//fun Order.toEntity(patientId: String): OrderEntity {
//    return OrderEntity(
//            id = id,
//            internalId = internalId,
//            patientId = patientId,
//            totalPrice = totalPrice,
//            totalCount = totalCount,
//            status = status,
//            cityId = city.id,
//            cityOfficeId = cityOffice.id,
//            version = version,
//            createdAt = createdAt,
//            updatedAt = updatedAt
//    )
//}
//
//fun OrderAnalysisEntity.toModel(
//        order: Order,
//        analysis: Analysis
//): OrderAnalyses {
//    return OrderAnalyses(
//            id = id,
//            order = order,
//            analysis = analysis,
//            ids = ids,
//            price = price,
//            status = status,
//            version = version,
//            createdAt = createdAt,
//            updatedAt = updatedAt ?: LocalDateTime.now()
//    )
//}
//
//fun OrderAnalyses.toEntity(): OrderAnalysisEntity {
//    return OrderAnalysisEntity(
//            id = id,
//            orderId = order.id,
//            analysisId = analysis.id,
//            ids = ids,
//            price = price,
//            status = status,
//            version = version,
//            createdAt = createdAt,
//            updatedAt = updatedAt
//    )
//}
//
fun ReportEntity.toModel(): Report {
    return Report(
            id = id,
            reportCode = reportCode,
            reportName = reportName,
            type = type,
            version = version,
            updatedAt = updatedAt,
            createdAt = createdAt
    )
}

fun Report.toEntity(): ReportEntity {
    return ReportEntity(
            id = id,
            reportCode = reportCode,
            reportName = reportName,
            type = type,
            version = version,
            updatedAt = updatedAt,
            createdAt = createdAt
    )
}

fun CityOfficeEntity.toModel(): CityOffice {
    return CityOffice(
            id = id,
            name = name,
            version = version,
            updatedAt = updatedAt,
            createdAt = createdAt
    )
}

fun CityOffice.toEntity(
    cityId: String
): CityOfficeEntity {
    return CityOfficeEntity(
            id = id,
            cityId = cityId,
            name = name,
            version = version,
            updatedAt = updatedAt ?: LocalDateTime.now(),
            createdAt = createdAt
    )
}

fun ClinicEntity.toModel(): Clinic {
    return Clinic(
        id = id,
        code = code,
        name = name,
        address = address,
        phone = phone,
        email = email,
        distanceFromLab = distanceFromLab,
        active = active,
        priceVersion = priceVersion,
        cityOfficeId = cityOfficeId,
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Clinic.toEntity(): ClinicEntity {
    return ClinicEntity(
        id = id,
        code = code,
        name = name,
        address = address,
        phone = phone,
        email = email,
        distanceFromLab = distanceFromLab,
        active = active,
        priceVersion = priceVersion,
        cityOfficeId = cityOfficeId,
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt ?: LocalDateTime.now()
    )
}

fun PriceItemEntity.toModel(): PriceItem {
    return PriceItem(
        id = id,
        customerId = customerId,
        code = code,
        name = name,
        price = price,
        currency = currency,
        effectiveFrom = effectiveFrom,
        effectiveTo = effectiveTo,
        priceVersion = priceVersion,
        version = version,
        createdAt = createdAt
    )
}

fun PriceItem.toEntity(): PriceItemEntity {
    return PriceItemEntity(
        id = id,
        customerId = customerId,
        code = code,
        name = name,
        price = price,
        currency = currency ?: "KZT",
        effectiveFrom = effectiveFrom ?: LocalDateTime.now(),
        effectiveTo = effectiveTo,
        priceVersion = priceVersion,
        version = version,
        createdAt = createdAt ?: LocalDateTime.now(),
        updatedAt = updatedAt ?: LocalDateTime.now()
    )
}

fun <T> apiFutureToMono(apiFuture: ApiFuture<T>): Mono<T> {
    return Mono.create { sink ->
        ApiFutures.addCallback(apiFuture, object : ApiFutureCallback<T> {
            override fun onSuccess(result: T) {
                sink.success(result)
            }
            override fun onFailure(t: Throwable) {
                sink.error(t)
            }
        }, MoreExecutors.directExecutor())
    }
}

fun PushSubscriptionEntity.toModel() = PushSubscription(
    id, userId, endpoint, p256dh, auth, userAgent, active, version, createdAt, updatedAt
)

fun PushSubscription.toEntity() = PushSubscriptionEntity(
    id, userId, endpoint, p256dh, auth, userAgent, active, version, createdAt, updatedAt
)