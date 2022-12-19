package name.hennr.cctrayhub.cctray.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import com.fasterxml.jackson.core.JsonGenerator
import net.logstash.logback.composite.AbstractJsonProvider

class NettyAccessLogEventJsonProvider : AbstractJsonProvider<ILoggingEvent>() {

    private companion object {
        private const val NETTY_ACCESS_LOGGER_NAME = "reactor.netty.http.server.AccessLog"
    }

    override fun writeTo(generator: JsonGenerator, event: ILoggingEvent) {
        if (event.loggerName == NETTY_ACCESS_LOGGER_NAME) {
            val (remoteHost, method, contentLength, statusCode, uri) = event.argumentArray.slice(IntRange(0, 5))
            val (requestDuration, _, host, referer, useragent) = event.argumentArray.slice(IntRange(5, 10))
            val forwarded = event.argumentArray[10]

            generator.writeObjectField("ip", remoteHost)
            generator.writeObjectField("method", method)
            generator.writeObjectField("size", contentLength.toString())
            generator.writeObjectField("code", statusCode.toString())
            generator.writeObjectField("@fields.requested_uri", uri)
            generator.writeObjectField("@fields.elapsed_time", requestDuration)
            generator.writeObjectField("requested_host", host)
            generator.writeObjectField("referer", referer)
            generator.writeObjectField("useragent", useragent)
            generator.writeObjectField("Forwarded", forwarded)
        }
    }

}
