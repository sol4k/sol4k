package org.sol4k

import org.junit.jupiter.api.Test
import java.util.Base64
import kotlin.test.assertEquals


internal class TransactionTest {

    @Test
    fun shouldSign() {
        // https://jup.ag/ swap
        val t =
            "AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAoVQxhqYIKKYYeMpA6TRO9mmZnz7F7ey0Arxzusf/Es0tJwccDT11PCgZnvGncl43WttfK2QUfCBVUqNg8vpBi7S3yqkxCBRoNKvUQM6+vM7hdUBgKi+akZpbvaCpd1sVYfl6fiMQT0LnAXBDu2lQOARhtYi5QbgO4L6/gDqyD/dS+fPs/q96K8ow96krYAokWVzZaNzbWKSIcxNgQQzBKEgwkKzcQCjJktPFDq/uMmm1vR0JPHfzTSU/YmDHMVPYs3qLLQ4QY0S20HU2ioqmnunsWIpHYgUUVifOcbOi5XS4HL5/Tq7ETeVhqOTtChh/pFHz+eEhBUyQfl0VMvc6/zgMjqi5tCwzl46rfpfq7Ar6aeSwFEFdHMOzjAsCPJTqq91ipydsU+eIhTH/m/TKngg0D0n/6oHyWCREj1ntWq4ZfgINIPkc3KG4eh6BHDA91d4BVdrP+dBe5F+DHttZ3bCQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAALzjEbkBDAlaM77NkXMPfqXNLSveCkWI7UEgNs31WEWCMlyWPTiSJ8bs9ECkUjg2DC1oTmdr/EIQEjnvY2+n4WaXVyp4Ez121kLcUui/jLLFZEz/BwZK3Ilf9B9OcsEAeAwZGb+UhFzL/7K26csOb57yM5bvF9xJrLEObOkAAAAC0P/on9df2SnTAmx8pWHneSwmrNt/J3VFLMhqns4zl6Mb6evO+2606PWXzaqvJdDGxu+TC0vbg5HymAgNFL11hBHnVW/IxwG7udMVuzmgVB/2xst6j9I5RArHNola8E48Gm4hX/quBhPtof2NGGMA12sQ53BrrO1WYoPAAAAAAAQbd9uHXZaGT2cvhRs7reawctIXtX1s3kTqM9YV+/wCpfwvmlw/gXULgLIhT912jP0NhVJRdx73Gp6B8AFCvBgsIDwAFAvPQAgAPAAkDjA4AAAAAAAANBgAFABMLFAEBCwIABQwCAAAAgJaYAAAAAAAUAQUBEQ0GAAkAEQsUAQESGBQABQkSERIQEg4ADAgFCQoCBBQBAwYHEiPlF8uXeuOtKgEAAAAaZAABgJaYAAAAAAA2mRQAAAAAADIAABQDBQAAAQk="
        val tx = Transaction.from(t)
        val st = Base64.getEncoder().encodeToString(tx.serialize())

        val recoverTx = Transaction.from(st)
        val recoverSt = Base64.getEncoder().encodeToString(recoverTx.serialize())
        assertEquals(recoverSt, st)
    }
}

