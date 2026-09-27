package io.github.kiberStender
package fjwt
package decoder

import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.claim.SimpleClaim
import io.github.kiberStender.fjwt.exception.JWTException.Not2TokenPartsException
import io.github.kiberStender.fjwt.header.SimpleHeader
import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.fromLongToLong
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonDecoder
import io.github.kiberStender.fjwt.models.JWToken
import io.github.kiberStender.fjwt.payload.Payload
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpecLike

class JWTDecoderNoValidationTest extends AnyFlatSpecLike with GivenWhenThen:

  private type F = [T] =>> Either[Throwable, T]
  private type Token = JWToken[SimpleHeader, SimpleClaim[Long], Payload]
  private val header: SimpleHeader = SimpleHeader("HS512", 1)

  private lazy val decoder: JWTDecoder[F, SimpleHeader, SimpleClaim[Long], Payload] = NoValidation.dsl

  private val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"

  "JWTDecoderNoValidation" should "decode a given token" in {
    Given("An encrypted token and its private key")
    val expectedPayload: Payload = Payload("John Doe", true)
    val expectedClaim: SimpleClaim[Long] =
      SimpleClaim(None, Some("1234567890"), None, None, None, Some(1516239022), None, "extra")
    val expected = JWToken(header, expectedClaim, expectedPayload)
    val input =
      "eyJhbGciOiJIUzUxMiIsImV4dHJhRmllbGQiOjF9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJleHRyYUZpZWxkIjoiZXh0cmEiLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0.HeMqdWIehOnkiqCUzMG2pzoSdKLHhRp3_bXuNf_0nf5QPby3oSo8d5MNG5MAoFYHxeJwGZLHTaZomaEPiC6NXg"

    When("Decoding this token")
    val actual: F[Token] = decoder.decode(key)(input)

    Then("It should return the correct Payload")
    actual
      .fold(_ => assert(false), res => assert(res === expected))
  }

  it must "decode a token with only two parts" in {
    // GIVEN
    val expectedPayload: Payload = Payload("John Doe", true)
    val expectedClaim: SimpleClaim[Long] =
      SimpleClaim(None, Some("1234567890"), None, None, None, Some(1516239022), None, "extra")
    val expected: F[Token] = JWToken(header, expectedClaim, expectedPayload).pure[F]
    val input =
      "eyJhbGciOiJIUzUxMiIsImV4dHJhRmllbGQiOjF9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJleHRyYUZpZWxkIjoiZXh0cmEiLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode a token with less than 2 parts" in {
    // GIVEN
    val expected: F[Token] = (Not2TokenPartsException: Throwable).raiseError[F, Token]
    val input =
      "eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }