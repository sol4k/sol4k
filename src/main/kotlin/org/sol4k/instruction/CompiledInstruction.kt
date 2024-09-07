package org.sol4k.instruction

internal data class CompiledInstruction(
    val data: ByteArray,
    val accounts: List<Int>,
    val programIdIndex: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompiledInstruction

        if (!data.contentEquals(other.data)) return false
        if (accounts != other.accounts) return false
        if (programIdIndex != other.programIdIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + accounts.hashCode()
        result = 31 * result + programIdIndex
        return result
    }
}
