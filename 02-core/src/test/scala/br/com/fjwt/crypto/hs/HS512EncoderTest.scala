package br.com.fjwt
package crypto
package hs

import br.com.fjwt.error.JWTError
import br.com.fjwt.error.JWTError.{EmptyPrivateKey, EmptyToken, NullPrivateKey, NullToken}
import br.com.fjwt.validation.CodecValidation

import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxApplicativeErrorId

import org.scalatest.flatspec.AnyFlatSpecLike

class HS512EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[JWTError, T]
  given CodecValidation[F] = CodecValidation.dsl
  private lazy val encoder: HmacEncoder[F] = HmacEncoder.hs512Encoder

  "HS512Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val privatekey = "kleber-super-secret-key"
    val header = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0="
    val input = s"$header.$payload"
    val expected = "d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae6".pure[F]

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
