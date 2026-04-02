package project.gigienist_reports.feature.authority.domain.models


import java.time.LocalDateTime
import java.util.*
import org.springframework.security.core.GrantedAuthority

data class Authority (
        val id: String = UUID.randomUUID().toString(),
        var name: String = "",
        var description: String = "",
        val version: Long? = null,
        val createdAt: LocalDateTime? = null,
        var updatedAt: LocalDateTime = LocalDateTime.now()
): GrantedAuthority {

    override fun getAuthority(): String = name

    fun update(name: String?, description: String?) {
        updateName(name)
        updateDescription(description)
        updated()
    }

    private fun updated(){
        this.updatedAt = LocalDateTime.now()
    }

    private fun updateName(name: String?) {
        if (name != null) {
            this.name = name
            this.updatedAt = LocalDateTime.now()
        }
    }

    private fun updateDescription(description: String?) {
        if (description != null) {
            this.description = description
            this.updatedAt = LocalDateTime.now()
        }
    }

}