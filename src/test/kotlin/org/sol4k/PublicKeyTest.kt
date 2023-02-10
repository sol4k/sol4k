package org.sol4k

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PublicKeyTest {

    @Test
    fun bytes() {
        val input = byteArrayOf(
            -64,
            125,
            24,
            -57,
            -110,
            -101,
            0,
            90,
            -68,
            -92,
            53,
            123,
            91,
            -51,
            -14,
            -90,
            35,
            73,
            54,
            19,
            -64,
            -86,
            -26,
            85,
            44,
            -72,
            -116,
            -5,
            -76,
            -6,
            -22,
            -31,
        )
        val publicKey = PublicKey(input)

        val bytes = publicKey.bytes()

        assertArrayEquals(input, bytes)
    }

    @Test
    fun toBase58() {
        val input = byteArrayOf(
            -64,
            125,
            24,
            -57,
            -110,
            -101,
            0,
            90,
            -68,
            -92,
            53,
            123,
            91,
            -51,
            -14,
            -90,
            35,
            73,
            54,
            19,
            -64,
            -86,
            -26,
            85,
            44,
            -72,
            -116,
            -5,
            -76,
            -6,
            -22,
            -31,
        )
        val publicKey = PublicKey(input)

        val base58string = publicKey.toBase58()

        assertEquals("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx", base58string)
    }

    @Test
    fun testToString() {
        val input = byteArrayOf(
            -64,
            125,
            24,
            -57,
            -110,
            -101,
            0,
            90,
            -68,
            -92,
            53,
            123,
            91,
            -51,
            -14,
            -90,
            35,
            73,
            54,
            19,
            -64,
            -86,
            -26,
            85,
            44,
            -72,
            -116,
            -5,
            -76,
            -6,
            -22,
            -31,
        )
        val publicKey = PublicKey(input)

        val publicKeyString = publicKey.toString()

        assertEquals("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx", publicKeyString)
    }
}
