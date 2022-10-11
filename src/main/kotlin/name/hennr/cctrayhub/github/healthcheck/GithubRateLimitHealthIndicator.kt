package name.hennr.cctrayhub.github.healthcheck

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.AbstractReactiveHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration
import java.util.*

@Component
class GithubRateLimitHealthIndicator(
    @Value("\${github.pat.bearer.token}") private val githubBearerToken: String,
    webClientBuilder: WebClient.Builder
) : AbstractReactiveHealthIndicator() {

    private val webClient = webClientBuilder
        .defaultHeaders { httpHeaders ->
            httpHeaders.setBearerAuth(githubBearerToken)
            httpHeaders.add("Accept", "application/vnd.github.v3+json")
        }
        .build()

    override fun doHealthCheck(builder: Health.Builder?): Mono<Health> {
        return Mono.from(getGithubRateLimit().map {
            val health: Health.Builder
            if (it.resources.core.remaining < 50) {
                health = Health.down()
            } else {
                health = Health.up()
            }
            health
                .withDetail("limit", it.resources.core.limit)
                .withDetail("used", it.resources.core.used)
                .withDetail("reset in min", (it.resources.core.reset * 1000 - Date().time) / 1000 / 60)
                .withDetail("remaining", it.resources.core.remaining)
                .build()
        })
    }

    private fun getGithubRateLimit(): Mono<RateLimitResponseDto> {
        return webClient.get()
            .uri(URI("https://api.github.com/rate_limit"))
            .retrieve()
            .bodyToMono<RateLimitResponseDto>()
            .checkpoint()
            .timeout(Duration.ofSeconds(2))
    }

}
