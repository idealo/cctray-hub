package name.hennr.cctrayhub.cctray.security


import name.hennr.cctrayhub.cctray.controller.CctrayService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource

import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = ["cctray-hub.username=test_username", "cctray-hub.password=test_password"])
class CctrayBasicAuthIT {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var cctrayService: CctrayService

    @BeforeEach
    fun setUp() {
        whenever(cctrayService.getLatestWorkflowRun("group", "repo","workflow.yml")).thenReturn(Mono.empty())
    }

    @Test
    @WithAnonymousUser
    fun `returns 200 for unprotected endpoint`() {
        webTestClient.get()
            .uri("/actuator")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @WithAnonymousUser
    fun `returns WWW-Authenticate response header for unauthenticated call`() {
        webTestClient.get()
            .uri("/cctray/group/repo/workflow.yml")
            .exchange()
            .expectHeader().exists("WWW-Authenticate")
    }

    @Test
    @WithAnonymousUser
    fun `returns 401 for unauthenticated call`() {
        webTestClient.get()
            .uri("/cctray/group/repo/workflow.yml")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    @WithMockUser
    fun `returns 200 for authenticated call`() {
        webTestClient.get()
            .uri("/cctray/group/repo/workflow.yml")
            .exchange()
            .expectStatus().isOk
    }
}