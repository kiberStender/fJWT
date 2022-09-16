package br.com.fjwt
package encoder

import cats.*, cats.syntax.all.*
import io.circe.*

import org.scalatest.flatspec.AnyFlatSpecLike
import br.com.fjwt.crypto.base64.encode.Base64Encoder
import br.com.fjwt.crypto.hs512.HS512Encoder

private final case class Payload(sub: String, name: String, admin: Boolean, iat: Long)

private given Encoder[Payload] = io.circe.generic.semiauto.deriveCodec

class JWTEncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> T
  private lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl
  private lazy val hs512Encoder: HS512Encoder[F] = HS512Encoder.dsl
  private lazy val encoder: JWTEncoder[F] = JWTEncoder.dsl(base64Encoder, hs512Encoder)

  "JWTEncoder" should "generate a JWT Token" in {
    // GIVEN
    val payload = Payload("1234567890", "John Doe", true, 1516239022)
    val key = "kleber-super-secret-key"
    val expected = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae6".pure[F]

    // WHEN
    val actual = encoder.encode(key)(payload)

    // THEN
    assert(actual === expected)
  }