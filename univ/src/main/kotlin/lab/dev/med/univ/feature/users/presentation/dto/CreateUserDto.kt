package project.gigienist_reports.feature.users.presentation.dto


data class CreateUserDto(
    val name: String,
    val login: String?,
    val password: String?,
    val surname: String?,
    val email: String,
    val phone: String,
    val authorityIds: List<String>?
)