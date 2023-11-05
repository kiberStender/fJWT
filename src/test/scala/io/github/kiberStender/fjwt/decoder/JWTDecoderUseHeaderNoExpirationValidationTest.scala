package io.github.kiberStender.fjwt.decoder

import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, catsSyntaxOptionId}
import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.error.JWTError.{
  EmptyPrivateKey,
  EmptyToken,
  InvalidSignature,
  Not2TokenParts,
  Not3TokenParts,
  NullPrivateKey,
  NullToken
}
import io.github.kiberStender.fjwt.model.Claim
import io.github.kiberStender.fjwt.payload.Payload
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpecLike

class JWTDecoderUseHeaderNoExpirationValidationTest extends AnyFlatSpecLike with GivenWhenThen:
  private type F = [T] =>> Either[Throwable, T]

  private lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl
  private lazy val base64Decoder: Base64Decoder[F] = Base64Decoder.dsl

  private lazy val decoder: JWTDecoder[F, Payload] =
    JWTDecoder.useHeaderNoExpirationValidation(base64Encoder, base64Decoder)

  private val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"

  "JWTDecoderUseHeaderNoExpirationValidation" should "decode a given token" in {
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

  it must "fail to decode a token with the wrong signature" in {
    // GIVEN
    val expected: F[(Claim, Payload)] = InvalidSignature.raiseError[F, (Claim, Payload)]
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0.aknjhsdfhkjdsfjdfsajhdsfkj"

    // WHEN
    val actual: F[(Claim, Payload)] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode a token with less than 3 parts" in {
    // GIVEN
    val expected: F[(Claim, Payload)] = Not3TokenParts.raiseError[F, (Claim, Payload)]
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0"

    // WHEN
    val actual: F[(Claim, Payload)] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode a token with less than 2 parts" in {
    // GIVEN
    val expected: F[(Claim, Payload)] = Not3TokenParts.raiseError[F, (Claim, Payload)]
    val input =
      "eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0"

    // WHEN
    val actual: F[(Claim, Payload)] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode when the the private key is null" in {
    // GIVEN
    val expected = NullPrivateKey.raiseError[F, Payload]
    val key = null
    val input =
      "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245"

    // WHEN
    val actual: F[(Claim, Payload)] = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the the private key is empty" in {
    // GIVEN
    val expected = EmptyPrivateKey.raiseError[F, Payload]
    val key = ""
    val input =
      "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245"

    // WHEN
    val actual: F[(Claim, Payload)] = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the token is null" in {
    // GIVEN
    val expected = NullToken.raiseError[F, Payload]
    val input = null

    // WHEN
    val actual: F[(Claim, Payload)] = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the token is empty" in {
    // GIVEN
    val expected = EmptyToken.raiseError[F, Payload]
    val input = ""

    // WHEN
    val actual: F[(Claim, Payload)] = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }
