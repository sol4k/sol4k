package org.sol4k.rpc

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.sol4k.PublicKey

object PublicKeySerializer : KSerializer<PublicKey> {
    override fun deserialize(decoder: Decoder): PublicKey {
        return PublicKey(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: PublicKey) {
        encoder.encodeString(value.toString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("PublicKey", PrimitiveKind.STRING)
}