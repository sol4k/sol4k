package org.sol4k

data class CompiledAddressLookupTable(
    val publicKey: PublicKey,
    val writableIndexes: ByteArray,
    val readonlyIndexes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompiledAddressLookupTable

        if (publicKey != other.publicKey) return false
        if (!writableIndexes.contentEquals(other.writableIndexes)) return false
        if (!readonlyIndexes.contentEquals(other.readonlyIndexes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + writableIndexes.contentHashCode()
        result = 31 * result + readonlyIndexes.contentHashCode()
        return result
    }
}
