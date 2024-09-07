package org.sol4k

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun info(message: String) {
        try {
            val stackTrace = Thread.currentThread().stackTrace
            val caller = stackTrace[2]
            val simpleClassName = caller.className.substring(caller.className.lastIndexOf('.') + 1)
            val methodName = caller.methodName
            val timestamp = LocalDateTime.now().format(formatter)
            System.out.printf("[%s] %s.%s: %s%n", timestamp, simpleClassName, methodName, message)
        } catch (_: Exception) {}
    }
}
