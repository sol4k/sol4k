@file:Suppress("unused")

package org.sol4k

import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val INSTRUCTION_REQUEST_UNITS = 0
private const val INSTRUCTION_REQUEST_HEAP_FRAME = 1
private const val INSTRUCTION_SET_COMPUTE_UNIT_LIMIT = 2
private const val INSTRUCTION_SET_COMPUTE_UNIT_PRICE = 3

internal fun computeBudget(data: List<ByteArray>): BigDecimal {
    if (data.size != 2) return BigDecimal.ZERO

    var unitLimit = 0
    var unitPrice = 0L
    data.map {
        var d = it
        val instruction = d.first().toInt()
        d = d.drop(1).toByteArray()
        when (instruction) {
            INSTRUCTION_SET_COMPUTE_UNIT_LIMIT -> {
                val buffer = ByteBuffer.wrap(d.take(4).toByteArray())
                    .order(ByteOrder.LITTLE_ENDIAN)
                unitLimit = buffer.asIntBuffer().get()
            }
            INSTRUCTION_SET_COMPUTE_UNIT_PRICE -> {
                // micro-lamports
                val buffer = ByteBuffer.wrap(d.take(8).toByteArray())
                    .order(ByteOrder.LITTLE_ENDIAN)
                unitPrice = buffer.asLongBuffer().get()
            }
            else -> {
                return BigDecimal.ZERO
            }
        }
    }
    if (unitLimit == 0 || unitPrice == 0L) {
        return BigDecimal.ZERO
    }
    val feeInLamports = Convert.microToLamport(BigDecimal(unitLimit).multiply(BigDecimal(unitPrice)))
    return Convert.lamportToSol(feeInLamports)
}

internal fun decodeComputeUnitLimit(data: ByteArray): Int {
    var d = data
    val instruction = d.first().toInt()
    if (instruction != INSTRUCTION_SET_COMPUTE_UNIT_LIMIT) {
        return 0
    }
    d = d.drop(1).toByteArray()
    val buffer = ByteBuffer.wrap(d.take(4).toByteArray())
        .order(ByteOrder.LITTLE_ENDIAN)
    return buffer.asIntBuffer().get()
}

// compute unit price in "micro-lamports"
internal fun decodeComputeUnitPrice(data: ByteArray): Long {
    var d = data
    val instruction = d.first().toInt()
    if (instruction != INSTRUCTION_SET_COMPUTE_UNIT_PRICE) {
        return 0
    }
    d = d.drop(1).toByteArray()
    val buffer = ByteBuffer.wrap(d.take(8).toByteArray())
        .order(ByteOrder.LITTLE_ENDIAN)
    return buffer.asLongBuffer().get()
}
