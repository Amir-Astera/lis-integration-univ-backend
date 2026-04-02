package project.gigienist_reports.core.config.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
class FileStorageConfig {
    @Bean
    fun getLocation(@Value("\${file.storage.directory}") directory: String): Path {
        val location: Path = Paths.get(directory).toAbsolutePath().normalize()
        try {
            Files.createDirectories(location)
            return location
        } catch (ex: Exception) {
            throw IllegalStateException("Cannot create directory")
        }
    }
}