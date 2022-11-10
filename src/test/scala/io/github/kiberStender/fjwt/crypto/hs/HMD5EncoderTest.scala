package io.github.kiberStender.fjwt.crypto.hs

import io.github.kiberStender.fjwt.error.JWTError.{EmptyPrivateKey, EmptyToken, NullPrivateKey, NullToken}
import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxApplicativeErrorId
import io.github.kiberStender.fjwt.error.JWTError
import org.scalatest.flatspec.AnyFlatSpecLike

class HMD5EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[JWTError, T]
  private lazy val encoder: HmacEncoder[F] = HmacEncoder.hMD5Encoder

  "HMD5Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val privatekey: String = "kleber-super-secret-key"
    val header = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0="
    val input: String = s"$header.$payload"
    val expected = "2b3d5b8c2e058d7fe632cbde9123c521".pure[F]

    // WHEN
    val actual = encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }
