package project.gigienist_reports.feature.authorization.presentation.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class AuthResponseDto(
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("refresh_token")
    val refreshToken: String,
    @JsonProperty("expires_in")
    val expiresIn: String
)