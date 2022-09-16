package br.com.fjwt
package decoder

import io.circe.*, io.circe.syntax.*
import cats.*, cats.syntax.all.*

import org.scalatest.flatspec.AnyFlatSpecLike
import br.com.fjwt.crypto.base64.decode.Base64Decoder
import br.com.fjwt.crypto.hs512.HS512Encoder

private final case class Payload(sub: String, name: String, admin: Boolean, iat: Long)

private given Decoder[Payload] = io.circe.generic.semiauto.deriveCodec

class JWTDecoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val base64Encoder: Base64Decoder[F] = Base64Decoder.dsl
  private lazy val hs512Encoder: HS512Encoder[F] = HS512Encoder.dsl
  private lazy val decoder: JWTDecoder[F] = JWTDecoder.dsl(base64Encoder, hs512Encoder)

  "JWTDecoder" should "decode a JWT token into a Payload object" in {
    // GIVEN
    val expected = Payload("1234567890", "John Doe", true, 1516239022).pure[F]
    val key = "kleber-super-secret-key"
    val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae6"
    
    // WHEN
    val actual = decoder.decode(key)(input)
    
    // THEN
    assert(actual === expected)
  }

  it must "fail to decode a JWT with less than 3 parts" in {
    // GIVEN
    val expected = "Token has to has 3 parts: header.payload.signature"
    val key = "kleber-super-secret-key"
    val input = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae"
    
    // WHEN
    val actual = decoder.decode(key)(input)
    
    // THEN
    assert(actual.isLeft)
    actual match {
      case Left(e) => assert(e.getMessage() === expected)
      case _ => assert(false)
    }
  }

  it must "fail to decode a JWT with an invalid signature" in {
    // GIVEN
    val expected = "JWT Signature does not match"
    val key = "kleber-super-secret-key"
    val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae"
    
    // WHEN
    val actual = decoder.decode(key)(input)
    
    // THEN
    assert(actual.isLeft)
    actual match {
      case Left(e) => assert(e.getMessage() === expected)
      case _ => assert(false)
    }
  }

  it must "fail to decode the payload when it is corrupted in any way" in {
    // GIVEN
    val expected = "exhausted input"
    val key = "kleber-super-secret-key"
    val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMg==.15cbeaac9859d150113e333a49d751fb37d52baca1e700f395db790848860120a942e0fa228d9ee3b4c62a377eeb5154084a85c9a4d09df2c5876a725844b447"
    
    // WHEN
    val actual = decoder.decode(key)(input)
    
    // THEN
    assert(actual.isLeft)
    actual match {
      case Left(e) => assert(e.getMessage() === expected)
      case _ => assert(false)
    }
  }