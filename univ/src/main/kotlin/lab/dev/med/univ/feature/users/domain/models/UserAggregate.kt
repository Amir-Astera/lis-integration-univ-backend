package project.gigienist_reports.feature.users.domain.models

import project.gigienist_reports.feature.authority.domain.errors.AuthorityNotFoundException
import project.gigienist_reports.feature.authority.domain.models.Authority
import project.gigienist_reports.feature.users.presentation.dto.UpdateUserDto
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class UserAggregate(
        val id: String = UUID.randomUUID().toString(),
        login: String,
        name: String,
        surname: String?,
        email: String?,
        phone: String?,
        var logoUrl: String? = null,
        val createdAt: LocalDateTime? = null,
        version: Long? = null,
        updatedAt: LocalDateTime = LocalDateTime.now(),
        authorities: Collection<Authority> = emptyList()
) {
    var login = login
        private set

    var name = name
        private set

    var surname = surname
        private set

    var email = email
        private set

    var phone = phone
        private set

    var version = version
        private set

    var updatedAt: LocalDateTime = updatedAt
        private set

    private val   _authorities = authorities.toMutableList()
    val authorities: Collection<Authority>
        get() = _authorities

    fun updateStatus(status: UserStatus) {
        this.updatedAt = LocalDateTime.now()
    }

    fun addAuthority(authorityId: String): Authority {
        return _authorities.find { it.id == authorityId }
                ?: Authority(authorityId).also { _authorities.add(it) }
    }

    fun deleteAuthority(authorityId: String): Authority {
        return _authorities.find { it.id == authorityId }?.also { _authorities.remove(it) }
                ?: throw AuthorityNotFoundException()
    }

    fun update(dto: UpdateUserDto) {
        this.name = dto.name ?: this.name
        this.surname = dto.surname ?: this.surname
        this.email = dto.email ?: this.email
        this.phone = dto.phone ?: this.phone
    }

    fun updateAuthorities(authorityIds: Collection<String>) {
        val currentAuthorities = ArrayList(_authorities)

        currentAuthorities.forEach { currentAuthority ->
            if (!authorityIds.contains(currentAuthority.id)) {
                deleteAuthority(currentAuthority.id)
            }
        }

        authorityIds.forEach { authorityId ->
            if (currentAuthorities.isEmpty()) {
                currentAuthorities.add(addAuthority(authorityId))
            }
            currentAuthorities.find { it.id != authorityId }.also {
                addAuthority(authorityId)
            }
        }
    }


    fun checkAdminAuthority(): Boolean {
        val adminAuthority = this.authorities.find { it.name.toLowerCase() == "admin" }
        return adminAuthority != null
    }

    fun hasPublisherOrUserAuthority(): Boolean {
        val authority = this.authorities.find { it.name.toLowerCase() == "user" || it.name.toLowerCase() == "publisher" }
        return authority != null
    }

    fun compileLogoPath(fileExt: String) = "${id}/logo$fileExt"

    fun addLogin(login: String) {
        this.login = login
    }

    enum class UserStatus(val value: Short) {
        ACTIVE(1),
        INACTIVE(2),
        ARCHIVED(3);

        companion object {
            fun from(value: Short): UserStatus {
                return entries.find { it.value == value }
                        ?: throw IllegalArgumentException("Status '$value' not defined")
            }
        }
    }
}