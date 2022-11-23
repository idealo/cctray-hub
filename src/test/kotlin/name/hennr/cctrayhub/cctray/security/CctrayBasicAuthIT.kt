package name.hennr.cctrayhub.cctray.security


import name.hennr.cctrayhub.cctray.controller.CctrayService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic

import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import reactor.core.publisher.Mono

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["cctray-hub.username=test_username", "cctray-hub.password=test_password"])
@AutoConfigureMockMvc
class CctrayBasicAuthIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var cctrayService: CctrayService

    @BeforeEach
    fun setUp() {
        whenever(cctrayService.getLatestWorkflowRun("group", "repo","workflow.yml")).thenReturn(Mono.empty())
    }

    @Test
    fun `returns 200 for unprotected endpoint`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/actuator"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `returns WWW-Authenticate response header for unauthenticated call`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/cctray/group/repo/workflow.yml"))
            .andExpect(MockMvcResultMatchers.header().exists("WWW-Authenticate"))
    }

    @Test
    fun `returns 401 for unauthenticated call`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/cctray/group/repo/workflow.yml"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `returns 200 for authenticated call`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/cctray/group/repo/workflow.yml")
            .with(httpBasic("test_username","test_password")))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
}