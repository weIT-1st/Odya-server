package kr.weit.odya.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile("!test")
@RestController
class HealthController {
    @GetMapping("/health")
    fun health(): HealthResponse {
        return HealthResponse(status = HttpServletResponse.SC_OK)
    }

    @GetMapping("/ready")
    fun ready() {
    }
}

data class HealthResponse(
    val status: Int,
)
