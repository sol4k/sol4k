package org.sol4k

import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.net.SocketTimeoutException
import kotlin.test.assertFailsWith

internal class ConnectionTest {

    @Test
    fun shouldTimeOutWhenServerNeverResponds() {
        // A server socket that accepts connections but never writes a response,
        // so the read blocks until readTimeout elapses.
        ServerSocket(0).use { server ->
            val rpcUrl = "http://127.0.0.1:${server.localPort}"
            val connection = Connection(rpcUrl, readTimeout = 100)

            assertFailsWith<SocketTimeoutException> {
                connection.getHealth()
            }
        }
    }
}
