//
//interface GetUserPatientUseCase {
//    suspend operator fun invoke(phone: String): Map<String, Any>
//}
//
//@Service
//internal class GetUserPatientUseCaseImpl(
//    private val userAggregateService: UserAggregateService,
//    private val patientService: PatientService
//): GetUserPatientUseCase {
//    override suspend fun invoke(phone: String): Map<String, Any> {
//        val user = userAggregateService.getByPhone(phone) ?: throw UserNotFoundException()
//        val patient = patientService.getByUser(user.id)
//        return mapOf(
//                "user" to user,
//                "patient" to patient
//        )
//    }
//}