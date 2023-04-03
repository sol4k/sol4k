package org.sol4k

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PublicKeyTest {

    @Test
    fun testBytes() {
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
    fun testToBase58() {
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

    @Test
    fun shouldFindProgramDerivedAddress() {
        val holder = PublicKey("CYLdTZhP8d1GDGeeNapgPdUcPiux1U9B26315x38TtbQ")
        val tokenAddress = PublicKey("CYLdTZhP8d1GDGeeNapgPdUcPiux1U9B26315x38TtbQ")

        val (publicKey, nonce) = PublicKey.findProgramDerivedAddress(holder, tokenAddress)

        assertEquals(nonce, 254)
        assertEquals("3W9cYxjkWXUPAsfGJ1GNdFiZsGEwcoopwMz4S8eAkkXd", publicKey.toBase58())
    }

    @Test
    fun shouldVerifySignature() {
        val publicKey = PublicKey("CQgQp6hWVzzCMLHoAW8zRhVdwkTAGj5wwzDfWvgP4xJc")
        val message = byteArrayOf(1, 2, 3, 4, 5)
        val signature = byteArrayOf(
            55,
            56,
            76,
            -35,
            87,
            12,
            -52,
            64,
            90,
            -100,
            11,
            45,
            8,
            -6,
            53,
            127,
            -8,
            33,
            -25,
            95,
            42,
            77,
            -76,
            -79,
            -46,
            -4,
            -103,
            17,
            -44,
            32,
            29,
            -20,
            -44,
            39,
            -55,
            110,
            15,
            19,
            -30,
            51,
            -103,
            5,
            97,
            20,
            -97,
            67,
            65,
            95,
            35,
            22,
            127,
            124,
            100,
            36,
            37,
            -128,
            -59,
            26,
            -122,
            -45,
            6,
            105,
            0,
            3
        )

        val result = publicKey.verify(signature, message)

        assertTrue("signature must be correct") { result }
    }

    @Test
    fun shouldFailToVerifySignature() {
        val publicKey = PublicKey("64uZYgmTYeB22nHczFPsPJSyqntjQhafzscK1ccKxmqe")
        val message = byteArrayOf(1, 2, 3, 4, 5)
        val signature = byteArrayOf(
            55,
            56,
            76,
            -35,
            87,
            12,
            -52,
            64,
            90,
            -100,
            11,
            45,
            8,
            -6,
            53,
            127,
            -8,
            33,
            -25,
            95,
            42,
            77,
            -76,
            -79,
            -46,
            -4,
            -103,
            17,
            -44,
            32,
            29,
            -20,
            -44,
            39,
            -55,
            110,
            15,
            19,
            -30,
            51,
            -103,
            5,
            97,
            20,
            -97,
            67,
            65,
            95,
            35,
            22,
            127,
            124,
            100,
            36,
            37,
            -128,
            -59,
            26,
            -122,
            -45,
            6,
            105,
            0,
            3
        )

        val result = publicKey.verify(signature, message)

        assertFalse("signature must be incorrect") { result }
    }
}
