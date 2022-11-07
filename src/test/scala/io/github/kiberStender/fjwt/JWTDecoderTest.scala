package io.github.kiberStender
package fjwt

import io.github.kiberStender.fjwt.error.JWTError.*
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, catsSyntaxOptionId}
import io.circe.*
import io.circe.syntax.*
import io.github.kiberStender.fjwt.crypto.base64.Base64Decoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoder
import io.github.kiberStender.fjwt.error.JWTError
import org.scalatest.flatspec.AnyFlatSpecLike

import java.time.ZoneId

class JWTDecoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[JWTError, T]
  private lazy val base64Encoder: Base64Decoder[F] = Base64Decoder.dsl
  private lazy val hs512Encoder: HmacEncoder[F] = HmacEncoder.hs512Encoder
  private lazy val decoder: JWTDecoder[F] = JWTDecoder.dsl(base64Encoder, hs512Encoder)
  private given ZoneId = ZoneId.of("UTC")

  private final case class Payload(name: String, admin: Boolean)

  private given Codec[Payload] = io.circe.generic.semiauto.deriveCodec

  "JWTDecoder" should "decode a JWT token into a Payload object" in {
    // GIVEN
    val expected = Payload("John Doe", true).pure[F]
    val key = "kleber-super-secret-key"
    val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245"
    
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
    val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae"
    
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
    val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMg==.15cbeaac9859d150113e333a49d751fb37d52baca1e700f395db790848860120a942e0fa228d9ee3b4c62a377eeb5154084a85c9a4d09df2c5876a725844b447"
    
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
    val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6MTY2MzUxNzU1MzM0MiwibmJmIjpudWxsLCJpYXQiOjE2NjM1MTgxNTMzNDIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.cfb9339cac0b9f210502473f6302ee116c8ee60e7277d3d35aee34b8dd5bc6f6b8ab7b4fcb5050821f39e4531d6c09a9811c623bb10b560330eb26bb72636a0b"
    
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
    val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245"

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
    val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245"

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
