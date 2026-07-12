package io.github.kiberStender
package fjwt
package crypto
package base64

import cats.syntax.all.catsSyntaxApplicativeId
import io.github.kiberStender.fjwt.implicits.base64.Implicits
import org.scalatest.*
import flatspec.*
import matchers.*

class Base64DecoderTest extends AnyFlatSpecLike with should.Matchers with GivenWhenThen:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val base64Decoder: Base64Decoder[F] = Implicits.apacheCommonDecoder[F]

  "Base64Decoder" should "decrypt the header" in {
    Given("hashed eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9")
    val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val expected = """{"alg":"HS512","typ":"JWT"}""".pure[F]

    When("decoding it")
    val actual = base64Decoder.decode(input)

    Then(s"it should return $expected")
    expected should be(actual)
  }

  it must "decrypt the payload" in {
    Given("hashed eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0")
    val input =
      "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0"
    val expected =
      """{"sub":"1234567890","name":"John Doe","admin":true,"iat":1516239022}""".pure[F]

    When("decoding it")
    val actual = base64Decoder.decode(input)

    Then(s"it should return $expected")
    expected should be(actual)
  }
