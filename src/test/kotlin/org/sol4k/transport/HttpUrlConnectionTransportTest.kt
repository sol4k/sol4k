package org.sol4k.transport

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.SocketTimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class HttpUrlConnectionTransportTest {

    @Test
    fun shouldTimeOutWhenServerNeverResponds() {
        ServerSocket(0).use { server ->
            val transport = HttpUrlConnectionTransport(
                connectTimeoutMillis = 1_000,
                readTimeoutMillis = 250,
            )

            assertFailsWith<SocketTimeoutException> {
                transport.post(
                    "http://localhost:${server.localPort}",
                    """{"method":"getHealth"}""",
                    emptyMap(),
                )
            }
        }
    }

    @Test
    fun shouldReturnErrorBodyOnHttpErrorStatus() {
        val errorBody = """{"jsonrpc":"2.0","error":{"code":429,"message":"Too many requests"},"id":1}"""
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/") { exchange ->
            exchange.requestBody.use { it.readBytes() }
            val bytes = errorBody.toByteArray()
            exchange.sendResponseHeaders(429, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        try {
            val transport = HttpUrlConnectionTransport()

            val response = transport.post(
                "http://localhost:${server.address.port}/",
                """{"method":"getHealth"}""",
                emptyMap(),
            )

            assertEquals(errorBody, response)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun shouldReportHttpStatusWhenErrorBodyIsNotJson() {
        val errorBody = "<html><body>502 Bad Gateway</body></html>"
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/") { exchange ->
            exchange.requestBody.use { it.readBytes() }
            val bytes = errorBody.toByteArray()
            exchange.sendResponseHeaders(502, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        try {
            val transport = HttpUrlConnectionTransport()

            val exception = assertFailsWith<IOException> {
                transport.post(
                    "http://localhost:${server.address.port}/",
                    """{"method":"getHealth"}""",
                    emptyMap(),
                )
            }

            assertTrue(exception.message!!.contains("502"))
            assertTrue(exception.message!!.contains(errorBody))
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun shouldRejectNegativeTimeouts() {
        assertFailsWith<IllegalArgumentException> {
            HttpUrlConnectionTransport(connectTimeoutMillis = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            HttpUrlConnectionTransport(readTimeoutMillis = -1)
        }
    }
}
