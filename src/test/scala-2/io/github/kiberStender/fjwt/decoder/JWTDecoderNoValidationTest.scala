package io.github.kiberStender
package fjwt
package decoder

import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.exception.JWTError.Not2TokenParts
import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.fromLong
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonDecoder
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA512
import io.github.kiberStender.fjwt.models.{Claim, JWToken}
import io.github.kiberStender.fjwt.payload.Payload
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpecLike

class JWTDecoderNoValidationTest extends AnyFlatSpecLike with GivenWhenThen {

  private type F[T] = Either[Throwable, T]
  private type Token = JWToken[Long, Payload]

  private lazy val decoder: JWTDecoder[F, Long, Payload] = JWTDecoder.noValidation

  private val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"

  "JWTDecoderNoValidation" should "decode a given token" in {
    Given("An encrypted token and its private key")
    val expectedPayload: Payload = Payload("John Doe", true)
    val expectedClaim: Claim[Long] =
      Claim(None, Some("1234567890"), None, None, None, Some(1516239022), None)
    val expected = JWToken(HmacSHA512, expectedClaim, expectedPayload)
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0.z3LDxh4ejoYjtF1B3IJaosPXGqjM_qnKMiTZltLw7R-nfaRV71rI6nJXDflP0Z782Z2D1bimvleLpahltDDd5Q"

    When("Decoding this token")
    val actual: F[Token] = decoder.decode(key)(input)

    Then("It should return the correct Payload")
    actual
      .fold(_ => assert(false), res => assert(res === expected))
  }

  it must "decode a token with only two parts" in {
    // GIVEN
    val expectedPayload: Payload = Payload("John Doe", true)
    val expectedClaim: Claim[Long] =
      Claim(None, Some("1234567890"), None, None, None, Some(1516239022), None)
    val expected: F[Token] = JWToken(HmacSHA512, expectedClaim, expectedPayload).pure[F]
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode a token with less than 2 parts" in {
    // GIVEN
    val expected: F[Token] = (Not2TokenParts: Throwable).raiseError[F, Token]
    val input =
      "eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }
}