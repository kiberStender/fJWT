package io.github.kiberStender
package fjwt
package crypto
package base64

import cats.syntax.all.catsSyntaxApplicativeId
import io.github.kiberStender.fjwt.implicits.base64.Implicits
import org.scalatest.*
import flatspec.*
import matchers.*

class Base64EncoderTest extends AnyFlatSpecLike with should.Matchers with GivenWhenThen:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val base64Encoder: Base64Encoder[F] = Implicits.apacheCommonEncoder[F]

  "Base64Encoder" should "encrypt the header" in {
    Given("""the JSON {"alg":"HS512","typ":"JWT"}""")
    val input = """{"alg":"HS512","typ":"JWT"}"""
    val expected = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9".pure[F]

    When("encoding it")
    val actual = base64Encoder.encodeURLSafe(input)

    Then(s"it should return $expected")
    expected should be(actual)
  }

  it must "encrypt the payload" in {
    Given("""the JSON {"sub":"1234567890","name":"John Doe","admin":true,"iat":1516239022}""")
    val input = """{"sub":"1234567890","name":"John Doe","admin":true,"iat":1516239022}"""
    val expected =
      "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0"
        .pure[F]

    When("encoding it")
    val actual = base64Encoder.encodeURLSafe(input)

    Then(s"it should return $expected")
    expected should be(actual)
  }