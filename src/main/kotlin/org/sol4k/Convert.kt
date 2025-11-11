package org.sol4k

import java.math.BigDecimal
import java.math.RoundingMode

object Convert {
    @JvmStatic
    fun lamportToSol(v: String): BigDecimal = lamportToSol(BigDecimal(v))

    @JvmStatic
    fun lamportToSol(v: BigDecimal): BigDecimal = v.divide(BigDecimal.TEN.pow(9)).setScale(9, RoundingMode.CEILING)

    @JvmStatic
    fun solToLamport(v: String): BigDecimal = solToLamport(BigDecimal(v))

    @JvmStatic
    fun solToLamport(v: BigDecimal): BigDecimal = v.multiply(BigDecimal.TEN.pow(9))

    @JvmStatic
    fun microToLamport(v: BigDecimal): BigDecimal = v.divide(BigDecimal.TEN.pow(6))

    @JvmStatic
    fun lamportToMicro(v: BigDecimal): BigDecimal = v.multiply(BigDecimal.TEN.pow(6))
}
