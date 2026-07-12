package io.github.kiberStender
package fjwt
package decoder

import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.exception.JWTError.{EmptyPrivateKey, EmptyToken, ExpiredToken, InvalidSignature, Not3TokenParts, NotMappedError, NullPrivateKey, NullToken}
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA512
import io.github.kiberStender.fjwt.models.{Claim, JWToken}
import io.github.kiberStender.fjwt.payload.Payload
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpecLike

import java.time.ZoneId

class JWTDecoderAllValidationsTest extends AnyFlatSpecLike with GivenWhenThen:
  private type F = [T] =>> Either[Throwable, T]
  private type Token = JWToken[Long, Payload]
  private implicit val zoneId: ZoneId = ZoneId.of("UTC")

  import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
  import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonDecoder
  import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.{fromLong, expirable}
  import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons

  private lazy val encodeAlg: HmacAlgorithm = HmacSHA512

  private lazy val decoder: JWTDecoder[F, Long, Payload] = JWTDecoder.allValidations(encodeAlg)

  private val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"

  "JWTDecoderAllValidations" should "decode a JWT token into a Payload object" in {
    // GIVEN
    val expectedPayload: Payload = payload.Payload("John Doe", true)
    val expectedClaim: Claim[Long] =
      Claim(None, Some("1234567890"), None, None, None, Some(1516239022), None)
    val expected: F[Token] = JWToken(HmacSHA512, expectedClaim, expectedPayload).pure[F]
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.dpgGN_CWoqP6dI-3gg5tZGw2OdSuk9_W7AkAppkGgldj1b8f9TDNQ-EA5yRuqOkDd9mzNrYXH_2zf4tmAazfdg"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode a JWT with less than 3 parts" in {
    // GIVEN
    val expected = (Not3TokenParts: Throwable).raiseError[F, Token]
    val input =
      "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae"

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode a JWT with an invalid signature" in {
    // GIVEN
    val expected = (InvalidSignature: Throwable).raiseError[F, Token]
    val input =
      "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae"

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode the payload when it is corrupted in any way" in {
    // GIVEN
    val expected = NotMappedError("exhausted input")
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMg.-6Qzi7rf1nusRaVvMDrdKAnQYTez-YrTLeqPR8iOEESCLAsbaxPuFEkfUxexszzVve2q99XzP6Zv4srKiBiVFg"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    actual.fold(throwable => assert(throwable.getMessage === expected.message), _ => assert(false))
  }
  it must "fail to decode when the the token is expired" in {
    // GIVEN
    val expected: F[Token] = (ExpiredToken: Throwable).raiseError[F, Token]
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6MTY2MzUxNzU1MzM0MiwibmJmIjpudWxsLCJpYXQiOjE2NjM1MTgxNTMzNDIsImp0aSI6bnVsbCwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.-MLy1sBbwHXccT-HvSapuLAysJWXVLRQ8keMBeWN8M2ApI_vH8kMjjWsDCzlyWPWJF0hOAgBSeDP2GPcPsh56g"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the the private key is null" in {
    // GIVEN
    val expected = (NullPrivateKey: Throwable).raiseError[F, Token]
    val key = null
    val input =
      "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245"

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the the private key is empty" in {
    // GIVEN
    val expected = (EmptyPrivateKey: Throwable).raiseError[F, Token]
    val key = ""
    val input =
      "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245"

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the token is null" in {
    // GIVEN
    val expected = (NullToken: Throwable).raiseError[F, Token]
    val input = null

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the token is empty" in {
    // GIVEN
    val expected = (EmptyToken: Throwable).raiseError[F, Token]
    val input = ""

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }
