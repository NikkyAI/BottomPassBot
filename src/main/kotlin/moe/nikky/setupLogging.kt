package moe.nikky

import io.klogging.Level
import io.klogging.config.LoggingConfig
import io.klogging.config.loggingConfiguration
import io.klogging.rendering.RENDER_ANSI
import io.klogging.rendering.RENDER_SIMPLE
import io.klogging.sending.STDOUT
import io.klogging.sending.SendString
import io.ktor.util.cio.*
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun setupLogging() {
    loggingConfiguration {
            sink("stdout", RENDER_ANSI, STDOUT)
        sink("file_latest", RENDER_SIMPLE, logFile(File("logs/latest.log")))
        sink("file_latest_trace", RENDER_SIMPLE, logFile(File("logs/latest-trace.log")))
        val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Date())
        sink("file", RENDER_SIMPLE, logFile(File("logs/log-$timestamp.log")))

        fun LoggingConfig.applyFromMinLevel(level: Level) {
            fromMinLevel(level) {
                toSink("stdout")
                toSink("file_latest")
                toSink("file")
            }
        }
        logging {
            fromLoggerBase("moe.nikky", stopOnMatch = true)
            applyFromMinLevel(Level.DEBUG)
            fromMinLevel(Level.TRACE) {
                toSink("file_latest_trace")
            }
        }
        logging {
            fromLoggerBase("dev.kord.rest", stopOnMatch = true)
            applyFromMinLevel(Level.INFO)
        }
        logging {
            //TODO: fix logger matcher
            exactLogger("\\Q[R]:[KTOR]:[ExclusionRequestRateLimiter]\\E", stopOnMatch = true)
            applyFromMinLevel(Level.INFO)
        }
        logging {
            fromLoggerBase("dev.kord", stopOnMatch = true)
            applyFromMinLevel(Level.INFO)
        }
        logging {
            fromLoggerBase("com.kotlindiscord.kord.extensions", stopOnMatch = true)
            applyFromMinLevel(Level.INFO)
        }
        logging {
            applyFromMinLevel(Level.INFO)
        }
    }
}

fun logFile(file: File, append: Boolean = false): SendString {
    file.parentFile.mkdirs()
    if(!append && file.exists()) {
        file.delete()
        file.createNewFile()
    } else if(!file.exists()) {
        file.createNewFile()
    }
    val writeChannel = file.writeChannel()
    val writer = writeChannel.bufferedWriter()
    return { line ->
        writer.write(line)
        writer.newLine()
//        writeChannel.writeStringUtf8(line + "\n")
        delay(1)
    }
}