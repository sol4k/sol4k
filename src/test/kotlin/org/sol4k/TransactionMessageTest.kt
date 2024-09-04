package org.sol4k

import org.junit.jupiter.api.Test
import org.sol4k.Constants.SYSTEM_PROGRAM
import org.sol4k.instruction.BaseInstruction
import org.sol4k.instruction.CompiledInstruction
import kotlin.test.assertEquals

class TransactionMessageTest {
    @Test
    fun testNewMessage() {
        val data = listOf(2, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0).map { it.toByte() }.toByteArray()
        var message = TransactionMessage.newMessage(
            feePayer = PublicKey("EvN4kgKmCmYzdbd5kL8Q8YgkUW5RoqMTpBczrfLExtx7"),
            recentBlockhash = "FwRYtTPRk5N4wUeP87rTw9kQVSwigB6kbikGzzeCMrW5",
            instructions = listOf(
                BaseInstruction(
                    programId = SYSTEM_PROGRAM,
                    keys = listOf(
                        AccountMeta.signerAndWritable(PublicKey("EvN4kgKmCmYzdbd5kL8Q8YgkUW5RoqMTpBczrfLExtx7")),
                        AccountMeta.writable(PublicKey("A4iUVr5KjmsLymUcv4eSKPedUtoaBceiPeGipKMYc69b")),
                    ),
                    data = data,
                ),
            ),
            addressLookupTableAccounts = emptyList(),
        )
        var target = TransactionMessage(
            version = TransactionMessage.MessageVersion.Legacy,
            header = MessageHeader(
                numRequireSignatures = 1,
                numReadonlySignedAccounts = 0,
                numReadonlyUnsignedAccounts = 1,
            ),
            accounts = listOf(
                PublicKey("EvN4kgKmCmYzdbd5kL8Q8YgkUW5RoqMTpBczrfLExtx7"),
                PublicKey("A4iUVr5KjmsLymUcv4eSKPedUtoaBceiPeGipKMYc69b"),
                SYSTEM_PROGRAM,
            ),
            recentBlockhash = "FwRYtTPRk5N4wUeP87rTw9kQVSwigB6kbikGzzeCMrW5",
            instructions = listOf(
                CompiledInstruction(
                    programIdIndex = 2,
                    accounts = listOf(0, 1),
                    data = data,
                )
            ),
            addressLookupTables = emptyList(),
        )
        assertEquals(message, target)

        message = TransactionMessage.newMessage(
            feePayer = PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde"),
            recentBlockhash = "9rAtxuhtKn8qagc3UtZFyhLrw5zkh6etv43TibaXuSKo",
            instructions = listOf(
                BaseInstruction(
                    programId = SYSTEM_PROGRAM,
                    keys = listOf(
                        AccountMeta.signerAndWritable(PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde")),
                        AccountMeta.writable(PublicKey("2xNweLHLqrbx4zo1waDvgWJHgsUpPj8Y8icbAFeR4a8i")),
                    ),
                    data = data,
                ),
            ),
            addressLookupTableAccounts = listOf(
                AddressLookupTableAccount(
                    key = PublicKey("HEhDGuxaxGr9LuNtBdvbX2uggyAKoxYgHFaAiqxVu8UY"),
                    addresses = emptyList(),
                )
            ),
        )
        target = TransactionMessage(
            version = TransactionMessage.MessageVersion.V0,
            header = MessageHeader(
                numRequireSignatures = 1,
                numReadonlySignedAccounts = 0,
                numReadonlyUnsignedAccounts = 1,
            ),
            accounts = listOf(
                PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde"),
                PublicKey("2xNweLHLqrbx4zo1waDvgWJHgsUpPj8Y8icbAFeR4a8i"),
                SYSTEM_PROGRAM,
            ),
            recentBlockhash = "9rAtxuhtKn8qagc3UtZFyhLrw5zkh6etv43TibaXuSKo",
            instructions = listOf(
                CompiledInstruction(
                    programIdIndex = 2,
                    accounts = listOf(0, 1),
                    data = data,
                )
            ),
            addressLookupTables = emptyList(),
        )
        assertEquals(message, target)

        message = TransactionMessage.newMessage(
            feePayer = PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde"),
            recentBlockhash = "5EvWPqKeYfN2P7SAQZ2TLnXhV3Ltjn6qEhK1F279dUUW",
            instructions = listOf(
                BaseInstruction(
                    programId = SYSTEM_PROGRAM,
                    keys = listOf(
                        AccountMeta.signerAndWritable(PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde")),
                        AccountMeta.writable(PublicKey("2xNweLHLqrbx4zo1waDvgWJHgsUpPj8Y8icbAFeR4a8i")),
                    ),
                    data = data,
                ),
            ),
            addressLookupTableAccounts = listOf(
                AddressLookupTableAccount(
                    key = PublicKey("HEhDGuxaxGr9LuNtBdvbX2uggyAKoxYgHFaAiqxVu8UY"),
                    addresses = listOf(
                        PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde"),
                        PublicKey("2xNweLHLqrbx4zo1waDvgWJHgsUpPj8Y8icbAFeR4a8i"),
                    ),
                )
            ),
        )
        target = TransactionMessage(
            version = TransactionMessage.MessageVersion.V0,
            header = MessageHeader(
                numRequireSignatures = 1,
                numReadonlySignedAccounts = 0,
                numReadonlyUnsignedAccounts = 1,
            ),
            accounts = listOf(
                PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde"),
                SYSTEM_PROGRAM,
            ),
            recentBlockhash = "5EvWPqKeYfN2P7SAQZ2TLnXhV3Ltjn6qEhK1F279dUUW",
            instructions = listOf(
                CompiledInstruction(
                    programIdIndex = 1,
                    accounts = listOf(0, 2),
                    data = data,
                )
            ),
            addressLookupTables = listOf(
                CompiledAddressLookupTable(
                    publicKey = PublicKey("HEhDGuxaxGr9LuNtBdvbX2uggyAKoxYgHFaAiqxVu8UY"),
                    writableIndexes = byteArrayOf(1.toByte()),
                    readonlyIndexes = byteArrayOf(),
                )
            )
        )
        assertEquals(message, target)
    }

    @Test
    fun testWithNewBlockhash() {
        val data = listOf(2, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0).map { it.toByte() }.toByteArray()
        var message = TransactionMessage.newMessage(
            feePayer = PublicKey("EvN4kgKmCmYzdbd5kL8Q8YgkUW5RoqMTpBczrfLExtx7"),
            recentBlockhash = "FwRYtTPRk5N4wUeP87rTw9kQVSwigB6kbikGzzeCMrW5",
            instructions = listOf(
                BaseInstruction(
                    programId = SYSTEM_PROGRAM,
                    keys = listOf(
                        AccountMeta.signerAndWritable(PublicKey("EvN4kgKmCmYzdbd5kL8Q8YgkUW5RoqMTpBczrfLExtx7")),
                        AccountMeta.writable(PublicKey("A4iUVr5KjmsLymUcv4eSKPedUtoaBceiPeGipKMYc69b")),
                    ),
                    data = data,
                ),
            ),
            addressLookupTableAccounts = emptyList(),
        )
        message = message.withNewBlockhash("EwRYtTPRk5N4wUeP87rTw9kQVSwigB6kbikGzzeCMrW4")
        val target = TransactionMessage(
            version = TransactionMessage.MessageVersion.Legacy,
            header = MessageHeader(
                numRequireSignatures = 1,
                numReadonlySignedAccounts = 0,
                numReadonlyUnsignedAccounts = 1,
            ),
            accounts = listOf(
                PublicKey("EvN4kgKmCmYzdbd5kL8Q8YgkUW5RoqMTpBczrfLExtx7"),
                PublicKey("A4iUVr5KjmsLymUcv4eSKPedUtoaBceiPeGipKMYc69b"),
                SYSTEM_PROGRAM,
            ),
            recentBlockhash = "EwRYtTPRk5N4wUeP87rTw9kQVSwigB6kbikGzzeCMrW4",
            instructions = listOf(
                CompiledInstruction(
                    programIdIndex = 2,
                    accounts = listOf(0, 1),
                    data = data,
                )
            ),
            addressLookupTables = emptyList(),
        )
        assertEquals(message, target)
    }
}
