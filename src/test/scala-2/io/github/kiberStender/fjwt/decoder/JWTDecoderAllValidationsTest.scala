package io.github.kiberStender
package fjwt
package decoder

import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.claim.SimpleClaim
import io.github.kiberStender.fjwt.exception.JWTException.{EmptyPrivateKeyException, EmptyTokenException, ExpiredTokenException, InvalidSignatureException, Not3TokenPartsException, NotMappedException, NullPrivateKeyException, NullTokenException}
import io.github.kiberStender.fjwt.header.SimpleHeader
import io.github.kiberStender.fjwt.models.JWToken
import io.github.kiberStender.fjwt.payload.Payload
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpecLike

import java.time.ZoneId

class JWTDecoderAllValidationsTest extends AnyFlatSpecLike with GivenWhenThen {
  private type F[T] = Either[Throwable, T]
  private type Token = JWToken[SimpleHeader, SimpleClaim[Long], Payload]
  private implicit val zoneId: ZoneId = ZoneId.of("UTC")

  import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
  import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonDecoder
  import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.{fromLongToLong, expirable}
  import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons

  private val header: SimpleHeader = SimpleHeader("HS512", 1)

  private lazy val decoder: JWTDecoder[F, SimpleHeader, SimpleClaim[Long], Payload] = AllValidations.dsl(header)

  private val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"

  "JWTDecoderAllValidations" should "decode a JWT token into a Payload object" in {
    Given("A token")
    val expectedPayload: Payload = Payload("John Doe", true)
    val expectedClaim: SimpleClaim[Long] =
      SimpleClaim(None, Some("1234567890"), None, None, None, Some(1516239022), None, "extra")

    val expected: F[Token] = JWToken(header, expectedClaim, expectedPayload).pure[F]
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJleHRyYUZpZWxkIjoiZXh0cmEiLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0.RpfatL_4ldMevKdNTqdHE1mbRc8KwN1JH5RofDGDWzNCiE4QLd3kwFtArMbuKWtVfYsEe9cEdKHLNkD6jFbwJw"

    When("Decoding it")
    val actual: F[Token] = decoder.decode(key)(input)

    Then(s"it should return [$expected]")
    assert(actual === expected)
  }

  it must "fail to decode a JWT with less than 3 parts" in {
    // GIVEN
    val expected = (Not3TokenPartsException: Throwable).raiseError[F, Token]
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
    val expected = (InvalidSignatureException: Throwable).raiseError[F, Token]
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
    val expected = NotMappedException("exhausted input")
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
    val expected: F[Token] = (ExpiredTokenException: Throwable).raiseError[F, Token]
    val input =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoxNjYzNTE3NTUzMzQyLCJpYXQiOjE2NjM1MTgxNTMzNDIsImV4dHJhRmllbGQiOiJmaWVsZCIsIm5hbWUiOiJKb2huIERvZSIsImFkbWluIjp0cnVlfQ.ewbewULI1_Fc4UWOmHyvJAXp5o7Lg6yfk8A0-PUC0b_n-cuOcbrhzsQIeOsfWQxFBATmeT5EVYIqTSiJrATKqw"

    // WHEN
    val actual: F[Token] = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the the private key is null" in {
    // GIVEN
    val expected = (NullPrivateKeyException: Throwable).raiseError[F, Token]
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
    val expected = (EmptyPrivateKeyException: Throwable).raiseError[F, Token]
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
    val expected = (NullTokenException: Throwable).raiseError[F, Token]
    val input = null

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the token is empty" in {
    // GIVEN
    val expected = (EmptyTokenException: Throwable).raiseError[F, Token]
    val input = ""

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }
}
