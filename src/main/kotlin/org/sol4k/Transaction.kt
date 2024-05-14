package org.sol4k

import org.sol4k.instruction.BaseInstruction
import org.sol4k.instruction.Instruction
import java.nio.ByteBuffer
import java.util.Base64

class Transaction(
    private val recentBlockhash: String,
    private val instructions: List<Instruction>,
    private val feePayer: PublicKey,
) {
    constructor(
        recentBlockhash: String,
        instruction: Instruction,
        feePayer: PublicKey,
    ) : this(recentBlockhash, listOf(instruction), feePayer)

    private val signatures: MutableList<String> = mutableListOf()

    fun sign(keypair: Keypair) {
        val message = transactionMessage()
        val signature = keypair.sign(message)
        signatures.add(Base58.encode(signature))
    }

    private fun addSignature(signature: String) {
        signatures.add(signature)
    }

    private fun transactionMessage(): ByteArray {
        val accountKeys = buildAccountKeys()
        val transactionAccountPublicKeys = accountKeys.map { it.publicKey }
        val accountAddressesLength = Binary.encodeLength(accountKeys.size)
        val instructionBytes =
            instructions.map { instruction ->
                val keyIndices =
                    ByteArray(instruction.keys.size) {
                        transactionAccountPublicKeys.indexOf(instruction.keys[it].publicKey).toByte()
                    }
                byteArrayOf(transactionAccountPublicKeys.indexOf(instruction.programId).toByte()) +
                    Binary.encodeLength(instruction.keys.size) +
                    keyIndices +
                    Binary.encodeLength(instruction.data.size) +
                    instruction.data
            }
        val instructionsLength = Binary.encodeLength(instructions.size)
        val bufferSize =
            HEADER_LENGTH +
                RECENT_BLOCK_HASH_LENGTH +
                accountAddressesLength.size +
                (accountKeys.size * PUBLIC_KEY_LENGTH) +
                instructionsLength.size +
                instructionBytes.sumOf { it.size }
        val buffer = ByteBuffer.allocate(bufferSize)
        val numRequiredSignatures = accountKeys.count { it.signer }.toByte()
        val numReadonlySignedAccounts = accountKeys.count { it.signer && !it.writable }.toByte()
        val numReadonlyUnsignedAccounts = accountKeys.count { !it.signer && !it.writable }.toByte()
        buffer.put(byteArrayOf(numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts))
        buffer.put(accountAddressesLength)
        accountKeys.forEach { accountMeta ->
            buffer.put(accountMeta.publicKey.bytes())
        }
        buffer.put(Base58.decode(recentBlockhash))
        buffer.put(instructionsLength)
        instructionBytes.forEach { buffer.put(it) }
        return buffer.array()
    }

    fun serialize(): ByteArray {
        val signaturesLength = Binary.encodeLength(signatures.size)
        val message = this.transactionMessage()
        val buffer =
            ByteBuffer.allocate(
                signaturesLength.size + signatures.size * SIGNATURE_LENGTH + message.size,
            )
        buffer.put(signaturesLength)
        signatures.forEach { signature ->
            buffer.put(Base58.decode(signature))
        }
        buffer.put(message)
        return buffer.array()
    }

    private fun buildAccountKeys(): List<AccountMeta> {
        val programIds =
            instructions
                .map { it.programId }.toSet()
        val baseAccountKeys =
            instructions
                .asSequence()
                .flatMap { it.keys }
                .filter { acc -> acc.publicKey != this.feePayer }
                .filter { acc -> acc.publicKey !in programIds }
                .distinctBy { it.publicKey }
                .sortedWith(compareBy({ it.signer }, { it.signer && !it.writable }, { !it.signer && !it.writable }))
                .toList()
        val programIdKeys =
            programIds
                .map { AccountMeta(it, writable = false, signer = false) }
        val feePayerList = listOf(AccountMeta(feePayer, writable = true, signer = true))
        return feePayerList + baseAccountKeys + programIdKeys
    }

    companion object {
        private const val HEADER_LENGTH = 3
        private const val RECENT_BLOCK_HASH_LENGTH = 32
        private const val PUBLIC_KEY_LENGTH = 32
        private const val SIGNATURE_LENGTH = 64

        @JvmStatic
        fun from(encodedTransaction: String): Transaction {
            var byteArray = Base64.getDecoder().decode(encodedTransaction)

            // 1. remove signatures
            val signaturesDecodedLength = Binary.decodeLength(byteArray)
            byteArray = signaturesDecodedLength.bytes
            val signatures = mutableListOf<String>()
            for (i in 0 until signaturesDecodedLength.length) {
                val signature = byteArray.slice(0 until SIGNATURE_LENGTH)
                byteArray = byteArray.drop(SIGNATURE_LENGTH).toByteArray()
                val encodedSignature = Base58.encode(signature.toByteArray())
                val zeroSignature = Base58.encode(ByteArray(SIGNATURE_LENGTH))
                if (encodedSignature != zeroSignature) {
                    signatures.add(encodedSignature)
                }
            }

            // 2. decompile Message
            val numRequiredSignatures = byteArray.first().toInt()
            byteArray = byteArray.drop(1).toByteArray()
            val numReadonlySignedAccounts = byteArray.first().toInt()
            byteArray = byteArray.drop(1).toByteArray()
            val numReadonlyUnsignedAccounts = byteArray.first().toInt()
            byteArray = byteArray.drop(1).toByteArray()

            val accountDecodedLength = Binary.decodeLength(byteArray)
            byteArray = accountDecodedLength.bytes
            val accountKeys = mutableListOf<String>() // list of all accounts
            for (i in 0 until accountDecodedLength.length) {
                val account = byteArray.slice(0 until PUBLIC_KEY_LENGTH)
                byteArray = byteArray.drop(PUBLIC_KEY_LENGTH).toByteArray()
                accountKeys.add(Base58.encode(account.toByteArray()))
            }

            val recentBlockhash = byteArray.slice(0 until PUBLIC_KEY_LENGTH).toByteArray()
            byteArray = byteArray.drop(PUBLIC_KEY_LENGTH).toByteArray()

            val instructionDecodedLength = Binary.decodeLength(byteArray)
            byteArray = instructionDecodedLength.bytes
            val instructions = mutableListOf<Instruction>()
            for (i in 0 until instructionDecodedLength.length) {
                val programIdIndex = byteArray.first().toInt()
                byteArray = byteArray.drop(1).toByteArray()
                val programId = accountKeys[programIdIndex]

                val instructionAccountDecodedLength = Binary.decodeLength(byteArray)
                byteArray = instructionAccountDecodedLength.bytes

                val accountIndices =
                    byteArray.slice(0 until instructionAccountDecodedLength.length).toByteArray().toList().map(Byte::toInt)
                byteArray = byteArray.drop(instructionAccountDecodedLength.length).toByteArray()

                val dataDecodedLength = Binary.decodeLength(byteArray)
                byteArray = dataDecodedLength.bytes
                val dataSlice = byteArray.slice(0 until dataDecodedLength.length).toByteArray()
                byteArray = byteArray.drop(dataDecodedLength.length).toByteArray()
                instructions.add(
                    BaseInstruction(
                        programId = PublicKey(programId),
                        data = dataSlice,
                        keys =
                        accountIndices.map { accountIdx ->
                            AccountMeta(
                                publicKey = PublicKey(accountKeys[accountIdx]),
                                signer = accountIdx < numRequiredSignatures,
                                writable =
                                accountIdx < numRequiredSignatures - numReadonlySignedAccounts ||
                                    (
                                        accountIdx >= numRequiredSignatures &&
                                            accountIdx < accountKeys.count() - numReadonlyUnsignedAccounts
                                        ),
                            )
                        },
                    ),
                )
            }

            // 3. construct Transaction
            if (numRequiredSignatures <= 0) throw Exception("FeePayer does not exist")

            val tx =
                Transaction(
                    feePayer = PublicKey(accountKeys[0]),
                    recentBlockhash = Base58.encode(recentBlockhash),
                    instructions = instructions,
                )
            signatures.forEach { signature ->
                tx.addSignature(signature)
            }
            return tx
        }
    }
}
