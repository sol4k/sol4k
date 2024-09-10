package org.sol4k.api

import kotlinx.serialization.Serializable
import org.sol4k.PublicKey
import org.sol4k.rpc.BigIntegerSerializer
import org.sol4k.rpc.PublicKeySerializer
import java.math.BigInteger

@Serializable
data class AccountInfo<T>(
    val data: T,
    val executable: Boolean,
    @Serializable(with = BigIntegerSerializer::class)
    val lamports: BigInteger,
    @Serializable(with = PublicKeySerializer::class)
    val owner: PublicKey,
    @Serializable(with = BigIntegerSerializer::class)
    val rentEpoch: BigInteger,
    val space: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountInfo<*>

        if(data is ByteArray) {
            if (!data.contentEquals(other.data as ByteArray?)) return false
        } else {
            if (!data?.equals(other.data)!!) return false
        }
        if (executable != other.executable) return false
        if (lamports != other.lamports) return false
        if (owner != other.owner) return false
        if (rentEpoch != other.rentEpoch) return false
        if (space != other.space) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        if(data is ByteArray) {
            result = data.contentHashCode()
        } else {
            result = result.hashCode()
        }
        result = 31 * result + executable.hashCode()
        result = 31 * result + lamports.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + rentEpoch.hashCode()
        result = 31 * result + space.hashCode()
        return result
    }
}
