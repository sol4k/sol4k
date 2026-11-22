package org.sol4k

import org.sol4k.Constants.SIGNATURE_LENGTH
import org.sol4k.exception.SerializationException
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.ByteBuffer
import java.util.Base64
import kotlin.math.max

class VersionedTransaction private constructor(
    val message: TransactionMessage,
    internal val signatures: MutableList<String?>,
) {

    constructor(
        message: TransactionMessage,
    ) : this(message, MutableList(message.header.numRequiredSignatures) { null })

    fun sign(keypair: Keypair) {
        val data = message.serialize()
        val signature = keypair.sign(data)
        addSignature(signature, data)
    }

    fun addSignature(signature: String) {
        val signatureBytes = Base58.decode(signature)
        addSignature(signatureBytes)
    }

    private fun addSignature(signature: ByteArray, serializedMessage: ByteArray? = null) {
        val data = serializedMessage ?: message.serialize()

        for (i in 0 until message.header.numRequiredSignatures) {
            val a = message.accounts[i]
            if (a.verify(signature, data)) {
                signatures[i] = Base58.encode(signature)
                break
            }
        }
    }

    fun serialize(): ByteArray {
        if (signatures.isEmpty() || signatures.filterNotNull().size != message.header.numRequiredSignatures) {
            throw SerializationException("Signature verification failed")
        }

        val messageData = message.serialize()

        val b = ByteArrayOutputStream()
        if (message.version == TransactionMessage.MessageVersion.V1) {
            // v1 puts the message first, then the signatures with no length prefix.
            b.write(messageData)
            for (s in signatures.filterNotNull()) {
                b.write(Base58.decode(s))
            }
            return b.toByteArray()
        }
        b.write(Binary.encodeLength(signatures.size))
        for (s in signatures.filterNotNull()) {
            b.write(Base58.decode(s))
        }
        b.write(messageData)
        return b.toByteArray()
    }

    fun calculateFee(lamportsPerSignature: Int): BigDecimal {
        val sigFee = Convert.lamportToSol(BigDecimal(lamportsPerSignature * max(signatures.size, 1)))
        val accounts = message.accounts
        val data = message.instructions
            .filter { accounts[it.programIdIndex] == Constants.COMPUTE_BUDGET_PROGRAM_ID }
            .map { it.data }
        val msgFee = computeBudget(data)
        return sigFee.add(msgFee).setScale(9, RoundingMode.CEILING)
    }

    companion object {

        @JvmStatic
        fun from(encodedTransaction: String): VersionedTransaction {
            var byteArray = Base64.getDecoder().decode(encodedTransaction)
            if (byteArray.isNotEmpty() && (byteArray.first().toInt() and 0xFF) == TransactionMessage.V1_PREFIX) {
                return fromV1(byteArray)
            }
            val signaturesDecodedLength = Binary.decodeLength(byteArray)
            byteArray = signaturesDecodedLength.bytes
            val signatures = mutableListOf<String>()
            for (i in 0 until signaturesDecodedLength.length) {
                val signature = byteArray.slice(0 until SIGNATURE_LENGTH)
                byteArray = byteArray.drop(SIGNATURE_LENGTH).toByteArray()
                val encodedSignature = Base58.encode(signature.toByteArray())
                signatures.add(encodedSignature)
            }

            val message = TransactionMessage.deserialize(byteArray)
            if (message.version == TransactionMessage.MessageVersion.V1) {
                throw SerializationException("A v1 message must not be preceded by a signature count")
            }

            if (signaturesDecodedLength.length > 0 && message.header.numRequiredSignatures != signaturesDecodedLength.length) {
                throw SerializationException("numRequiredSignatures is not equal to signatureCount")
            }
            return VersionedTransaction(message, signatures.toMutableList())
        }

        private fun fromV1(byteArray: ByteArray): VersionedTransaction {
            val buffer = ByteBuffer.wrap(byteArray)
            val message = TransactionMessage.deserializeV1(buffer)
            val signatureCount = message.header.numRequiredSignatures
            if (buffer.remaining() != signatureCount * SIGNATURE_LENGTH) {
                throw SerializationException("Expected $signatureCount signatures after the v1 message")
            }
            val signatures = MutableList<String?>(signatureCount) {
                Base58.encode(ByteArray(SIGNATURE_LENGTH).also { buffer.get(it) })
            }
            return VersionedTransaction(message, signatures)
        }
    }
}
