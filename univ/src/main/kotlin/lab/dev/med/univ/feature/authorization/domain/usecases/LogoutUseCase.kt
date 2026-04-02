package project.gigienist_reports.feature.authorization.domain.usecases

import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Service
import java.time.Duration

interface LogoutUseCase {
    suspend operator fun invoke(response: ServerHttpResponse)
}

@Service
internal class LogoutUseCaseImpl(

): LogoutUseCase {
    override suspend fun invoke(response: ServerHttpResponse) {
        val removeCookie = ResponseCookie.from("SESSIONID", "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofSeconds(0))
                .sameSite("None")
                .build()

        response.addCookie(removeCookie)
    }

}