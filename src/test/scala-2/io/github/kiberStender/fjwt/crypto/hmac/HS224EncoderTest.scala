package io.github.kiberStender
package fjwt
package crypto
package hmac

import org.scalatest._
import flatspec._
import io.github.kiberStender.fjwt.models.Header
import matchers._

class HS224EncoderTest extends AnyFlatSpecLike with should.Matchers with GivenWhenThen {
  private type F[T] = Either[Throwable, T]
  private val header: Header = new Header {
    val alg: String = "HS224"
  }

  import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons

  "HS224Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"
    val input = "test"
    val expected: Array[Byte] = Array(
      -107, 18, -54, -108, -122, 38, 7, -103, -94, 115, -114, 120, -68, -6, -101, -77, 28, 61, -103,
      -121, -1, -90, 84, -41, 59, 116, -72, 26
    )

    // WHEN
    val actual: F[Array[Byte]] = implicitly[Hmac[F]].hash(header)(key)(input)

    // THEN
    actual.map(value => value should be(expected))
  }
}
