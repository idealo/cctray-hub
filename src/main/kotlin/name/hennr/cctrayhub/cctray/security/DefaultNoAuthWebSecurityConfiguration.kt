package name.hennr.cctrayhub.cctray.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@ConditionalOnMissingBean(BasicAuthWebSecurityConfiguration::class)
class DefaultNoAuthWebSecurityConfiguration {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeRequests().anyRequest().permitAll()
        return http.build()
    }

}