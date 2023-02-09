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
import org.sol4k.rpc.BlockhashResponse
import org.sol4k.rpc.RpcRequest
import org.sol4k.rpc.RpcResponse
import java.util.Base64

class Connection(private val rpcUrl: String) {
    private val client = OkHttpClient()
    private val json = "application/json".toMediaType()

    fun getBalance(walletAddress: String): String {
        val request = Request.Builder()
            .url(rpcUrl)
            .post(
                Json.encodeToString(
                    RpcRequest("getBalance", listOf(walletAddress)),
                ).toRequestBody(json),
            )
            .build()

        return client.newCall(request).execute().use { response ->
            response.body!!.string()
        }
    }

    fun getLatestBlockhash(): Blockhash {
        val request = Request.Builder()
            .url(rpcUrl)
            .post(
                Json.encodeToString(
                    RpcRequest(
                        "getLatestBlockhash",
                        listOf(mapOf("commitment" to "finalized")),
                    ),
                ).toRequestBody(json),
            )
            .build()
        val response = client.newCall(request).execute().use { response ->
            response.body!!.string()
        }
        val (_, result) = Json.decodeFromString<RpcResponse<BlockhashResponse>>(response)
        return Blockhash(
            blockhash = result.value.blockhash,
            slot = result.context.slot,
            lastValidBlockHeight = result.value.lastValidBlockHeight,
        )
    }

    fun sendTransaction(transaction: Transaction): String {
        val encodedTransaction = Base64.getEncoder().encodeToString(transaction.serialize())
        val request = Request.Builder()
            .url(rpcUrl)
            .post(
                Json.encodeToString(
                    RpcRequest(
                        "sendTransaction",
                        listOf(
                            Json.encodeToJsonElement(encodedTransaction),
                            Json.encodeToJsonElement(mapOf("encoding" to "base64")),
                        ),
                    ),
                ).toRequestBody(json)
            )
            .build()
        val result = client.newCall(request).execute().use { response ->
            response.body!!.string()
        }
        return Json.decodeFromString<RpcResponse<String>>(result).result
    }
}
