package org.sol4k

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun info(message: String) {
        try {
            val caller = Thread.currentThread().stackTrace[2]
            val className = caller.className.substring(caller.className.lastIndexOf('.') + 1)
            val timestamp = LocalDateTime.now().format(formatter)
            System.out.printf("[%s] %s.%s: %s%n", timestamp, className, caller.methodName, message)
        } catch (_: Exception) {}
    }
}
