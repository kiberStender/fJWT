package io.github.kiberStender.fjwt.crypto.hs

import io.github.kiberStender.fjwt.error.JWTError.{
  EmptyPrivateKey,
  EmptyToken,
  NullPrivateKey,
  NullToken
}
import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxApplicativeErrorId
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms.HmacSHA256
import io.github.kiberStender.fjwt.error.JWTError
import org.scalatest.flatspec.AnyFlatSpecLike

class HS256EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val encoder: HmacEncoderAlgorithms = HmacSHA256

  "HS256Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"
    val input = "test"
    val expected: Array[Byte] = Array(
      -40, -96, -50, 121, 125, -35, -44, -97, 116, -112, 100, -25, 38, -105, 90, 104, 11, -92, -117,
      -23, -48, 123, 9, 77, 7, 79, -22, 87, -13, -102, -18, -2
    )

    // WHEN
    val actual: F[Array[Byte]] = encoder.encode(key)(input)

    // THEN
    actual.map(value => assert(value === expected))
  }
