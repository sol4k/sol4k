package org.sol4k

import org.bitcoinj.core.Base58 as BitcoinjBase58

object Base58 {
    @JvmStatic
    fun encode(input: ByteArray): String = BitcoinjBase58.encode(input)

    @JvmStatic
    fun decode(input: String): ByteArray = BitcoinjBase58.decode(input)
}
