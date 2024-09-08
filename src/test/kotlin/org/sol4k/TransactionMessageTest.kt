package org.sol4k

import org.junit.jupiter.api.Test
import org.sol4k.Constants.SYSTEM_PROGRAM
import org.sol4k.TransactionMessage.MessageVersion.V0
import org.sol4k.instruction.BaseInstruction
import org.sol4k.instruction.CompiledInstruction
import kotlin.test.assertEquals
import kotlin.test.assertSame

class TransactionMessageTest {

    @Test
    fun shouldCreateNewMessage() {
        val expectedMessage = TransactionMessage(
            version = V0,
            header = header(),
            accounts = listOf(
                feePayerPublicKey(),
                PublicKey("A4iUVr5KjmsLymUcv4eSKPedUtoaBceiPeGipKMYc69b"),
                SYSTEM_PROGRAM,
            ),
            recentBlockhash = "FwRYtTPRk5N4wUeP87rTw9kQVSwigB6kbikGzzeCMrW5",
            instructions = compiledInstructions(),
            addressLookupTables = emptyList(),
        )

        val message = TransactionMessage.newMessage(
            feePayer = feePayerPublicKey(),
            recentBlockhash = "FwRYtTPRk5N4wUeP87rTw9kQVSwigB6kbikGzzeCMrW5",
            instructions = listOf(
                BaseInstruction(
                    programId = SYSTEM_PROGRAM,
                    keys = listOf(
                        AccountMeta.signerAndWritable(feePayerPublicKey()),
                        AccountMeta.writable(PublicKey("A4iUVr5KjmsLymUcv4eSKPedUtoaBceiPeGipKMYc69b")),
                    ),
                    data = data(),
                ),
            ),
        )

        assertEquals(message, expectedMessage)
    }

    @Test
    fun shouldCreateNewMessageGivenEmptyAddressesInAddressLookupTables() {
        val expectedMessage = TransactionMessage(
            version = V0,
            header = header(),
            accounts = listOf(
                PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde"),
                PublicKey("2xNweLHLqrbx4zo1waDvgWJHgsUpPj8Y8icbAFeR4a8i"),
                SYSTEM_PROGRAM,
            ),
            recentBlockhash = "9rAtxuhtKn8qagc3UtZFyhLrw5zkh6etv43TibaXuSKo",
            instructions = compiledInstructions(),
            addressLookupTables = emptyList(),
        )

        val message = TransactionMessage.newMessage(
            feePayer = PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde"),
            recentBlockhash = "9rAtxuhtKn8qagc3UtZFyhLrw5zkh6etv43TibaXuSKo",
            instructions = listOf(
                BaseInstruction(
                    programId = SYSTEM_PROGRAM,
                    keys = listOf(
                        AccountMeta.signerAndWritable(PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde")),
                        AccountMeta.writable(PublicKey("2xNweLHLqrbx4zo1waDvgWJHgsUpPj8Y8icbAFeR4a8i")),
                    ),
                    data = data(),
                ),
            ),
            addressLookupTableAccounts = listOf(
                AddressLookupTableAccount(
                    key = PublicKey("HEhDGuxaxGr9LuNtBdvbX2uggyAKoxYgHFaAiqxVu8UY"),
                    addresses = emptyList(),
                )
            ),
        )

        assertEquals(expectedMessage, message)
    }

    @Test
    fun shouldCreateNewMessageGivenAddressLookupTables() {
        val expectedMessage = TransactionMessage(
            version = V0,
            header = header(),
            accounts = listOf(
                PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde"),
                SYSTEM_PROGRAM,
            ),
            recentBlockhash = "5EvWPqKeYfN2P7SAQZ2TLnXhV3Ltjn6qEhK1F279dUUW",
            instructions = listOf(
                CompiledInstruction(
                    programIdIndex = 1,
                    accounts = listOf(0, 2),
                    data = data(),
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

        val message = TransactionMessage.newMessage(
            feePayer = PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde"),
            recentBlockhash = "5EvWPqKeYfN2P7SAQZ2TLnXhV3Ltjn6qEhK1F279dUUW",
            instructions = listOf(
                BaseInstruction(
                    programId = SYSTEM_PROGRAM,
                    keys = listOf(
                        AccountMeta.signerAndWritable(PublicKey("9aE476sH92Vz7DMPyq5WLPkrKWivxeuTKEFKd2sZZcde")),
                        AccountMeta.writable(PublicKey("2xNweLHLqrbx4zo1waDvgWJHgsUpPj8Y8icbAFeR4a8i")),
                    ),
                    data = data(),
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

        assertEquals(expectedMessage, message)
    }

    @Test
    fun shouldCreateTransactionMessageWithNewBlockhash() {
        val inputMessage = TransactionMessage.newMessage(
            feePayer = feePayerPublicKey(),
            recentBlockhash = "FwRYtTPRk5N4wUeP87rTw9kQVSwigB6kbikGzzeCMrW5",
            instructions = listOf(
                BaseInstruction(
                    programId = SYSTEM_PROGRAM,
                    keys = listOf(
                        AccountMeta.signerAndWritable(feePayerPublicKey()),
                        AccountMeta.writable(PublicKey("A4iUVr5KjmsLymUcv4eSKPedUtoaBceiPeGipKMYc69b")),
                    ),
                    data = data(),
                ),
            ),
            addressLookupTableAccounts = emptyList(),
        )
        val expectedBlockHash = "EwRYtTPRk5N4wUeP87rTw9kQVSwigB6kbikGzzeCMrW4"

        val newMessage = inputMessage.withNewBlockhash(expectedBlockHash)

        assertEquals(inputMessage.version, newMessage.version)
        assertSame(inputMessage.header, newMessage.header)
        assertSame(inputMessage.accounts, newMessage.accounts)
        assertSame(inputMessage.instructions, newMessage.instructions)
        assertSame(inputMessage.addressLookupTables, newMessage.addressLookupTables)
        assertEquals(expectedBlockHash, newMessage.recentBlockhash)
    }

    private fun feePayerPublicKey() = PublicKey("EvN4kgKmCmYzdbd5kL8Q8YgkUW5RoqMTpBczrfLExtx7")

    private fun header() = MessageHeader(
        numRequireSignatures = 1,
        numReadonlySignedAccounts = 0,
        numReadonlyUnsignedAccounts = 1,
    )

    private fun compiledInstructions() = listOf(
        CompiledInstruction(
            programIdIndex = 2,
            accounts = listOf(0, 1),
            data = data(),
        )
    )

    private fun data(): ByteArray = listOf(2, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0).map { it.toByte() }.toByteArray()
}
