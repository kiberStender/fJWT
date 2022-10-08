package br.com.fjwt
package crypto
package base64

import br.com.fjwt.error.JWTError
import br.com.fjwt.error.JWTError.EmptyToken
import br.com.fjwt.error.JWTError.NullToken
import br.com.fjwt.validation.CodecValidation

import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxApplicativeErrorId
import org.scalatest.flatspec.AnyFlatSpecLike

class Base64EncoderTest extends AnyFlatSpecLike: 
  private type F = [T] =>> Either[JWTError, T]
  private given CodecValidation[F] = CodecValidation.dsl
  private lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl

  "Base64Encoder" should "encrypt the header" in {
    // GIVEN
    val input = """{"alg":"HS512","typ":"JWT"}"""
    val expected = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9".pure[F]
    // WHEN
    val actual = base64Encoder.encode(input)

    // THEN
    assert(expected === actual)
  }

  it must "encrypt the payload" in {
    // GIVEN
    val input = """{"sub":"1234567890","name":"John Doe","admin":true,"iat":1516239022}"""
    val expected = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=".pure[F]
    // WHEN
    val actual = base64Encoder.encode(input)

    // THEN
    assert(expected === actual)
  }

  it must "not encrypt the payload when it is empty" in {
    // GIVEN
    val input: String = ""
    val expected: F[String] = EmptyToken.raiseError[F, String]
    // WHEN
    val actual = base64Encoder.encode(input)

    // THEN
    assert(expected === actual)
  }

  it must "not encrypt the payload when it is null" in {
    // GIVEN
    val input: String = null
    val expected: F[String] = NullToken.raiseError[F, String]
    // WHEN
    val actual = base64Encoder.encode(input)

    // THEN
    assert(expected === actual)
  }
