package project.gigienist_reports.core.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.cache.annotation.EnableCaching
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager =
        CaffeineCacheManager("priceMap").apply {
            setCaffeine(
                Caffeine.newBuilder()
                    .maximumSize(500)                       // сколько (customerId:version) держим
                    .expireAfterAccess(Duration.ofDays(7))  // TTL в памяти
            )
        }
}
