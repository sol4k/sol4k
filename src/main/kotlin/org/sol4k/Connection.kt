package org.sol4k

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.sol4k.api.Blockhash
import org.sol4k.rpc.Balance
import org.sol4k.rpc.BlockhashResponse
import org.sol4k.rpc.RpcRequest
import org.sol4k.rpc.RpcResponse
import java.math.BigInteger
import java.util.Base64

class Connection(private val rpcUrl: String) {
    private val client = OkHttpClient()
    private val contentType = "application/json".toMediaType()
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun getBalance(walletAddress: PublicKey): BigInteger {
        val request = request("getBalance", listOf(walletAddress.toBase58()))
        return rpcCall<Balance>(request).value
    }

    fun getLatestBlockhash(): Blockhash {
        val request = request("getLatestBlockhash", listOf(mapOf("commitment" to "finalized")))
        val result = rpcCall<BlockhashResponse>(request)
        return Blockhash(
            blockhash = result.value.blockhash,
            slot = result.context.slot,
            lastValidBlockHeight = result.value.lastValidBlockHeight,
        )
    }

    fun sendTransaction(transaction: Transaction): String {
        val encodedTransaction = Base64.getEncoder().encodeToString(transaction.serialize())
        val request = request(
            "sendTransaction",
            listOf(
                Json.encodeToJsonElement(encodedTransaction),
                Json.encodeToJsonElement(mapOf("encoding" to "base64")),
            )
        )
        return rpcCall(request)
    }

    private inline fun <reified T> rpcCall(request: Request): T {
        val response = client.newCall(request).execute().use { response ->
            val body = response.body ?: throw IllegalArgumentException("Received empty response from the RPC node")
            body.string()
        }
        val (_, result) = jsonParser.decodeFromString<RpcResponse<T>>(response)
        return result
    }

    private inline fun <reified T : Any> request(method: String, params: List<T>): Request =
        Request.Builder().url(rpcUrl).post(
            Json.encodeToString(
                RpcRequest(method, params)
            ).toRequestBody(contentType),
        ).build()
}
