package name.hennr.cctrayhub.cctray.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
@ConditionalOnMissingBean(BasicAuthWebSecurityConfiguration::class)
class DefaultNoAuthWebSecurityConfiguration {

    @Bean
    fun filterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.authorizeExchange { it.anyExchange().permitAll() }
        return http.build()
    }

}
