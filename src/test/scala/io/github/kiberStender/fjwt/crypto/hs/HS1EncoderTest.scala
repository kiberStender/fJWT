package io.github.kiberStender.fjwt.crypto.hs

import io.github.kiberStender.fjwt.error.JWTError.{
  EmptyPrivateKey,
  EmptyToken,
  NullPrivateKey,
  NullToken
}
import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxApplicativeErrorId
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms.HmacSHA1
import io.github.kiberStender.fjwt.error.JWTError
import org.scalatest.flatspec.AnyFlatSpecLike

class HS1EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val encoder: HmacEncoderAlgorithms = HmacSHA1

  "HS1Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"
    val input = "test"
    val expected: Array[Byte] = Array(
      120, 124, -32, -47, 70, 59, 60, -75, 6, -125, -123, -39, -85, -32, -58, 43, -49, 88, -5, 51
    )

    // WHEN
    val actual: F[Array[Byte]] = encoder.encode(key)(input)

    // THEN
    actual.map(value => assert(value === expected))
  }
