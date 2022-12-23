package name.hennr.cctrayhub.cctray.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.ReactiveSessionRepository
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import java.util.concurrent.ConcurrentHashMap


@Configuration
@EnableSpringWebSession
@ConditionalOnProperty("cctray-hub.username", "cctray-hub.password")
class SessionConfig {

    @Bean
    fun reactiveSessionRepository(): ReactiveSessionRepository<*> {
        return ReactiveMapSessionRepository(ConcurrentHashMap())
    }

}