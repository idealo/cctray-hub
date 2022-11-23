package name.hennr.cctrayhub.cctray.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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