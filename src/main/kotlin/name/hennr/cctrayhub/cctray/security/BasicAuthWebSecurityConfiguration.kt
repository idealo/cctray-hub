package name.hennr.cctrayhub.cctray.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain


@Configuration
@ConditionalOnProperty("cctray-hub.username", "cctray-hub.password")
class BasicAuthWebSecurityConfiguration(
    val basicAuthenticationEntryPoint: BasicAuthenticationEntryPoint,
    @Value("\${cctray-hub.username}") private val username: String,
    @Value("\${cctray-hub.password}") private val password: String
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeRequests()
            .antMatchers("/cctray/**").authenticated()
            .anyRequest().permitAll()
            .and()
            .httpBasic()
            .authenticationEntryPoint(basicAuthenticationEntryPoint)
        return http.build()
    }

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): InMemoryUserDetailsManager {
        val user: UserDetails = User
            .withUsername(username)
            .password(passwordEncoder.encode(password))
            .roles("USER_ROLE")
            .build()
        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}