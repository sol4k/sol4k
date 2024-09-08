package org.sol4k

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.sol4k.api.AccountInfo
import org.sol4k.api.Blockhash
import org.sol4k.api.Commitment
import org.sol4k.api.Commitment.FINALIZED
import org.sol4k.api.EpochInfo
import org.sol4k.api.Health
import org.sol4k.api.IsBlockhashValidResult
import org.sol4k.api.TokenAccountBalance
import org.sol4k.api.TransactionSimulation
import org.sol4k.api.TransactionSimulationError
import org.sol4k.api.TransactionSimulationSuccess
import org.sol4k.exception.RpcException
import org.sol4k.rpc.Balance
import org.sol4k.rpc.BlockhashResponse
import org.sol4k.rpc.EpochInfoResult
import org.sol4k.rpc.GetAccountInfoResponse
import org.sol4k.rpc.GetTokenApplyResponse
import org.sol4k.rpc.Identity
import org.sol4k.rpc.RpcErrorResponse
import org.sol4k.rpc.RpcRequest
import org.sol4k.rpc.RpcResponse
import org.sol4k.rpc.SimulateTransactionResponse
import org.sol4k.rpc.TokenAmount
import org.sol4k.rpc.TokenBalanceResult
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

class Connection @JvmOverloads constructor(
    private val rpcUrl: String,
    private val commitment: Commitment = FINALIZED,
) {
    @JvmOverloads
    constructor(
        rpcUrl: RpcUrl,
        commitment: Commitment = FINALIZED,
    ) : this(rpcUrl.value, commitment)

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun getBalance(walletAddress: PublicKey, optional: Map<String, Any>  = mapOf()): BigInteger {
        val balance: Balance = rpcCall("getBalance",
            listOf(
                Json.encodeToJsonElement(walletAddress.toBase58()),
                Json.encodeToJsonElement(mapToJsonElement(optional)))
            )
        return balance.value
    }

    @JvmOverloads
    fun getTokenAccountBalance(
        accountAddress: PublicKey,
        optional: Map<String, Any>  = mapOf("commitment" to commitment.toString()),
    ): TokenAccountBalance {
        val result: TokenBalanceResult = rpcCall(
            "getTokenAccountBalance",
            listOf(
                Json.encodeToJsonElement(accountAddress.toBase58()),
                Json.encodeToJsonElement(mapToJsonElement(optional))
            ),
        )
        val (amount, decimals, uiAmountString) = result.value
        return TokenAccountBalance(
            amount = BigInteger(amount),
            decimals = decimals,
            uiAmount = uiAmountString,
        )
    }

    @JvmOverloads
    fun getLatestBlockhash(optional: Map<String, Any>  = mapOf("commitment" to commitment.toString())): String =
        this.getLatestBlockhashExtended(optional).blockhash

    @JvmOverloads
    fun getLatestBlockhashExtended(optional: Map<String, Any>  = mapOf("commitment" to commitment.toString())): Blockhash {
        val result: BlockhashResponse = rpcCall(
            "getLatestBlockhash",
            listOf(Json.encodeToJsonElement(mapToJsonElement(optional)))
        )
        return Blockhash(
            blockhash = result.value.blockhash,
            slot = result.context.slot,
            lastValidBlockHeight = result.value.lastValidBlockHeight,
        )
    }

    @JvmOverloads
    fun isBlockhashValid(blockhash: String, optional: Map<String, Any>  = mapOf("commitment" to commitment.toString())): Boolean {
        val result: IsBlockhashValidResult = rpcCall(
            "isBlockhashValid",
            listOf(
                Json.encodeToJsonElement(blockhash),
                Json.encodeToJsonElement(mapToJsonElement(optional))
            ),
        )
        return result.value
    }

    fun getHealth(): Health {
        val result: String = rpcCall("getHealth", listOf<String>())
        return if (result == "ok") Health.OK else Health.ERROR
    }

    fun getEpochInfo(optional: Map<String, Any>  = mapOf()): EpochInfo {
        val result: EpochInfoResult = rpcCall("getEpochInfo", listOf(Json.encodeToJsonElement(mapToJsonElement(optional))))
        return EpochInfo(
            absoluteSlot = result.absoluteSlot,
            blockHeight = result.blockHeight,
            epoch = result.epoch,
            slotIndex = result.slotIndex,
            slotsInEpoch = result.slotsInEpoch,
            transactionCount = result.transactionCount,
        )
    }

    fun getIdentity(): PublicKey {
        val (identity) = rpcCall<Identity, String>("getIdentity", listOf())
        return PublicKey(identity)
    }

    fun getTransactionCount(optional: Map<String, Any>  = mapOf()): Long = rpcCall<Long, JsonElement>("getTransactionCount",
        listOf(Json.encodeToJsonElement(mapToJsonElement(optional)))
    )

    fun getAccountInfo(accountAddress: PublicKey, optional: Map<String, Any>  = mapOf("encoding" to "base64")): AccountInfo? {
        val (value) = rpcCall<GetAccountInfoResponse, JsonElement>(
            "getAccountInfo",
            listOf(
                Json.encodeToJsonElement(accountAddress.toBase58()),
                Json.encodeToJsonElement(mapToJsonElement(optional)),
            ),
        )
        return value?.let {
            val data = Base64.getDecoder().decode(value.data[0])
            AccountInfo(
                data,
                executable = value.executable,
                lamports = value.lamports,
                owner = PublicKey(value.owner),
                rentEpoch = value.rentEpoch,
                space = value.space ?: data.size,
            )
        }
    }

    fun getMinimumBalanceForRentExemption(space: Int, optional: Map<String, Any> = mapOf()): Long {
        return rpcCall(
            "getMinimumBalanceForRentExemption",
            listOf(
                Json.encodeToJsonElement(space),
                Json.encodeToJsonElement(mapToJsonElement(optional)))
        )
    }

    fun getTokenSupply(tokenPubkey: String, optional: Map<String, Any> = mapOf()): TokenAmount {
        return rpcCall<GetTokenApplyResponse, JsonElement>("getTokenSupply",
            listOf(
                Json.encodeToJsonElement(tokenPubkey),
                Json.encodeToJsonElement(mapToJsonElement(optional))
            )).value
    }

    fun requestAirdrop(accountAddress: PublicKey, amount: Long, optional: Map<String, Any> = mapOf()): String {
        return rpcCall(
            "requestAirdrop",
            listOf(
                Json.encodeToJsonElement(accountAddress.toBase58()),
                Json.encodeToJsonElement(amount),
                Json.encodeToJsonElement(mapToJsonElement(optional))
            ),
        )
    }

    fun sendTransaction(transactionBytes: ByteArray, optional: Map<String, Any>  = mapOf("encoding" to "base64")): String {
        val encodedTransaction = Base64.getEncoder().encodeToString(transactionBytes)
        return rpcCall(
            "sendTransaction",
            listOf(
                Json.encodeToJsonElement(encodedTransaction),
                Json.encodeToJsonElement(mapToJsonElement(optional))
            )
        )
    }

    fun sendTransaction(transaction: Transaction, optional: Map<String, Any> = mapOf("encoding" to "base64")): String {
        return sendTransaction(transaction.serialize(), optional)
    }

    fun simulateTransaction(transactionBytes: ByteArray, optional: Map<String, Any>  = mapOf("encoding" to "base64")): TransactionSimulation {
        val encodedTransaction = Base64.getEncoder().encodeToString(transactionBytes)
        val result: SimulateTransactionResponse = rpcCall(
            "simulateTransaction",
            listOf(
                Json.encodeToJsonElement(encodedTransaction),
                Json.encodeToJsonElement(mapToJsonElement(optional))
            )
        )
        val (err, logs) = result.value
        if (err != null) {
            when (err) {
                is JsonPrimitive -> return TransactionSimulationError(err.content)
                else -> throw IllegalArgumentException("Failed to parse the error")
            }
        } else if (logs != null) {
            return TransactionSimulationSuccess(logs)
        }
        throw IllegalArgumentException("Unable to parse simulation response")
    }

    fun simulateTransaction(transaction: Transaction, optional: Map<String, Any>  = mapOf("encoding" to "base64")): TransactionSimulation {
        return simulateTransaction(transaction.serialize(), optional)
    }

    private fun mapToJsonElement(map: Map<String, Any>): JsonElement {
        return buildJsonObject {
            for ((key, value) in map) {
                when (value) {
                    is String -> put(key, value)
                    is Number -> put(key, value)
                    is Boolean -> put(key, value)
                    is Map<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        put(key, mapToJsonElement(value as Map<String, Any>))
                    }
                    else -> put(key, Json.encodeToJsonElement(value)) // Универсальная обработка любого объекта
                }
            }
        }
    }
    private inline fun <reified T, reified I : Any> rpcCall(method: String, params: List<I>): T {
        val connection = URL(rpcUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.outputStream.use {
            val body = Json.encodeToString(
                RpcRequest(method, params)
            )
            it.write(body.toByteArray())
        }
        val responseBody = connection.inputStream.use {
            BufferedReader(InputStreamReader(it)).use { reader ->
                reader.readText()
            }
        }
        connection.disconnect()
        try {
            val (result) = jsonParser.decodeFromString<RpcResponse<T>>(responseBody)
            return result
        } catch (_: SerializationException) {
            val (error) = jsonParser.decodeFromString<RpcErrorResponse>(responseBody)
            throw RpcException(error.code, error.message, responseBody)
        }
    }
}
