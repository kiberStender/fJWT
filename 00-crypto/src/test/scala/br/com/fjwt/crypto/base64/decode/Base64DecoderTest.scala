package br.com.fjwt
package crypto
package base64
package decode

import cats.*, cats.syntax.all.*

import org.scalatest.flatspec.AnyFlatSpecLike

class Base64DecoderTest extends AnyFlatSpecLike: 
  private type F = [T] =>> Either[Throwable, T]
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

  it must "fail to decrypt the non Base64 string" in {
    // GIVEN
    val input = "d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae6"
    val expected = new IllegalArgumentException().raiseError[F, String]
    // WHEN
    val actual = base64Decoder.decode(input)

    // THEN
    assert(expected === actual)
  }

  it must "fail to decrypt the broken Base64 string" in {
    // GIVEN
    val input = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyM0"
    val expected = new IllegalArgumentException().raiseError[F, String]
    // WHEN
    val actual = base64Decoder.decode(input)

    // THEN
    assert(expected === actual)
  }
