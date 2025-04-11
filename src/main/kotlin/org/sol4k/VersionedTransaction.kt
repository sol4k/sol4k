package org.sol4k

import org.sol4k.Constants.SIGNATURE_LENGTH
import org.sol4k.exception.SerializationException
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Base64
import kotlin.math.max

class VersionedTransaction private constructor(
    val message: TransactionMessage,
    internal val signatures: MutableList<String?>,
) {

    constructor(
        message: TransactionMessage,
    ) : this(message, MutableList(message.header.numRequireSignatures) { null })

    fun sign(keypair: Keypair) {
        val data = message.serialize()
        val signature = keypair.sign(data)
        sign(signature, data)
    }

    fun sign(signature: ByteArray, serializedMessage: ByteArray? = null) {
        val data = serializedMessage ?: message.serialize()

        for (i in 0 until message.header.numRequireSignatures) {
            val a = message.accounts[i]
            if (a.verify(signature, data)) {
                signatures[i] = Base58.encode(signature)
                break
            }
        }
    }

    fun serialize(): ByteArray {
        if (signatures.isEmpty() || signatures.filterNotNull().size != message.header.numRequireSignatures) {
            throw SerializationException("Signature verification failed")
        }

        val messageData = message.serialize()

        val b = ByteArrayOutputStream()
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
            .filter { accounts[it.programIdIndex] != Constants.COMPUTE_BUDGET_PROGRAM_ID }
            .map { it.data }
        val msgFee = computeBudget(data)
        return sigFee.add(msgFee).setScale(9, RoundingMode.CEILING)
    }

    companion object {

        @JvmStatic
        fun from(encodedTransaction: String): VersionedTransaction {
            var byteArray = Base64.getDecoder().decode(encodedTransaction)
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

            if (signaturesDecodedLength.length > 0 && message.header.numRequireSignatures != signaturesDecodedLength.length) {
                throw SerializationException("numRequireSignatures is not equal to signatureCount")
            }
            return VersionedTransaction(message, signatures.toMutableList())
        }
    }
}
