package io.github.kiberStender
package fjwt
package crypto
package base64

import io.github.kiberStender.fjwt.error.JWTError.EmptyToken
import io.github.kiberStender.fjwt.error.JWTError.NullToken
import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxApplicativeErrorId
import io.github.kiberStender.fjwt.error.JWTError
import org.scalatest.flatspec.AnyFlatSpecLike

class Base64EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl

  "Base64Encoder" should "encrypt the header" in {
    // GIVEN
    val input = """{"alg":"HS512","typ":"JWT"}"""
    val expected = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9".pure[F]
    // WHEN
    val actual = base64Encoder.encodeURLSafe(input)

    // THEN
    assert(expected === actual)
  }

  it must "encrypt the payload" in {
    // GIVEN
    val input = """{"sub":"1234567890","name":"John Doe","admin":true,"iat":1516239022}"""
    val expected =
      "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0"
        .pure[F]
    // WHEN
    val actual = base64Encoder.encodeURLSafe(input)

    // THEN
    assert(expected === actual)
  }
