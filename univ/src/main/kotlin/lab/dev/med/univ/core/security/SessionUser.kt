package project.gigienist_reports.core.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class SessionUser(
    val name: String,
    val login: String,
    val isEmailVerified: Boolean,
    val issuer: String,
    val picture: String,
    authorities: Collection<GrantedAuthority>

) : User(login, "", true, true, true, true, authorities)