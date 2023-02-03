package org.sol4k.bn

object Binary {
    fun uint32(value: Long): ByteArray {
        val byteArray = ByteArray(4)
        byteArray[0] = (0xFFL and value).toByte()
        byteArray[1] = (0xFFL and (value shr 8)).toByte()
        byteArray[2] = (0xFFL and (value shr 16)).toByte()
        byteArray[3] = (0xFFL and (value shr 24)).toByte()
        return byteArray
    }

    fun int64(value: Long): ByteArray {
        val byteArray = ByteArray(8)
        byteArray[0] = (0xFFL and value).toByte()
        byteArray[1] = (0xFFL and (value shr 8)).toByte()
        byteArray[2] = (0xFFL and (value shr 16)).toByte()
        byteArray[3] = (0xFFL and (value shr 24)).toByte()
        byteArray[4] = (0xFFL and (value shr 32)).toByte()
        byteArray[5] = (0xFFL and (value shr 40)).toByte()
        byteArray[6] = (0xFFL and (value shr 48)).toByte()
        byteArray[7] = (0xFFL and (value shr 56)).toByte()
        return byteArray
    }

    fun uint16(value: Int): ByteArray {
        val byteArray = ByteArray(2)
        byteArray[0] = (0xFF and value).toByte()
        byteArray[1] = (0xFF and (value shr 8)).toByte()
        return byteArray
    }

    fun encodeLength(len: Int): ByteArray {
        val out = ByteArray(10)
        var remLen = len
        var cursor = 0
        while (true) {
            var elem = remLen and 0x7f
            remLen = remLen shr 7
            if (remLen == 0) {
                val uint16 = uint16(elem)
                out[cursor] = uint16[0]
                out[cursor + 1] = uint16[1]
                break
            } else {
                elem = elem or 0x80
                val uint16 = uint16(elem)
                out[cursor] = uint16[0]
                out[cursor + 1] = uint16[1]
                cursor += 1
            }
        }
        val bytes = ByteArray(cursor + 1)
        System.arraycopy(out, 0, bytes, 0, cursor + 1)
        return bytes
    }
}
