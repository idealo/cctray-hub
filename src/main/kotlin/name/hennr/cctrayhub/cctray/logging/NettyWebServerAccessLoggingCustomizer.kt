package name.hennr.cctrayhub.cctray.logging

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.netty.http.server.HttpServer
import reactor.netty.http.server.logging.AccessLog
import reactor.netty.http.server.logging.AccessLogArgProvider
import java.net.InetSocketAddress
import java.time.format.DateTimeFormatter

@Component
@Profile("json-log-format")
class NettyWebServerAccessLoggingCustomizer: WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
    override fun customize(factory: NettyReactiveWebServerFactory) {
        factory.addServerCustomizers(NettyServerCustomizer { httpServer: HttpServer ->
            httpServer.accessLog(true) { logArguments: AccessLogArgProvider ->
                val remoteHost = (logArguments.remoteAddress() as? InetSocketAddress)?.address ?: InetSocketAddress("unknown", 6666)
                AccessLog.create("remoteHost={}, method={}, size={}, code={}, uri={}, duration={}, timestamp={}, host={}, referer={}, user-agent={}, forwarded={}",
                    remoteHost,
                    logArguments.method(),
                    logArguments.contentLength(),
                    logArguments.status(),
                    logArguments.uri(),
                    logArguments.duration(),
                    logArguments.accessDateTime()?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    logArguments.requestHeader("host"),
                    logArguments.requestHeader("referer"),
                    logArguments.requestHeader("user-agent"),
                    logArguments.requestHeader("Forwarded")
                )
            }
        })
    }
}
