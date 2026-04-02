package project.gigienist_reports.core.config.api

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono
import java.net.URI


@Hidden
@Controller
class BaseController {

	@GetMapping("/")
	fun indexController(response: ServerHttpResponse): Mono<Void?>? {
		response.statusCode = HttpStatus.PERMANENT_REDIRECT
		response.headers.location = URI.create("/swagger-ui.html")
		return response.setComplete()
	}

	@GetMapping("/raise")
	fun raise(): String {
		throw NotImplementedError("Test")
	}
}
