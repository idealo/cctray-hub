package name.hennr.cctrayhub.github.client

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import de.mkammerer.wiremock.WireMockExtension
import name.hennr.cctrayhub.github.dto.GithubRepository
import name.hennr.cctrayhub.github.dto.GithubWorkflowRun
import name.hennr.cctrayhub.github.dto.GithubWorkflowRunsResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

@SpringBootTest
class GithubClientIT {

    companion object {
        @JvmField
        @RegisterExtension
        var wireMock = WireMockExtension(WireMockConfiguration.options().dynamicPort())
    }

    // use the spring-boot preconfigured webClient which honors the spring.codec.max-in-memory-size application property
    @Autowired
    lateinit var webClientBuilder: WebClient.Builder

    lateinit var githubClient: GithubClient

    @BeforeEach
    fun `configure githubClient to connect to WireMock`() {
        githubClient = GithubClient(
            githubApiBaseUrl = wireMock.baseUrl(),
            githubBearerToken = "666",
            webClientBuilder = webClientBuilder
        )
    }

    @Test
    fun `translates a green build from the github api to an internal GithubWorkflowRun object`() {
        // given
        aMockedGithubApiReturningASuccessfulBuild("hennr", "series-stalker", "maven.yml")
        val expectedResult = GithubWorkflowRunsResponse(
            84,
            arrayOf(
                GithubWorkflowRun(
                    "completed",
                    84,
                    "success",
                    "2021-08-23T06:04:02Z",
                    "https://github.com/hennr/series-stalker/actions/runs/1157529237",
                    "main",
                    GithubRepository("series-stalker")
                )
            )
        )
        // expect
        StepVerifier.create(githubClient.getWorkflowRuns("hennr", "series-stalker", "maven.yml"))
            .expectNextMatches {
                it.totalCount == expectedResult.totalCount &&
                it.latestWorkflowRun == expectedResult.latestWorkflowRun
            }
            .verifyComplete()
    }

    @Test
    fun `translates a running build from the github api to an internal GithubWorkflowRun object`() {
        // given
        val fileContent = this::class.java.getResource("/githubWorkflowResponseWithCurrentlyRunningBuild.json").readBytes()
        wireMock.stubFor(
            // ignoring query params like branch or per_page here
            get(urlPathMatching("^/repos/hennr/series-stalker/actions/workflows/maven.yml/runs"))
                .willReturn(
                    aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(fileContent)
                        .withStatus(200)
                )
        )
        val expectedResult = GithubWorkflowRunsResponse(
            5,
            arrayOf(
                GithubWorkflowRun(
                    "completed",
                    99,
                    "success",
                    "2021-09-24T19:12:47Z",
                    "https://github.com/hennr/series-stalker/actions/runs/1271057991",
                    "main",
                    GithubRepository("series-stalker")
                )
            )
        )
        // expect
        StepVerifier.create(githubClient.getWorkflowRuns("hennr", "series-stalker", "maven.yml"))
            .expectNextMatches {
                it.totalCount == expectedResult.totalCount &&
                it.latestWorkflowRun == expectedResult.latestWorkflowRun
            }
            .verifyComplete()
    }

    @Test
    fun `sends Accept header for github API v3`() {
        // given
        val githubWorkflowUrl = aMockedGithubApiReturningASuccessfulBuild("hennr", "series-stalker", "maven.yml")
        // when
        githubApiGetsRequestedFor("hennr", "series-stalker", "maven.yml")
        // then
        wireMock.verify(getRequestedFor(urlPathMatching("^$githubWorkflowUrl")) // ignoring query params like branch or per_page here
                .withHeader("Accept", equalTo("application/vnd.github.v3+json"))
        )
    }

    @Test
    fun `sends bearer token to github to deal with the API's rate limits`() {
        // given
        val githubWorkflowUrl = aMockedGithubApiReturningASuccessfulBuild("hennr", "series-stalker", "maven.yml")
        // when
        githubApiGetsRequestedFor("hennr", "series-stalker", "maven.yml")
        // then
        wireMock.verify(getRequestedFor(urlPathMatching("^$githubWorkflowUrl")) // ignoring query params like branch or per_page here
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer 666"))
        )
    }

    @Test
    fun `sends no If-None-Match header for the first request`() {
        // given
        val githubWorkflowUrl = aMockedGithubApiReturningASuccessfulBuild("hennr", "series-stalker", "maven.yml")
        // expect
        githubApiGetsRequestedFor("hennr", "series-stalker", "maven.yml")
        wireMock.verify(getRequestedFor(urlPathMatching("^$githubWorkflowUrl")) // ignoring query params like branch or per_page here
            .withoutHeader("If-None-Match")
        )
    }

    @Test
    fun `sends If-None-Match header for second request if ETAG has been received`() {
        // given
        val githubWorkflowUrl = aMockedGithubApiReturningASuccessfulBuild("hennr", "series-stalker", "maven.yml", "expectedEtag")
        // first request
        githubApiGetsRequestedFor("hennr", "series-stalker", "maven.yml")
        wireMock.verify(getRequestedFor(urlPathMatching("^$githubWorkflowUrl")) // ignoring query params like branch or per_page here
            .withoutHeader("If-None-Match")
        )
        // second request
        githubApiGetsRequestedFor("hennr", "series-stalker", "maven.yml")
        wireMock.verify(getRequestedFor(urlPathMatching("^$githubWorkflowUrl")) // ignoring query params like branch or per_page here
            .withHeader("If-None-Match", equalTo("expectedEtag"))
        )
    }

