package project.gigienist_reports.feature.users.domain.services

import com.google.firebase.auth.UserRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import project.gigienist_reports.core.config.FirebaseConfig
import project.gigienist_reports.feature.authorization.domain.services.LocalSessionAuthenticationService

interface UserIdentityProvisioningService {
    suspend fun createUser(userId: String, email: String?, password: String?)
}

@Service
@Profile("firebase")
class FirebaseUserIdentityProvisioningService(
    private val firebaseConfig: FirebaseConfig,
) : UserIdentityProvisioningService {
    override suspend fun createUser(userId: String, email: String?, password: String?) {
        withContext(Dispatchers.IO) {
            val request = UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setUid(userId)
            firebaseConfig.auth().createUser(request)
        }
    }
}

@Service
@Profile("!firebase")
class LocalUserIdentityProvisioningService(
    private val localSessionAuthenticationService: LocalSessionAuthenticationService,
) : UserIdentityProvisioningService {
    override suspend fun createUser(userId: String, email: String?, password: String?) {
        localSessionAuthenticationService.upsertCredential(userId, email, password)
    }
}
