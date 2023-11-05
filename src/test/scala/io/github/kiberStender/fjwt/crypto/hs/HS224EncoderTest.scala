package io.github.kiberStender.fjwt.crypto.hs

import io.github.kiberStender.fjwt.error.JWTError.{
  EmptyPrivateKey,
  EmptyToken,
  NullPrivateKey,
  NullToken
}
import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxApplicativeErrorId
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms.HmacSHA224
import io.github.kiberStender.fjwt.error.JWTError
import org.scalatest.flatspec.AnyFlatSpecLike

class HS224EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val encoder: HmacEncoderAlgorithms = HmacSHA224

  "HS224Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"
    val input = "test"
    val expected: Array[Byte] = Array(
      -107, 18, -54, -108, -122, 38, 7, -103, -94, 115, -114, 120, -68, -6, -101, -77, 28, 61, -103,
      -121, -1, -90, 84, -41, 59, 116, -72, 26
    )

    // WHEN
    val actual: F[Array[Byte]] = encoder.encode(key)(input)

    // THEN
    actual.map(value => assert(value === expected))
  }
