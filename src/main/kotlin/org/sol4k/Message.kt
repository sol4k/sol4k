package org.sol4k

import okio.Buffer
import okio.ByteString.Companion.toByteString
import org.sol4k.VersionedTransaction.Companion.PUBLIC_KEY_LENGTH
import org.sol4k.instruction.CompiledInstruction
import kotlin.experimental.and

data class Message(
    val version: MessageVersion,
    val header: MessageHeader,
    val accounts: List<PublicKey>,
    var recentBlockhash: String,
    val instructions: List<CompiledInstruction>,
    val addressLookupTables: List<CompiledAddressLookupTable>,
) {

    enum class MessageVersion {
        Legacy, V0
    }

    fun serialize(): ByteArray {
        val b = Buffer()
        if (version != MessageVersion.Legacy) {
            val v = version.name.substring(1).toIntOrNull()
            if (v == null || v > 255) {
                throw Exception("failed to parse message version")
            }
            if (v > 128) {
                throw Exception("unexpected message version")
            }
            b.writeByte(v.toByte() + 128.toByte())
        }

        b.writeByte(header.numRequireSignatures)
            .writeByte(header.numReadonlySignedAccounts)
            .writeByte(header.numReadonlyUnsignedAccounts)
            .write(Binary.encodeLength(accounts.size))
        for (a in accounts) {
            b.write(a.bytes())
        }
        b.write(Base58.decode(recentBlockhash))
            .write(Binary.encodeLength(instructions.size))
        for (i in instructions) {
            b.writeByte(i.programIdIndex)
                .write(Binary.encodeLength(i.accounts.size))
            for (a in i.accounts) {
                b.writeByte(a)
            }
            b.write(Binary.encodeLength(i.data.size))
                .write(i.data)
        }

        if (version != MessageVersion.Legacy) {
            var validAddressLookupCount = 0
            val accountLookupTableSerializedData = Buffer()
            for (a in addressLookupTables) {
                if (a.writableIndexes.isNotEmpty() || a.readonlyIndexes.isNotEmpty()) {
                    accountLookupTableSerializedData.write(a.publicKey.bytes())
                        .write(Binary.encodeLength(a.writableIndexes.size))
                        .write(a.writableIndexes)
                        .write(Binary.encodeLength(a.readonlyIndexes.size))
                        .write(a.readonlyIndexes)
                    validAddressLookupCount++
                }
            }

            b.write(Binary.encodeLength(validAddressLookupCount))
            b.write(accountLookupTableSerializedData.readByteString())
        }
        return b.readByteArray()
    }

    companion object {
        fun deserialize(d: ByteArray): Message {
            var data = d
            val v = data.first().toInt() and 0xFF
            val version = if (v > 127) {
                data = data.drop(1).toByteArray()
                MessageVersion.V0
            } else {
                MessageVersion.Legacy
            }

            val numRequiredSignatures = data.first().toInt().also { data = data.drop(1).toByteArray() }
            val numReadonlySignedAccounts = data.first().toInt().also { data = data.drop(1).toByteArray() }
            val numReadonlyUnsignedAccounts = data.first().toInt().also { data = data.drop(1).toByteArray() }

            val accountKeyDecodedLength = Binary.decodeLength(data)
            data = accountKeyDecodedLength.bytes
            val accountKeys = mutableListOf<PublicKey>() // list of all accounts
            for (i in 0 until accountKeyDecodedLength.length) {
                val account = data.slice(0 until PUBLIC_KEY_LENGTH)
                data = data.drop(PUBLIC_KEY_LENGTH).toByteArray()
                accountKeys.add(PublicKey(account.toByteArray()))
            }

            val recentBlockhash = data.slice(0 until PUBLIC_KEY_LENGTH).toByteArray().also {
                data = data.drop(PUBLIC_KEY_LENGTH).toByteArray()
            }

            val instructionDecodedLength = Binary.decodeLength(data)
            data = instructionDecodedLength.bytes
            val instructions = mutableListOf<CompiledInstruction>()
            for(i in 0 until instructionDecodedLength.length) {
                val programIdIndex = data.first().toInt().also { data = data.drop(1).toByteArray() }

                val accountDecodedLength = Binary.decodeLength(data)
                data = accountDecodedLength.bytes
                val accountIndices = data.slice(0 until accountDecodedLength.length).map(Byte::toInt).also {
                    data = data.drop(accountDecodedLength.length).toByteArray()
                }

                val dataDecodedLength = Binary.decodeLength(data)
                data = dataDecodedLength.bytes
                val dataSlice = data.slice(0 until dataDecodedLength.length).toByteArray().also {
                    data = data.drop(dataDecodedLength.length).toByteArray()
                }
                instructions.add(
                    CompiledInstruction(
                        programIdIndex = programIdIndex,
                        data = dataSlice,
                        accounts = accountIndices,
                    )
                )
            }

            val addressLookupTables = mutableListOf<CompiledAddressLookupTable>()
            if (version == MessageVersion.V0) {
                val addressLookupTableDecodedLength = Binary.decodeLength(data)
                data = addressLookupTableDecodedLength.bytes
                for (i in 0 until addressLookupTableDecodedLength.length) {
                    val account = data.slice(0 until PUBLIC_KEY_LENGTH).toByteArray().also {
                        data = data.drop(PUBLIC_KEY_LENGTH).toByteArray()
                    }
                    val writableAccountIdxDecodedLength = Binary.decodeLength(data)
                    data = writableAccountIdxDecodedLength.bytes
                    val writableAccountIdx = data.slice(0 until writableAccountIdxDecodedLength.length).toByteArray().also {
                        data = data.drop(writableAccountIdxDecodedLength.length).toByteArray()
                    }
                    val readOnlyAccountIdxDecodedLength = Binary.decodeLength(data)
                    data = readOnlyAccountIdxDecodedLength.bytes
                    val readOnlyAccountIdx = data.slice(0 until readOnlyAccountIdxDecodedLength.length).toByteArray().also {
                        data = data.drop(readOnlyAccountIdxDecodedLength.length).toByteArray()
                    }
                    addressLookupTables.add(
                        CompiledAddressLookupTable(
                            publicKey = PublicKey(account),
                            writableIndexes = writableAccountIdx,
                            readonlyIndexes = readOnlyAccountIdx,
                        )
                    )
                }
            }
            return Message(
                version = version,
                header = MessageHeader(
                    numRequireSignatures = numRequiredSignatures,
                    numReadonlySignedAccounts = numReadonlySignedAccounts,
                    numReadonlyUnsignedAccounts = numReadonlyUnsignedAccounts,
                ),
                accounts = accountKeys,
                recentBlockhash = Base58.encode(recentBlockhash),
                instructions = instructions,
                addressLookupTables = addressLookupTables,
            )
        }
    }
}

data class MessageHeader(
    val numRequireSignatures: Int,
    val numReadonlySignedAccounts: Int,
    val numReadonlyUnsignedAccounts: Int,
)