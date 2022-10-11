package name.hennr.cctrayhub.github.client

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import mu.KotlinLogging
import name.hennr.cctrayhub.github.dto.GithubResponseCode
import name.hennr.cctrayhub.github.dto.GithubWorkflowRunsResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration

@Service
class GithubClient(
    @Value("\${github.api.baseurl}") private val githubApiBaseUrl: String,
    @Value("\${github.pat.bearer.token}") private val githubBearerToken: String,
    webClientBuilder: WebClient.Builder
) {

    val logger = KotlinLogging.logger { }

    val etagCache: Cache<CacheKey, String> = Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(24)).build()
    val payloadCache: Cache<CacheKey, Mono<GithubWorkflowRunsResponse>> = Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(24)).build()

    private val webClient = webClientBuilder
        .defaultHeaders { httpHeaders ->
            httpHeaders.setBearerAuth(githubBearerToken)
            httpHeaders.add("Accept", "application/vnd.github.v3+json")
        }
        .build()

    fun getWorkflowRuns(githubGroup: String, githubRepo: String, githubWorkflowNameOrId: String): Mono<GithubWorkflowRunsResponse> {
        return webClient.get()
            .uri(URI("${githubApiBaseUrl}/repos/$githubGroup/$githubRepo/actions/workflows/$githubWorkflowNameOrId/runs?branch=main&per_page=1"))
            .headers { headers ->
                etagCache.getIfPresent(CacheKey(githubGroup, githubRepo, githubWorkflowNameOrId))?.also {
                        etag: String -> headers.setIfNoneMatch(etag)
                    }
            }
            .exchangeToMono { response ->
                if (response.statusCode() == HttpStatus.NOT_FOUND) {
                    handleNotFound(githubGroup, githubRepo, githubWorkflowNameOrId)
                } else if (response.statusCode() == HttpStatus.NOT_MODIFIED) {
                    handleNotModified(githubGroup, githubRepo, githubWorkflowNameOrId)
                } else if (response.statusCode().isError) {
                    handleError(githubGroup, githubRepo, githubWorkflowNameOrId)
                } else {
                    handleIsOk(response, githubGroup, githubRepo, githubWorkflowNameOrId)
                }
            }
            .timeout(Duration.ofSeconds(5))
            .doOnNext { cacheGithubPayload(it, githubGroup, githubRepo, githubWorkflowNameOrId) }
            .onErrorReturn(GithubWorkflowRunsResponse(0, emptyArray(), GithubResponseCode.TIMEOUT))
    }

    private fun cacheGithubPayload(it: GithubWorkflowRunsResponse, githubGroup: String, githubRepo: String, githubWorkflowNameOrId: String) {
        if (it.githubResponseCode == GithubResponseCode.SUCCESS) {
            logger.debug("caching response for $githubGroup/$githubRepo/$githubWorkflowNameOrId")
            payloadCache.put(
                CacheKey(githubGroup, githubRepo, githubWorkflowNameOrId),
                Mono.just(it.also { it.githubResponseCode = GithubResponseCode.CACHED })
            )
        }
    }

    private fun handleIsOk(response: ClientResponse, githubGroup: String, githubRepo: String, githubWorkflowNameOrId: String): Mono<GithubWorkflowRunsResponse> {
        // write etag to etag cache to be able to send it in the next request to hope
        // for a 304 - Not Modified - in which case we can get the result from the payload cache
        if (response.headers().header("etag").isNotEmpty()) {
            etagCache.put(
                CacheKey(githubGroup, githubRepo, githubWorkflowNameOrId),
                response.headers().header("etag")[0])
        }
        return response.bodyToMono(GithubWorkflowRunsResponse::class.java) // GithubWorkflowRunsResponse marked as SUCCESS per default
    }

    private fun handleError(githubGroup: String, githubRepo: String, githubWorkflowNameOrId: String): Mono<GithubWorkflowRunsResponse> {
        logger.error("failed to fetch details for repo: $githubGroup/$githubRepo/$githubWorkflowNameOrId")
        return Mono.just(GithubWorkflowRunsResponse(0, emptyArray(), GithubResponseCode.ERROR))
    }

    private fun handleNotModified(githubGroup: String, githubRepo: String, githubWorkflowNameOrId: String): Mono<GithubWorkflowRunsResponse> {
        logger.debug("providing cached response for $githubGroup/$githubRepo/$githubWorkflowNameOrId")
        val cachedGithubWorkflowRunsResponse = payloadCache.getIfPresent(CacheKey(githubGroup, githubRepo, githubWorkflowNameOrId))
        return cachedGithubWorkflowRunsResponse ?: Mono.just(GithubWorkflowRunsResponse(0, emptyArray(), GithubResponseCode.NOT_FOUND)
        )
    }

    private fun handleNotFound(githubGroup: String, githubRepo: String, githubWorkflowNameOrId: String): Mono<GithubWorkflowRunsResponse> {
        logger.warn { "nonexistent repo requested: $githubGroup/$githubRepo/$githubWorkflowNameOrId" }
        return Mono.just(GithubWorkflowRunsResponse(0, emptyArray(), GithubResponseCode.NOT_FOUND))
    }
}

data class CacheKey(val githubGroup: String, val githubRepo: String, val githubWorkflowNameOrId: String)
