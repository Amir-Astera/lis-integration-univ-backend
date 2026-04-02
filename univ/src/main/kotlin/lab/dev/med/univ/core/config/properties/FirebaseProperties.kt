package project.gigienist_reports.core.config.properties

data class FirebaseProperties(
    val sessionExpiryInDays: Int,
    val enableStrictServerSession: Boolean,
    val enableCheckSessionRevoked: Boolean,
    val enableLogoutEverywhere: Boolean,
    val apiIdentityUrl: String,
    val apiKey: String,
    val googleCredentials: String
)