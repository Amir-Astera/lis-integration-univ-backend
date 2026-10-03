package project.gigienist_reports.feature.authorization.presentation.dto

data class UserInfo(
        val email: String,
        val authorities: List<String> = emptyList(),
        val isAdmin: Boolean = false,
)
