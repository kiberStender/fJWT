package br.com.fjwt
package crypto
package base64
package decode

import cats.*, cats.syntax.all.*

import org.scalatest.flatspec.AnyFlatSpecLike

class Base64DecoderTest extends AnyFlatSpecLike: 
  // private type F = [T] =>> Either[Throwable, T]
  private type F = [A] =>> A 
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
