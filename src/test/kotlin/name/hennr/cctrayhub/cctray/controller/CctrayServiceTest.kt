package name.hennr.cctrayhub.cctray.controller

import name.hennr.cctrayhub.github.client.GithubClient
import name.hennr.cctrayhub.github.dto.GithubRepository
import name.hennr.cctrayhub.github.dto.GithubResponseCode
import name.hennr.cctrayhub.github.dto.GithubWorkflowRun
import name.hennr.cctrayhub.github.dto.GithubWorkflowRunsResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

internal class CctrayServiceTest {

    @Test
    fun `returns cctray xml for a valid github client response`() {
        // given
        val githubClient: GithubClient = mock()
        whenever(githubClient.getWorkflowRuns("idealo", "cctray-hub", "build.yml")).thenReturn(
            Mono.just(
                GithubWorkflowRunsResponse(
                    666, arrayOf(
                        GithubWorkflowRun(
                            "completed",
                            666,
                            "success",
                            "Mon Oct 11 15:15:10 CEST 2021",
                            "https://github.com/idealo/cctray-hub",
                            "main",
                            GithubRepository("cctray-hub")
                        )
                    )
                )
            )
        )
        val cctrayService = CctrayService(githubClient)
        // expect
        StepVerifier.create(cctrayService.getLatestWorkflowRun("idealo", "cctray-hub", "build.yml"))
            .expectNextMatches {
                it.equals(
                    """<Projects> <Project name="cctray-hub - build.yml" activity="Sleeping" lastBuildLabel="666" lastBuildStatus="Success" lastBuildTime="Mon Oct 11 15:15:10 CEST 2021" webUrl="https://github.com/idealo/cctray-hub" /> </Projects>"""
                )
            }
            .verifyComplete()
    }

    @Test
    fun `returns fallback cctray xml for a failed github client response`() {
        // given
        val githubClient: GithubClient = mock()
        whenever(githubClient.getWorkflowRuns("idealo", "cctray-hub", "build.yml")).thenReturn(
            Mono.just(GithubWorkflowRunsResponse(0, emptyArray(), GithubResponseCode.ERROR))
        )
        val cctrayService = CctrayService(githubClient)
        // expect
        StepVerifier.create(cctrayService.getLatestWorkflowRun("idealo", "cctray-hub", "build.yml"))
            .expectNextMatches {
                it.startsWith("""<Projects> <Project name="cctray-hub - build.yml (main branch)" activity="CheckingModifications" lastBuildLabel="0" lastBuildStatus="Unknown" lastBuildTime="""")
                // not testing the dynamic build time here
                it.endsWith("""webUrl="https://github.com/idealo/cctray-hub/actions/workflows/build.yml" /> </Projects>""")
            }
            .verifyComplete()
    }

    @Test
    fun `returns fallback cctray xml for a successful github response without any know runs for that branch`() {
        // given
        val githubClient: GithubClient = mock()
        whenever(githubClient.getWorkflowRuns("idealo", "cctray-hub", "build.yml")).thenReturn(
            Mono.just(GithubWorkflowRunsResponse(0, emptyArray(), GithubResponseCode.SUCCESS))
        )
        val cctrayService = CctrayService(githubClient)
        // expect
        StepVerifier.create(cctrayService.getLatestWorkflowRun("idealo", "cctray-hub", "build.yml"))
            .expectNextMatches {
                it.startsWith("""<Projects> <Project name="cctray-hub - build.yml (main branch)" activity="CheckingModifications" lastBuildLabel="0" lastBuildStatus="Unknown" lastBuildTime="""")
                // not testing the dynamic build time here
                it.endsWith("""webUrl="https://github.com/idealo/cctray-hub/actions/workflows/build.yml" /> </Projects>""")
            }
            .verifyComplete()
    }

    @Test
    fun `returns Unknown status for a timed out github client response`() {
        // given
        val githubClient: GithubClient = mock()
        whenever(githubClient.getWorkflowRuns("idealo", "cctray-hub", "build.yml")).thenReturn(
            Mono.just(GithubWorkflowRunsResponse(0, emptyArray(), GithubResponseCode.TIMEOUT))
        )
        val cctrayService = CctrayService(githubClient)
        // expect
        StepVerifier.create(cctrayService.getLatestWorkflowRun("idealo", "cctray-hub", "build.yml"))
            .expectNextMatches {
                it.startsWith("""<Projects> <Project name="cctray-hub - build.yml (main branch)" activity="CheckingModifications" lastBuildLabel="0" lastBuildStatus="Unknown" lastBuildTime="""")
                // not testing the dynamic build time here
                it.endsWith("""webUrl="https://github.com/idealo/cctray-hub/actions/workflows/build.yml" /> </Projects>""")
            }
            .verifyComplete()
    }

    @Test
    fun `marks nonexistent workflows as failed to bug people about this and not waste our github rate limit`() {
        // given
        val githubClient: GithubClient = mock()
        whenever(githubClient.getWorkflowRuns("idealo", "cctray-hub", "does-not-exist.yml")).thenReturn(
            Mono.just(GithubWorkflowRunsResponse(0, emptyArray(), GithubResponseCode.NOT_FOUND))
        )
        val cctrayService = CctrayService(githubClient)
        // expect
        StepVerifier.create(cctrayService.getLatestWorkflowRun("idealo", "cctray-hub", "does-not-exist.yml"))
            .expectNextMatches {
                it.startsWith("""<Projects> <Project name="idealo/cctray-hub - does-not-exist.yml" activity="Sleeping" lastBuildLabel="0" lastBuildStatus="Failure" lastBuildTime="""")
                // not testing the dynamic build time here
                it.endsWith("""webUrl="https://github.com/idealo/cctray-hub/actions/workflows/does-not-exist.yml" /> </Projects>""")
            }
            .verifyComplete()
    }
}
