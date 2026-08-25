package lab.dev.med.univ

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@ConfigurationPropertiesScan(basePackages = ["lab.dev.med.univ", "project.gigienist_reports"])
@SpringBootApplication(scanBasePackages = ["lab.dev.med.univ", "project.gigienist_reports"])
@EnableR2dbcRepositories(basePackages = ["lab.dev.med.univ", "project.gigienist_reports"])
@EnableScheduling
class UnivApplication

fun main(args: Array<String>) {
	runApplication<UnivApplication>(*args)
}
