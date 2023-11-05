package io.github.kiberStender.fjwt.decoder

import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, catsSyntaxOptionId}
import io.github.kiberStender.fjwt.crypto.base64.Base64Decoder
import io.github.kiberStender.fjwt.error.JWTError.Not2TokenParts
import io.github.kiberStender.fjwt.model.Claim
import io.github.kiberStender.fjwt.payload.Payload
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.GivenWhenThen

class JWTDecoderNoValidationTest extends AnyFlatSpecLike with GivenWhenThen:

  private type F = [T] =>> Either[Throwable, T]
  private lazy val base64Decoder: Base64Decoder[F] = Base64Decoder.dsl

  private lazy val decoder: JWTDecoder[F, Payload] = JWTDecoder.noValidation(base64Decoder)

  private val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"

  "JWTDecoderNoValidation" should "decode a given token" in {
    Given("An encrypted token and its private key")
    val expectedPayload: Payload = Payload("John Doe", true)
    val expectedClaim: Claim =
      Claim(None, Some("1234567890"), None, None, None, Some(1516239022), None)
    val expected = (expectedClaim, expectedPayload)
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0.z3LDxh4ejoYjtF1B3IJaosPXGqjM_qnKMiTZltLw7R-nfaRV71rI6nJXDflP0Z782Z2D1bimvleLpahltDDd5Q"

    When("Decoding this token")
    val actual: F[(Claim, Payload)] = decoder.decode(key)(input)

    Then("It should return the correct Payload")
    actual
      .fold(_ => assert(false), res => assert(res === expected))
  }

  it must "decode a token with only two parts" in {
    // GIVEN
    val expectedPayload: Payload = Payload("John Doe", true)
    val expectedClaim: Claim =
      Claim(None, Some("1234567890"), None, None, None, Some(1516239022), None)
    val expected: F[(Claim, Payload)] = (expectedClaim, expectedPayload).pure[F]
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0"

    // WHEN
    val actual: F[(Claim, Payload)] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode a token with less than 2 parts" in {
    // GIVEN
    val expected: F[(Claim, Payload)] = Not2TokenParts.raiseError[F, (Claim, Payload)]
    val input =
      "eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0"

    // WHEN
    val actual: F[(Claim, Payload)] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }
