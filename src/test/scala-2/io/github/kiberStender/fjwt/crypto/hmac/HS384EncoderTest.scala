package io.github.kiberStender
package fjwt
package crypto
package hmac

import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA384
import org.scalatest._
import flatspec._
import matchers._

class HS384EncoderTest extends AnyFlatSpecLike with should.Matchers with GivenWhenThen {
  private type F[T] = Either[Throwable, T]
  private lazy val encoder: HmacAlgorithm = HmacSHA384

  import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons

  "HS384Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"
    val input = "test"
    val expected: Array[Byte] =
      Array(
        -76, 120, 97, 46, -34, 102, 55, -28, 113, -39, 8, 102, -127, -19, -86, -120, -62, -27, 14,
        120, 121, 43, 46, 11, -61, -119, 102, 82, 8, -60, 116, -51, -44, -124, 11, 78, -28, -52,
        -73, -84, -41, -10, 78, -82, 12, 46, -56, -104
      )

    // WHEN
    val actual: F[Array[Byte]] = encoder.hash[F](key)(input)

    // THEN
    actual.map(value => assert(value === expected))
  }
}
