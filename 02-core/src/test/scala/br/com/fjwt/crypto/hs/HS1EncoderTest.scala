package br.com.fjwt
package crypto
package hs

import br.com.fjwt.error.JWTError
import br.com.fjwt.error.JWTError.{EmptyPrivateKey, EmptyToken, NullPrivateKey, NullToken}
import br.com.fjwt.validation.CodecValidation

import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxApplicativeErrorId

import org.scalatest.flatspec.AnyFlatSpecLike

class HS1EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[JWTError, T]
  given CodecValidation[F] = CodecValidation.dsl
  private lazy val encoder: HmacEncoder[F] = HmacEncoder.hs1Encoder

  "HS1Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val privatekey = "kleber-super-secret-key"
    val header = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0="
    val input = s"$header.$payload"
    val expected = "32e3da8cf63fc3640a1fa350564154212cd3e403".pure[F]

    // WHEN
    val actual = encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }

  it must "not encrypt a JWT Token when privateKey is empty" in {
    // GIVEN
    val privatekey: String = ""
    val header = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0="
    val input: String = s"$header.$payload"
    val expected: F[String] = EmptyPrivateKey.raiseError[F, String]

    // WHEN
    val actual = encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }

  it must "not encrypt a JWT Token when privateKey is null" in {
    // GIVEN
    val privatekey: String = null
    val header = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0="
    val input: String = s"$header.$payload"
    val expected: F[String] = NullPrivateKey.raiseError[F, String]

    // WHEN
    val actual = encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }

  it must "not encrypt a JWT Token when token is empty" in {
    // GIVEN
    val privatekey: String = "kleber-super-secret-key"
    val input: String = ""
    val expected: F[String] = EmptyToken.raiseError[F, String]

    // WHEN
    val actual = encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }

  it must "not encrypt a JWT Token when token is null" in {
    // GIVEN
    val privatekey: String = "kleber-super-secret-key"
    val input: String = null
    val expected: F[String] = NullToken.raiseError[F, String]

    // WHEN
    val actual = encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }