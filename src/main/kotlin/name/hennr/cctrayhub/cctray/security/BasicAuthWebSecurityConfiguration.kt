package name.hennr.cctrayhub.cctray.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.HttpBasicServerAuthenticationEntryPoint
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository


@Configuration
@EnableWebFluxSecurity
@ConditionalOnProperty("cctray-hub.username", "cctray-hub.password")
class BasicAuthWebSecurityConfiguration(
    @Value("\${cctray-hub.username}") private val username: String,
    @Value("\${cctray-hub.password}") private val password: String
) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity, httpBasicServerAuthenticationEntryPoint: HttpBasicServerAuthenticationEntryPoint): SecurityWebFilterChain {
        http
            .authorizeExchange { exchanges: AuthorizeExchangeSpec ->
                exchanges
                    .pathMatchers("/cctray/**").authenticated()
                    .anyExchange().permitAll()
            }
            .securityContextRepository(WebSessionServerSecurityContextRepository())
            .httpBasic()
            .authenticationEntryPoint(httpBasicServerAuthenticationEntryPoint)
        return http.build()
    }

    @Bean
    fun userDetailsService(): MapReactiveUserDetailsService? {
        val user = User.withDefaultPasswordEncoder()
            .username(username)
            .password(password)
            .roles("USER")
            .build()
        return MapReactiveUserDetailsService(user)
    }

    @Bean
    fun httpBasicServerAuthenticationEntryPoint(): HttpBasicServerAuthenticationEntryPoint {
        var entryPoint = HttpBasicServerAuthenticationEntryPoint()
        entryPoint.setRealm("cctray-hub")
        return entryPoint
    }

}
