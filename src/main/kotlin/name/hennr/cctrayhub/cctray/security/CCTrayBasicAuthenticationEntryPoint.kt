package name.hennr.cctrayhub.cctray.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("cctray-hub.username", "cctray-hub.password")
class CCTrayBasicAuthenticationEntryPoint : BasicAuthenticationEntryPoint() {

    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authenticationException: AuthenticationException
    ) {
        super.commence(request, response, authenticationException)
    }

    override fun afterPropertiesSet() {
        realmName = "cctray-hub"
        super.afterPropertiesSet()
    }
}
