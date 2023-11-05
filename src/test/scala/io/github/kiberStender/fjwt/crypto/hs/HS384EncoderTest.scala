package io.github.kiberStender.fjwt.crypto.hs

import io.github.kiberStender.fjwt.error.JWTError.{
  EmptyPrivateKey,
  EmptyToken,
  NullPrivateKey,
  NullToken
}
import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxApplicativeErrorId
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms.HmacSHA384
import io.github.kiberStender.fjwt.error.JWTError
import org.scalatest.flatspec.AnyFlatSpecLike

class HS384EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val encoder: HmacEncoderAlgorithms = HmacSHA384

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
    val actual: F[Array[Byte]] = encoder.encode(key)(input)

    // THEN
    actual.map(value => assert(value === expected))
  }
