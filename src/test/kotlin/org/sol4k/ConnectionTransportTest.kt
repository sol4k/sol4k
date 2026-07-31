package org.sol4k

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import org.sol4k.api.Commitment
import org.sol4k.api.Health
import org.sol4k.api.SendTransactionParams
import org.sol4k.transport.RpcTransport
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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
    fun shouldSendTransactionWithDefaultParams() {
        val transport = CapturingTransport()
        val connection = Connection("https://example.solana.rpc", transport = transport)

        connection.sendTransaction(byteArrayOf(1, 2, 3))

        val params = Json.parseToJsonElement(transport.body).jsonObject["params"]?.jsonArray
        val encodedTransaction = params?.get(0)?.jsonPrimitive?.content
        val options = params?.get(1)?.jsonObject

        assertEquals(Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3)), encodedTransaction)
        assertEquals("base64", options?.get("encoding")?.jsonPrimitive?.content)
        assertFalse(options!!.containsKey("skipPreflight"))
        assertFalse(options.containsKey("preflightCommitment"))
        assertFalse(options.containsKey("maxRetries"))
        assertFalse(options.containsKey("minContextSlot"))
    }

    @Test
    fun shouldSendTransactionWithExplicitDefaultParamsAndStillEncodeEncoding() {
        val transport = CapturingTransport()
        val connection = Connection("https://example.solana.rpc", transport = transport)

        connection.sendTransaction(byteArrayOf(1, 2, 3), SendTransactionParams())

        val params = Json.parseToJsonElement(transport.body).jsonObject["params"]?.jsonArray
        val options = params?.get(1)?.jsonObject

        assertEquals("base64", options?.get("encoding")?.jsonPrimitive?.content)
    }

    @Test
    fun shouldSendTransactionWithCustomParams() {
        val transport = CapturingTransport()
        val connection = Connection("https://example.solana.rpc", transport = transport)

        connection.sendTransaction(
            byteArrayOf(1, 2, 3),
            SendTransactionParams(
                skipPreflight = true,
                preflightCommitment = Commitment.CONFIRMED,
                maxRetries = 5,
                minContextSlot = 100,
            ),
        )

        val params = Json.parseToJsonElement(transport.body).jsonObject["params"]?.jsonArray
        val options = params?.get(1)?.jsonObject

        assertEquals("base64", options?.get("encoding")?.jsonPrimitive?.content)
        assertEquals(true, options?.get("skipPreflight")?.jsonPrimitive?.boolean)
        assertEquals("confirmed", options?.get("preflightCommitment")?.jsonPrimitive?.content)
        assertEquals(5, options?.get("maxRetries")?.jsonPrimitive?.int)
        assertEquals(100, options?.get("minContextSlot")?.jsonPrimitive?.int)
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
