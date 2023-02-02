package org.sol4k

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.sol4k.api.Blockhash
import org.sol4k.rpc.BlockhashResponse
import org.sol4k.rpc.RpcResponse
import java.util.*

class Connection(private val rpcUrl: String) {
    private val client = OkHttpClient()
    private val json = "application/json".toMediaType()

    fun getBalance(walletAddress: String): String {
        val request = Request.Builder()
            .url(rpcUrl)
            .post(
                """
                    {
                        "jsonrpc": "2.0",
                        "id": 1,
                        "method": "getBalance",
                        "params": [ "$walletAddress" ]
                      }
                """.toRequestBody(json)
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
                """{
                    "id":1,
                    "jsonrpc":"2.0",
                    "method":"getLatestBlockhash",
                    "params":[ { "commitment":"finalized" } ]
                }""".toRequestBody(json)
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
        val base64Trx = Base64.getEncoder().encodeToString(transaction.serialize())
        val request = Request.Builder()
            .url(rpcUrl)
            .post(
                """{
                    "id":1,
                    "jsonrpc":"2.0",
                    "method":"sendTransaction",
                    "params":["$base64Trx",{"encoding":"base64"}]
                }""".toRequestBody(json)
            )
            .build()
        val result = client.newCall(request).execute().use { response ->
            response.body!!.string()
        }
        return Json.decodeFromString<RpcResponse<String>>(result).result
    }
}