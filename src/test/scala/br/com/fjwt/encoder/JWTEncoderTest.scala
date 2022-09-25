package br.com.fjwt
package encoder

import cats.*, cats.syntax.all.*
import io.circe.*

import org.scalatest.flatspec.AnyFlatSpecLike
import br.com.fjwt.crypto.base64.encode.Base64Encoder
import br.com.fjwt.crypto.hs.HmacEncoder
import java.time.ZoneId
import java.time.Instant

private final case class Payload(name: String, admin: Boolean)

private given Codec[Payload] = io.circe.generic.semiauto.deriveCodec

class JWTEncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> T
  private lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl
  private lazy val hs512Encoder: HmacEncoder[F] = HmacEncoder.hs512Encoder
  private lazy val encoder: JWTEncoder[F] = JWTEncoder.dsl(base64Encoder, hs512Encoder)
  private given zoneId: ZoneId = ZoneId.of("UTC")

  "JWTEncoder" should "generate a JWT Token" in {
    // GIVEN
    val payload = Payload("John Doe", true)
    val key = "kleber-super-secret-key"
    val sub = "1234567890".some
    val epochMilli = 1516239022L
    val iat = Instant.ofEpochMilli(epochMilli).atZone(zoneId).toLocalDateTime().some
    val expected = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f74447e66ac8cac9319125a823911cd8aab49a21af719a43deb39707837f25edb9c64d83f1428e962476bf26c028a08b9073e1b15dc032d4e730e1089fd9f245".pure[F]    

    // WHEN
    val actual = encoder.encode(key)(sub = sub, iat = iat)(payload)

    // THEN
    assert(actual === expected)
  }
  it should "also generate a JWT Token when expiration time is set" in {
    // GIVEN
    val payload = Payload("John Doe", true)
    val key = "kleber-super-secret-key"
    val sub = "1234567890".some
    val iatEpochMilli = 1516239022L
    val expEpochMilli = 1516239150L
    val iat = Instant.ofEpochMilli(iatEpochMilli).atZone(zoneId).toLocalDateTime().some
    val exp = Instant.ofEpochMilli(expEpochMilli).atZone(zoneId).toLocalDateTime().some
    val expected = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6MTUxNjIzOTE1MCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.f118dd93e74d970016455cdfeeaaf90bd2237dfc861b22d5c90b5e7ebabe95444fd66ed39f9c0b24eecd0921c1b5aff0e62a6e45d2c88e711b8cf660fb1fb6f6".pure[F]

    // WHEN
    val actual = encoder.encode(key)(sub = sub, exp = exp, iat = iat)(payload)

    // THEN
    assert(actual === expected)
  }