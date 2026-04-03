package project.gigienist_reports.core.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import project.gigienist_reports.core.config.properties.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
 import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Primary
import java.io.FileInputStream
import java.io.IOException

// Firebase Auth is disabled. To re-enable: set profile "firebase" and configure google credentials.
@Configuration
@Profile("firebase")
class FirebaseConfig(
    private val securityProperties: SecurityProperties
) {
    @Primary
    @Bean
    @Throws(IOException::class)
    fun init(): FirebaseApp {
        val stream = FileInputStream(securityProperties.firebaseProps.googleCredentials)
        val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build()

        return FirebaseApp.getApps().firstOrNull() ?: FirebaseApp.initializeApp(options)

    }

    @Bean
    @DependsOn("init") 
    fun auth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
