package br.com.fjwt
package crypto
package base64

import br.com.fjwt.error.JWTError
import br.com.fjwt.error.JWTError.EmptyToken
import br.com.fjwt.error.JWTError.NullToken
import br.com.fjwt.error.JWTError.NotMappedError
import br.com.fjwt.validation.CodecValidation
import cats.*
import cats.syntax.all.*
import org.scalatest.flatspec.AnyFlatSpecLike

class Base64DecoderTest extends AnyFlatSpecLike: 
  private type F = [T] =>> Either[JWTError, T]
  private given CodecValidation[F] = CodecValidation.dsl
  private lazy val base64Decoder: Base64Decoder[F] = Base64Decoder.dsl

  "Base64Decoder" should "decrypt the header" in {
    // GIVEN
    val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val expected = """{"alg":"HS512","typ":"JWT"}""".pure[F]
    // WHEN
    val actual = base64Decoder.decode(input)

    // THEN
    assert(expected === actual)
  }

  it must "decrypt the payload" in {
    // GIVEN
    val input = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0"
    val expected = """{"sub":"1234567890","name":"John Doe","admin":true,"iat":1516239022}""".pure[F]
    // WHEN
    val actual = base64Decoder.decode(input)

    // THEN
    assert(expected === actual)
  }

  it must "not decrypt when token is empty" in {
    // GIVEN
    val input = ""
    val expected: F[String] = EmptyToken.raiseError[F, String]
    // WHEN
    val actual = base64Decoder.decode(input)

    // THEN
    assert(expected === actual)
  }

  it must "not decrypt when token is null" in {
    // GIVEN
    val input: String = null
    val expected: F[String] = NullToken.raiseError[F, String]
    // WHEN
    val actual = base64Decoder.decode(input)

    // THEN
    assert(expected === actual)
  }

  it must "fail to decrypt the broken Base64 string" in {
    // GIVEN
    val input = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyM"
    val expected = NotMappedError("Last unit does not have enough valid bits").raiseError[F, String]
    // WHEN
    val actual = base64Decoder.decode(input)

    // THEN
    assert(expected === actual)
  }