    @Test
    fun `updates If-None-Match header if new ETAG header was received`() {
        // given
        val githubWorkflowUrl = aMockedGithubApiReturningASuccessfulBuild(
            "hennr", "series-stalker", "maven.yml",
            "firstEtag")
        // first request
        githubApiGetsRequestedFor("hennr", "series-stalker", "maven.yml")
        wireMock.verify(getRequestedFor(urlPathMatching("^$githubWorkflowUrl")) // ignoring query params like branch or per_page here
            .withoutHeader("If-None-Match")
        )
        // second request
        githubApiGetsRequestedFor("hennr", "series-stalker", "maven.yml")
        wireMock.verify(getRequestedFor(urlPathMatching("^$githubWorkflowUrl")) // ignoring query params like branch or per_page here
            .withHeader("If-None-Match", equalTo("firstEtag"))
        )

        // new ETAG in response!
        aMockedGithubApiReturningASuccessfulBuild(
            "hennr", "series-stalker", "maven.yml",
            "secondEtag")
        // first request
        githubApiGetsRequestedFor("hennr", "series-stalker", "maven.yml")
        wireMock.verify(getRequestedFor(urlPathMatching("^$githubWorkflowUrl")) // ignoring query params like branch or per_page here
            .withHeader("If-None-Match", equalTo("firstEtag"))
        )
        // second request; new ETAG expected
        githubApiGetsRequestedFor("hennr", "series-stalker", "maven.yml")
        wireMock.verify(getRequestedFor(urlPathMatching("^$githubWorkflowUrl")) // ignoring query params like branch or per_page here
            .withHeader("If-None-Match", equalTo("secondEtag"))
        )
    }

    @Test
    fun `adds per_page query param to all requests to save bandwidth and time`() {
        // given
        val githubWorkflowUrl = aMockedGithubApiReturningASuccessfulBuild("hennr", "series-stalker", "maven.yml")
        // when
        githubApiGetsRequestedFor("hennr", "series-stalker", "maven.yml")
        // then
        wireMock.verify(
            getRequestedFor(urlPathMatching("^$githubWorkflowUrl"))
                .withQueryParam("per_page", equalTo("1"))
        )
    }

    @Test
    fun `adds branch query param to all requests to show only relevant builds`() {
        // given
        val githubWorkflowUrl = aMockedGithubApiReturningASuccessfulBuild("hennr", "series-stalker", "maven.yml")
        // when
        githubApiGetsRequestedFor("hennr", "series-stalker", "maven.yml")
        // then
        wireMock.verify(
            getRequestedFor(urlPathMatching("^$githubWorkflowUrl"))
                .withQueryParam("branch", equalTo("main"))
        )
    }

    @Test
    fun `handles API timeout by sending a empty response`() {
        // given
        val fileContent = this::class.java.getResource("/githubWorkflowResponse.json").readBytes()
        wireMock.stubFor(
            // ignoring query params here
            get(urlPathMatching("^/repos/hennr/series-stalker/actions/workflows/maven.yml/runs"))
                .willReturn(
                    aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(fileContent)
                        .withStatus(200)
                        .withFixedDelay(6000)
                )
        )

        // expect
        StepVerifier.create(githubClient.getWorkflowRuns("hennr", "series-stalker", "maven.yml"))
            .expectNextMatches {
                it.totalCount == 0 &&
                it.latestWorkflowRun == null
            }
            .verifyComplete()
    }

    @Test
    fun `return empty response for responses other than 200 github responses`() {
        // given
        val fileContent = this::class.java.getResource("/githubWorkflowResponse.json").readBytes()
        wireMock.stubFor(
            // ignoring query params here
            get(urlPathMatching("^/repos/hennr/series-stalker/actions/workflows/maven.yml/runs"))
                .willReturn(
                    aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(fileContent)
                        .withStatus(500)
                )
        )
        // expect
        StepVerifier.create(githubClient.getWorkflowRuns("hennr", "series-stalker", "maven.yml"))
            .expectNextMatches {
                it.totalCount == 0 &&
                it.latestWorkflowRun == null
            }
            .verifyComplete()
    }

    private fun aMockedGithubApiReturningASuccessfulBuild(
        githubGroup: String,
        githubRepo: String,
        githubWorkflowNameOrId: String,
        returnedEtag: String = "baeea7ccdd95f7d5c1e71d011182bf91f794dd860bca8e8938da7de1cca69790"): String {
        val url = "/repos/$githubGroup/$githubRepo/actions/workflows/$githubWorkflowNameOrId/runs"
        val fileContent = this::class.java.getResource("/githubWorkflowResponse.json").readBytes()
        wireMock.stubFor(
            get(urlPathMatching("^$url")) // ignoring query parameters for all test where this is not relevant
                .willReturn(
                    aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withHeader(HttpHeaders.ETAG, returnedEtag)
                        .withBody(fileContent)
                        .withStatus(200)
                )
        )
        return url
    }

    private fun githubApiGetsRequestedFor(githubGroup: String, githubRepo: String, githubWorkflowNameOrId: String) {
        StepVerifier.create(githubClient.getWorkflowRuns(githubGroup, githubRepo, githubWorkflowNameOrId))
            .expectNextMatches { it.totalCount == 84 }
            .verifyComplete()
    }
}
