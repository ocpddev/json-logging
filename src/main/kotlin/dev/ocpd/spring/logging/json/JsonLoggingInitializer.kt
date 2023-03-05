package dev.ocpd.spring.logging.json

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import net.logstash.logback.composite.loggingevent.*
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.getProperty
import java.time.ZoneId

private const val CONSOLE_APPENDER_NAME = "CONSOLE"

class JsonLoggingInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationCtx: ConfigurableApplicationContext) {
        val env = applicationCtx.environment
        val enabled = env.getProperty<Boolean>("logging.json.enabled") ?: false
        if (!enabled) return
        val loggerCtx = LoggerFactory.getILoggerFactory() as LoggerContext

        val appender = ConsoleAppender<ILoggingEvent>().apply {
            context = loggerCtx
            encoder = LoggingEventCompositeJsonEncoder().apply {
                context = loggerCtx
                providers = LoggingEventJsonProviders().apply {
                    context = loggerCtx
                    addTimestamp(timestampJsonProvider())
                    addLogLevel(LogLevelJsonProvider())
                    addThreadName(LoggingEventThreadNameJsonProvider())
                    addLoggerName(LoggerNameJsonProvider())
                    addLogstashMarkers(LogstashMarkersJsonProvider())
                    addMdc(MdcJsonProvider())
                    addMessage(MessageJsonProvider())
                    addStackTrace(StackTraceJsonProvider())
                }
                start()
            }
            name = CONSOLE_APPENDER_NAME
            start()
        }
        with(loggerCtx.getLogger(Logger.ROOT_LOGGER_NAME)) {
            detachAppender(CONSOLE_APPENDER_NAME)
            addAppender(appender)
        }
    }

    private fun timestampJsonProvider() =
        LoggingEventFormattedTimestampJsonProvider().apply {
            timeZone = ZoneId.systemDefault().id
            fieldName = "timestamp"
        }
}
