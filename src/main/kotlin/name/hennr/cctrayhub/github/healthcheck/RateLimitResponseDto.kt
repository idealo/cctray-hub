package name.hennr.cctrayhub.github.healthcheck

data class RateLimitResponseDto(val resources: Resources)

data class Resources(val core: Core)

data class Core(
    val limit: Int,
    val remaining: Int,
    val reset: Long,
    val used: Int
)
