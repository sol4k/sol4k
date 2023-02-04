package org.sol4k

class PublicKey {
    private val bytes: ByteArray

    constructor(bytes: ByteArray) {
        this.bytes = bytes
    }

    constructor(publicKey: String) {
        this.bytes = Base58.decode(publicKey)
    }

    fun bytes(): ByteArray = bytes.copyOf()

    fun toBase58(): String = Base58.encode(this.bytes)

    override fun toString(): String = toBase58()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicKey

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}
