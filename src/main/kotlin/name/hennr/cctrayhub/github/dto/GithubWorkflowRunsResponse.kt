package name.hennr.cctrayhub.github.dto

class GithubWorkflowRunsResponse(total_count: Int, workflow_runs: Array<GithubWorkflowRun>, var githubResponseCode: GithubResponseCode = GithubResponseCode.SUCCESS) {
    val totalCount = total_count
    val latestWorkflowRun = workflow_runs.getOrNull(0)
}

data class GithubWorkflowRun(
    val status: String,
    val run_number: Int,
    val conclusion: String?, // this is null when a build is running
    val created_at: String,
    val html_url: String,
    val head_branch: String,
    val repository: GithubRepository)

data class GithubRepository(val name: String)


enum class GithubResponseCode {
    SUCCESS, NOT_FOUND, ERROR, TIMEOUT, CACHED
}
