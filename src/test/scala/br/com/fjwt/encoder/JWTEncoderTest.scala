package br.com.fjwt
package encoder

import cats.*, cats.syntax.all.*
import io.circe.*

import org.scalatest.flatspec.AnyFlatSpecLike
import br.com.fjwt.crypto.base64.Base64Encoder
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
    val expected = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.29743acd123183547fe568550db9416a02ed3a7aeeb9c4a74ec73baf02bb688b58d58f6508f7f0ab956a22aaf619602359038abae336abf0e0ceb7b0a0268b64".pure[F]    

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
    val expected = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6MTUxNjIzOTE1MCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.e39c3a9ae2cc9453a4a0a58ad317f7c193ccfc5df0d1f4bb69aafe86c3f872c8f833f56dbe668940971866842ca04e6b77f9b7ae47cdfd82345c7dbc06d927e1".pure[F]

    // WHEN
    val actual = encoder.encode(key)(sub = sub, exp = exp, iat = iat)(payload)

    // THEN
    assert(actual === expected)
  }