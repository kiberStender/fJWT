package io.github.kiberStender
package fjwt
package crypto
package hmac

import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA512
import org.scalatest._
import flatspec._
import matchers._

class HS512EncoderTest extends AnyFlatSpecLike with should.Matchers with GivenWhenThen {
  private type F[T] = Either[Throwable, T]
  private lazy val encoder: HmacAlgorithm = HmacSHA512

  import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons

  "HS512Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"
    val input = "test"
    val expected: Array[Byte] =
      Array(
        25, -83, 85, 52, 10, 23, 95, -83, 12, -73, 64, -19, -26, -83, -104, 99, -11, -70, -103, -27,
        77, -20, -52, -1, 93, -8, 32, -66, 112, -45, 14, -64, 7, 46, -72, -12, 83, -16, -126, -44,
        85, -84, 38, -13, -128, -96, -22, 36, 33, -52, -28, 66, -80, 65, 23, 67, -86, -74, -70, -35,
        -36, 60, 78, 20
      )

    // WHEN
    val actual: F[Array[Byte]] = encoder.hash[F](key)(input)

    // THEN
    actual.map(value => assert(value === expected))
  }
}
