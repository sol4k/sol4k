package org.sol4k

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.sol4k.api.AccountInfo
import org.sol4k.api.Blockhash
import org.sol4k.api.Commitment
import org.sol4k.api.Commitment.FINALIZED
import org.sol4k.exception.RpcException
import org.sol4k.rpc.Balance
import org.sol4k.rpc.BlockhashResponse
import org.sol4k.rpc.GetAccountInfoResponse
import org.sol4k.rpc.RpcErrorResponse
import org.sol4k.rpc.RpcRequest
import org.sol4k.rpc.RpcResponse
import java.lang.IllegalStateException
import java.math.BigInteger
import java.util.Base64

class Connection @JvmOverloads constructor(
    private val rpcUrl: String,
    private val commitment: Commitment = FINALIZED,
) {
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

    @JvmOverloads
    fun getLatestBlockhash(commitment: Commitment = this.commitment): Blockhash {
        val request = request("getLatestBlockhash", listOf(mapOf("commitment" to commitment)))
        val result = rpcCall<BlockhashResponse>(request)
        return Blockhash(
            blockhash = result.value.blockhash,
            slot = result.context.slot,
            lastValidBlockHeight = result.value.lastValidBlockHeight,
        )
    }

    fun getAccountInfo(accountAddress: PublicKey): AccountInfo? {
        val request = request(
            "getAccountInfo",
            listOf(
                Json.encodeToJsonElement(accountAddress.toBase58()),
                Json.encodeToJsonElement(mapOf("encoding" to "base58")),
            )
        )
        val (value) = rpcCall<GetAccountInfoResponse>(request)
        return value?.let {
            AccountInfo(
                data = Base58.decode(value.data[0]),
                executable = value.executable,
                lamports = value.lamports,
                owner = PublicKey(value.owner),
                rentEpoch = value.rentEpoch,
                space = value.space,
            )
        }
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
        val responseBody = client.newCall(request).execute().use { response ->
            val body = response.body ?: throw IllegalStateException("Received empty response from the RPC node")
            body.string()
        }
        try {
            val (result) = jsonParser.decodeFromString<RpcResponse<T>>(responseBody)
            return result
        } catch (_: SerializationException) {
            val (error) = jsonParser.decodeFromString<RpcErrorResponse>(responseBody)
            throw RpcException(error.code, error.message, responseBody)
        }
    }

    private inline fun <reified T : Any> request(method: String, params: List<T>): Request =
        Request.Builder().url(rpcUrl).post(
            Json.encodeToString(
                RpcRequest(method, params)
            ).toRequestBody(contentType),
        ).build()
}
