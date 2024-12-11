package org.sol4k

import org.sol4k.Constants.PUBLIC_KEY_LENGTH
import org.sol4k.instruction.CompiledInstruction
import org.sol4k.instruction.Instruction
import java.io.ByteArrayOutputStream

data class TransactionMessage internal constructor(
    internal val version: MessageVersion,
    internal val header: MessageHeader,
    internal val accounts: List<PublicKey>,
    internal val recentBlockhash: String,
    internal val instructions: List<CompiledInstruction>,
    internal val addressLookupTables: List<CompiledAddressLookupTable>,
) {

    internal enum class MessageVersion {
        Legacy,
        V0,
    }

    fun withNewBlockhash(blockhash: String): TransactionMessage {
        return TransactionMessage(version, header, accounts, blockhash, instructions, addressLookupTables)
    }

    fun serialize(): ByteArray {
        ByteArrayOutputStream().use { b ->
            if (version == MessageVersion.V0) {
                b.write(ByteArray(1) { 128.toByte() })
            }

            b.write(header.numRequireSignatures)
            b.write(header.numReadonlySignedAccounts)
            b.write(header.numReadonlyUnsignedAccounts)
            b.write(Binary.encodeLength(accounts.size))
            for (a in accounts) {
                b.write(a.bytes())
            }
            b.write(Base58.decode(recentBlockhash))
            b.write(Binary.encodeLength(instructions.size))
            for (i in instructions) {
                b.write(i.programIdIndex)
                b.write(Binary.encodeLength(i.accounts.size))
                for (a in i.accounts) {
                    b.write(a)
                }
                b.write(Binary.encodeLength(i.data.size))
                b.write(i.data)
            }

            if (version != MessageVersion.Legacy) {
                var validAddressLookupCount = 0
                ByteArrayOutputStream().use { accountLookupTableSerializedData ->
                    for (a in addressLookupTables) {
                        if (a.writableIndexes.isNotEmpty() || a.readonlyIndexes.isNotEmpty()) {
                            accountLookupTableSerializedData.write(a.publicKey.bytes())
                            accountLookupTableSerializedData.write(Binary.encodeLength(a.writableIndexes.size))
                            accountLookupTableSerializedData.write(a.writableIndexes)
                            accountLookupTableSerializedData.write(Binary.encodeLength(a.readonlyIndexes.size))
                            accountLookupTableSerializedData.write(a.readonlyIndexes)
                            validAddressLookupCount++
                        }
                    }

                    b.write(Binary.encodeLength(validAddressLookupCount))
                    b.write(accountLookupTableSerializedData.toByteArray())
                }
            }
            return b.toByteArray()
        }
    }

    companion object {

        @JvmStatic
        fun deserialize(d: ByteArray): TransactionMessage {
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
            for (i in 0 until instructionDecodedLength.length) {
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
                    ),
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
                        ),
                    )
                }
            }
            return TransactionMessage(
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

        @JvmStatic
        @JvmOverloads
        fun newMessage(
            feePayer: PublicKey,
            recentBlockhash: String,
            instruction: Instruction,
            addressLookupTableAccounts: List<AddressLookupTableAccount> = emptyList(),
        ): TransactionMessage = newMessage(feePayer, recentBlockhash, listOf(instruction), addressLookupTableAccounts)

        @JvmStatic
        @JvmOverloads
        fun newMessage(
            feePayer: PublicKey,
            recentBlockhash: String,
            instructions: List<Instruction>,
            addressLookupTableAccounts: List<AddressLookupTableAccount> = emptyList(),
        ): TransactionMessage {
            val addressLookupTableMaps = mutableListOf<Map<PublicKey, Int>>()
            addressLookupTableAccounts.forEach { addressLookupTableAccount ->
                val m = mutableMapOf<PublicKey, Int>()
                addressLookupTableAccount.addresses.forEachIndexed { i, publicKey ->
                    m[publicKey] = i
                }
                addressLookupTableMaps.add(m)
            }

            val compileKeys = newCompileKeys(feePayer, instructions)
            val allKeys = compileKeys.keyMetaMap.keys.sortedWith { p1, p2 ->
                val a = p1.bytes()
                val b = p2.bytes()
                for (i in 0 until minOf(a.size, b.size)) {
                    val comparison = a[i].compareTo(b[i])
                    if (comparison != 0) {
                        return@sortedWith comparison
                    }
                }
                return@sortedWith a.size.compareTo(b.size)
            }

            val writableSignedAccount = mutableListOf<PublicKey>()
            val readOnlySignedAccount = mutableListOf<PublicKey>()
            val writableUnsignedAccount = mutableListOf<PublicKey>()
            val readOnlyUnsignedAccount = mutableListOf<PublicKey>()
            val addressLookupTableAccountCount = addressLookupTableAccounts.size
            val addressLookupTableWritable = List(addressLookupTableAccountCount) { mutableListOf<PublicKey>() }
            val addressLookupTableWritableIdx = List(addressLookupTableAccountCount) { mutableListOf<Byte>() }
            val addressLookupTableReadonly = List(addressLookupTableAccountCount) { mutableListOf<PublicKey>() }
            val addressLookupTableReadonlyIdx = List(addressLookupTableAccountCount) { mutableListOf<Byte>() }

            next@ for (key in allKeys) {
                if (key == feePayer) {
                    continue@next
                }
                val meta = requireNotNull(compileKeys.keyMetaMap[key])
                if (meta.signer) {
                    if (meta.writable) {
                        writableSignedAccount.add(key)
                    } else {
                        readOnlySignedAccount.add(key)
                    }
                } else {
                    if (meta.writable) {
                        for (n in 0 until addressLookupTableMaps.size) {
                            val addressLookupTableMap = addressLookupTableMaps[n]
                            val idx = addressLookupTableMap[key]
                            if (idx != null && !meta.invoked) {
                                addressLookupTableWritable[n].add(key)
                                addressLookupTableWritableIdx[n].add(idx.toByte())
                                continue@next
                            }
                        }
                        // if not found in address lookup table
                        writableUnsignedAccount.add(key)
                    } else {
                        for (n in 0 until addressLookupTableMaps.size) {
                            val addressLookupTableMap = addressLookupTableMaps[n]
                            val idx = addressLookupTableMap[key]
                            if (idx != null && !meta.invoked) {
                                addressLookupTableReadonly[n].add(key)
                                addressLookupTableReadonlyIdx[n].add(idx.toByte())
                                continue@next
                            }
                        }
                        // if not found in address lookup table
                        readOnlyUnsignedAccount.add(key)
                    }
                }
            }

            writableSignedAccount.add(0, feePayer)

            val publicKeys = mutableListOf<PublicKey>().apply {
                addAll(writableSignedAccount)
                addAll(readOnlySignedAccount)
                addAll(writableUnsignedAccount)
                addAll(readOnlyUnsignedAccount)
            }
            val compiledAddressLookupTables = mutableListOf<CompiledAddressLookupTable>()
            var lookupAddressCount = 0
            for (i in 0 until addressLookupTableAccountCount) {
                publicKeys.addAll(addressLookupTableWritable[i])
                lookupAddressCount += addressLookupTableWritable[i].size
            }
            for (i in 0 until addressLookupTableAccountCount) {
                publicKeys.addAll(addressLookupTableReadonly[i])
                lookupAddressCount += addressLookupTableReadonly[i].size

                if (addressLookupTableWritable[i].size > 0 || addressLookupTableReadonly[i].size > 0) {
                    compiledAddressLookupTables.add(
                        CompiledAddressLookupTable(
                            addressLookupTableAccounts[i].key,
                            addressLookupTableWritableIdx[i].toByteArray(),
                            addressLookupTableReadonlyIdx[i].toByteArray(),
                        ),
                    )
                }
            }

            val publicKeyToIdx = mutableMapOf<PublicKey, Int>()
            publicKeys.forEachIndexed { i, publicKey ->
                publicKeyToIdx[publicKey] = i
            }
            val compiledInstructions = mutableListOf<CompiledInstruction>()
            instructions.forEach { instruction ->
                val accountIndex = mutableListOf<Int>()
                instruction.keys.forEach { account ->
                    accountIndex.add(requireNotNull(publicKeyToIdx[account.publicKey]))
                }
                compiledInstructions.add(
                    CompiledInstruction(
                        data = instruction.data,
                        accounts = accountIndex.toList(),
                        programIdIndex = requireNotNull(publicKeyToIdx[instruction.programId]),
                    ),
                )
            }

            return TransactionMessage(
                version = MessageVersion.V0,
                header = MessageHeader(
                    numRequireSignatures = writableSignedAccount.size + readOnlySignedAccount.size,
                    numReadonlySignedAccounts = readOnlySignedAccount.size,
                    numReadonlyUnsignedAccounts = readOnlyUnsignedAccount.size,
                ),
                accounts = publicKeys.subList(0, publicKeys.size - lookupAddressCount),
                recentBlockhash = recentBlockhash,
                instructions = compiledInstructions,
                addressLookupTables = compiledAddressLookupTables,
            )
        }

        private fun newCompileKeys(
            feePayer: PublicKey,
            instructions: List<Instruction>,
        ): CompileKeys {
            val m = mutableMapOf<PublicKey, CompileKeyMeta>()
            instructions.forEach { instruction ->
                // compile program
                var v = m[instruction.programId]
                if (v == null) {
                    v = CompileKeyMeta(signer = false, writable = false, invoked = false)
                }
                v.invoked = true
                m[instruction.programId] = v

                // compile accounts
                instruction.keys.forEachIndexed { _, account ->
                    var a = m[account.publicKey]
                    if (a == null) {
                        a = CompileKeyMeta(signer = false, writable = false, invoked = false)
                    }
                    a.signer = a.signer || account.signer
                    a.writable = a.writable || account.writable
                    m[account.publicKey] = a
                }
            }

            val p = requireNotNull(m[feePayer])
            p.signer = true
            p.writable = true
            m[feePayer] = p

            return CompileKeys(feePayer, m)
        }
    }
}

internal data class MessageHeader(
    val numRequireSignatures: Int,
    val numReadonlySignedAccounts: Int,
    val numReadonlyUnsignedAccounts: Int,
)

data class AddressLookupTableAccount(
    val key: PublicKey,
    val addresses: List<PublicKey>,
)

private data class CompileKeys(
    val payer: PublicKey,
    val keyMetaMap: Map<PublicKey, CompileKeyMeta>,
)

private data class CompileKeyMeta(
    var signer: Boolean,
    var writable: Boolean,
    var invoked: Boolean,
)
