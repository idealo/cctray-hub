package name.hennr.cctrayhub.cctray.logging

import ch.qos.logback.access.tomcat.LogbackValve
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty("cctray-hub.access-log")
class ServletContainerLogbackConfig {

	@Bean
	fun servletContainer(): ServletWebServerFactory {
		val tomcat = TomcatServletWebServerFactory()
		tomcat.addContextValves(createValve())
		return tomcat
	}

	private fun createValve(): LogbackValve {
		val valve = LogbackValve()
		valve.isAsyncSupported = true
		return valve
	}

}
