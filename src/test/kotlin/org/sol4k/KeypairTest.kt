package org.sol4k

import org.junit.jupiter.api.Test
import org.sol4k.tweetnacl.TweetNaclFast.Signature
import kotlin.test.assertTrue

internal class KeypairTest {

    @Test
    fun shouldSign() {
        val secret = Base58.decode(
            "2h75aTMuyTXRrhLRkr8go9fYE7GEj1X3aX3zit91mBVTEkE8KKGNR69BU41zSyyVGBZSJAmHm5XUd4UAKDLD23WG"
        )
        val keypair = Keypair.fromSecretKey(secret)
        val publicKeyBytes = keypair.publicKey.bytes()
        val message = byteArrayOf(1, 2, 3, 4, 5)

        val signature = keypair.sign(message)

        val result = Signature(publicKeyBytes, ByteArray(0)).detached_verify(message, signature)
        assertTrue("signature must be correct") { result }
    }
}
