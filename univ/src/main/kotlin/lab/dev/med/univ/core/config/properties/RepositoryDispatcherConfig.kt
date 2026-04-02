package project.gigienist_reports.core.config.properties

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.reactor.asCoroutineDispatcher
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors

@Configuration
class RepositoryDispatcherConfig {

    @Bean
    fun dispatcher(
            @Value("5")
            connectionPoolSize: Int,
    ): CoroutineDispatcher {
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize)).asCoroutineDispatcher()
    }
}