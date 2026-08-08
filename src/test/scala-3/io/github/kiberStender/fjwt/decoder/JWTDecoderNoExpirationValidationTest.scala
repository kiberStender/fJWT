package io.github.kiberStender
package fjwt
package decoder

import cats.syntax.all.catsSyntaxApplicativeErrorId
import io.github.kiberStender.fjwt.exception.JWTError.{EmptyPrivateKeyError, EmptyTokenError, InvalidSignatureError, Not3TokenPartsError, NullPrivateKeyError, NullTokenError}
import io.github.kiberStender.fjwt.models.{Claim, JWToken}
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA512
import io.github.kiberStender.fjwt.payload.Payload
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpecLike

class JWTDecoderNoExpirationValidationTest extends AnyFlatSpecLike with GivenWhenThen:
  private type F = [T] =>> Either[Throwable, T]

  private type Token = JWToken[Long, Payload]

  private lazy val encodeAlg: HmacAlgorithm = HmacSHA512

  import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.fromLong
  import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
  import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonDecoder
  import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons

  private lazy val decoder: JWTDecoder[F, Long, Payload] = NoExpirationValidation.dsl(encodeAlg)

  private val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"

  private val isAdmin = true

  "JWTDecoderUseHeaderNoExpirationValidation" should "decode a given token" in {
    Given("An encrypted token and its private key")
    val expectedPayload: Payload = payload.Payload("John Doe", isAdmin)
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

  it must "fail to decode a token with the wrong signature" in {
    // GIVEN
    val expected: F[Token] = (InvalidSignatureError : Throwable).raiseError[F, Token]
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0.aknjhsdfhkjdsfjdfsajhdsfkj"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode a token with less than 3 parts" in {
    // GIVEN
    val expected: F[Token] = (Not3TokenPartsError : Throwable).raiseError[F, Token]
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode a token with less than 2 parts" in {
    // GIVEN
    val expected: F[Token] = (Not3TokenPartsError : Throwable).raiseError[F, Token]
    val input =
      "eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode when the the private key is null" in {
    // GIVEN
    val expected: F[Token] = (NullPrivateKeyError : Throwable).raiseError[F, Token]
    val key: Null = null
    val input =
      "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the the private key is empty" in {
    // GIVEN
    val expected: F[Token] = (EmptyPrivateKeyError : Throwable).raiseError[F, Token]
    val key = ""
    val input =
      "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the token is null" in {
    // GIVEN
    val expected: F[Token] = (NullTokenError : Throwable).raiseError[F, Token]
    val input: Null = null

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the token is empty" in {
    // GIVEN
    val expected: F[Token] = (EmptyTokenError : Throwable).raiseError[F, Token]
    val input = ""

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }