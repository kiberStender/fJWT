package io.github.kiberStender
package fjwt
package decoder

import io.github.kiberStender.fjwt.error.JWTError.*
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, catsSyntaxOptionId}
import io.circe.*
import io.circe.syntax.*
import io.github.kiberStender.fjwt.crypto.base64.Base64Decoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoder
import io.github.kiberStender.fjwt.error.JWTError
import io.github.kiberStender.fjwt.payload.*
import io.github.kiberStender.fjwt.utils.PayloadExtractor
import org.scalatest.flatspec.AnyFlatSpecLike

import java.time.ZoneId

class JWTDecoderHeaderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val base64Decoder: Base64Decoder[F] = Base64Decoder.dsl

  private lazy val decoder: JWTDecoder[F, Payload] = JWTDecoder.dsl(base64Decoder)
  private given ZoneId = ZoneId.of("UTC")

  "JWTDecoderHeader" should "decode a JWT token into a Payload object" in {
    // GIVEN
    val expected = Payload("John Doe", true).pure[F]
    val key = "kleber-super-secret-key"
    val input = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.29743acd123183547fe568550db9416a02ed3a7aeeb9c4a74ec73baf02bb688b58d58f6508f7f0ab956a22aaf619602359038abae336abf0e0ceb7b0a0268b64"

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode a JWT with less than 3 parts" in {
    // GIVEN
    val expected = WrongTokenParts.raiseError[F, Payload]
    val key = "kleber-super-secret-key"
    val input = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae"

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode a JWT with an invalid signature" in {
    // GIVEN
    val expected = InvalidSignature.raiseError[F, Payload]
    val key = "kleber-super-secret-key"
    val input = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae"

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode the payload when it is corrupted in any way" in {
    // GIVEN
    val expected = NotMappedError("exhausted input").raiseError[F, Payload]
    val key = "kleber-super-secret-key"
    val input = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMg==.9ba31eaf501e464131e4b6ec0ae4ba5bf6ea0b9ebbcd2cce462d3d2fb538e1f279aa1850f6323cf8e95bbbf6a5a592a88f3bd4fa5dd69bc0345c60e9a6ab0f4f"

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }
  it must "fail to decode when the the token is expired" in {
    // GIVEN
    val expected = ExpiredToken.raiseError[F, Payload]
    val key = "kleber-super-secret-key"
    val input = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6MTY2MzUxNzU1MzM0MiwibmJmIjpudWxsLCJpYXQiOjE2NjM1MTgxNTMzNDIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.1ce2db83ae064b10966a7e6a321358521a3557431bc6690f0787fc5908b357aea03a839839c99673d10f9c5b48793f09a956ef1f9e65c99ee54ede4a2b4fc3a2"

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the the private key is null" in {
    // GIVEN
    val expected = NullPrivateKey.raiseError[F, Payload]
    val key = null
    val input = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245"

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the the private key is empty" in {
    // GIVEN
    val expected = EmptyPrivateKey.raiseError[F, Payload]
    val key = ""
    val input = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245"

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the the token is null" in {
    // GIVEN
    val expected = NullToken.raiseError[F, Payload]
    val key = "kleber-super-secret-key"
    val input = null

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the the token is empty" in {
    // GIVEN
    val expected = EmptyToken.raiseError[F, Payload]
    val key = "kleber-super-secret-key"
    val input = ""

    // WHEN
    val actual = decoder.decode(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }
