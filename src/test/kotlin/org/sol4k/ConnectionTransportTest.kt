package org.sol4k

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import org.sol4k.api.Health
import org.sol4k.exception.RpcException
import org.sol4k.transport.RpcTransport
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ConnectionTransportTest {

    @Test
    fun shouldUseInjectedTransportAndForwardHeaders() {
        val transport = CapturingTransport()
        val headers = mapOf(
            "x-api-key" to "test-api-key",
            "x-client" to "sol4k-test",
        )
        val connection = Connection(
            "https://example.solana.rpc",
            transport = transport,
            headers = headers,
        )

        val health = connection.getHealth()

        assertEquals(Health.OK, health)
        assertEquals(1, transport.callCount)
        assertEquals("https://example.solana.rpc", transport.url)
        assertEquals(headers, transport.headers)

        val request = Json.parseToJsonElement(transport.body).jsonObject
        assertEquals("getHealth", request["method"]?.jsonPrimitive?.content)
        assertEquals("2.0", request["jsonrpc"]?.jsonPrimitive?.content)
        assertEquals(0, request["params"]?.jsonArray?.size)
    }

    @Test
    fun shouldUseInjectedTransportWithRpcUrlConstructor() {
        val transport = CapturingTransport()
        val headers = mapOf("Authorization" to "Bearer test-token")
        val connection = Connection(
            RpcUrl.DEVNET,
            transport = transport,
            headers = headers,
        )

        val health = connection.getHealth()

        assertEquals(Health.OK, health)
        assertEquals(1, transport.callCount)
        assertEquals(RpcUrl.DEVNET.value, transport.url)
        assertEquals(headers, transport.headers)
    }

    @Test
    fun shouldThrowRpcExceptionWhenNodeReturnsError() {
        val errorBody = """{"jsonrpc":"2.0","error":{"code":-32602,"message":"Invalid params"},"id":1}"""
        val connection = Connection(
            "https://example.solana.rpc",
            transport = StubTransport(errorBody),
        )

        val exception = assertFailsWith<RpcException> {
            connection.getHealth()
        }

        assertEquals(-32602, exception.code)
        assertEquals("Invalid params", exception.message)
        assertEquals(errorBody, exception.rawResponse)
    }

    @Test
    fun shouldThrowRpcExceptionWhenResponseIsNotJsonRpc() {
        val htmlBody = "<html><body>502 Bad Gateway</body></html>"
        val connection = Connection(
            "https://example.solana.rpc",
            transport = StubTransport(htmlBody),
        )

        val exception = assertFailsWith<RpcException> {
            connection.getHealth()
        }

        assertEquals(htmlBody, exception.rawResponse)
    }

    private class StubTransport(private val response: String) : RpcTransport {
        override fun post(
            url: String,
            body: String,
            headers: Map<String, String>,
        ): String = response
    }

    private class CapturingTransport : RpcTransport {
        lateinit var url: String
        lateinit var body: String
        lateinit var headers: Map<String, String>
        var callCount = 0

        override fun post(
            url: String,
            body: String,
            headers: Map<String, String>,
        ): String {
            this.url = url
            this.body = body
            this.headers = headers
            callCount++

            return """
                {"result":"ok","id":1,"jsonrpc":"2.0"}
            """.trimIndent()
        }
    }
}
