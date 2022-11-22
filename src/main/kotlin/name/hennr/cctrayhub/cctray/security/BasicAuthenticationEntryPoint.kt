package name.hennr.cctrayhub.cctray.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.PrintWriter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@ConditionalOnProperty("cctray-hub.username", "cctray-hub.password")
class BasicAuthenticationEntryPoint : BasicAuthenticationEntryPoint() {

    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authenticationException: AuthenticationException
    ) {
        response.addHeader("WWW-Authenticate", "Basic realm=" + realmName + "")
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        val writer: PrintWriter = response.writer
        writer.println("HTTP Status 401 - " + authenticationException.message)
    }

    override fun afterPropertiesSet() {
        realmName = "cctray-hub"
        super.afterPropertiesSet()
    }
}