package io.github.kiberStender
package fjwt
package crypto
package hmac

import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA224
import org.scalatest.*
import flatspec.*
import matchers.*

class HS224EncoderTest extends AnyFlatSpecLike with should.Matchers with GivenWhenThen:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val encoder: HmacAlgorithm = HmacSHA224

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
    val actual: F[Array[Byte]] = encoder.hash[F](key)(input)

    // THEN
    actual.map(value => value should be(expected))
  }