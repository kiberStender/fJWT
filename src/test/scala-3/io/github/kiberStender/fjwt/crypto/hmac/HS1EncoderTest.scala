package io.github.kiberStender
package fjwt
package crypto
package hmac

import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons
import org.scalatest.*
import flatspec.*
import io.github.kiberStender.fjwt.models.Header
import matchers.*

class HS1EncoderTest extends AnyFlatSpecLike with should.Matchers with GivenWhenThen:
  private type F = [T] =>> Either[Throwable, T]
  private val header: Header = new Header {
    val alg: String = "HS1"
  }

  "HS1Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"
    val input = "test"
    val expected: Array[Byte] = Array(
      120, 124, -32, -47, 70, 59, 60, -75, 6, -125, -123, -39, -85, -32, -58, 43, -49, 88, -5, 51
    )

    // WHEN
    val actual: F[Array[Byte]] = implicitly[Hmac[F]].hash(header)(key)(input)

    // THEN
    actual.map(value => value should be(expected))
  }