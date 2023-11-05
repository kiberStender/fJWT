package io.github.kiberStender
package fjwt
package encoder

import io.github.kiberStender.fjwt.error.JWTError.{EmptyPrivateKey, NullPrivateKey}
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, catsSyntaxOptionId}
import io.github.kiberStender.fjwt.crypto.base64.Base64Encoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms.HmacSHA512
import io.github.kiberStender.fjwt.error.JWTError
import io.github.kiberStender.fjwt.payload.Payload
import org.scalatest.flatspec.AnyFlatSpecLike

import java.time.ZoneId

class JWTEncoderLongTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl
  private lazy val hs512Encoder: HmacEncoderAlgorithms = HmacSHA512
  private given ZoneId = ZoneId.of("UTC")
  private lazy val encoder: JWTEncoder[F, Long, Payload] =
    JWTEncoderLong.dsl(base64Encoder, hs512Encoder)
  private val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"

  "JWTEncoder" should "generate a JWT Token" in {
    // GIVEN
    val payload: Payload = Payload("John Doe", true)
    val sub = "1234567890".some
    val iat = 1516239022L.some
    val expected: F[String] =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZSwic3ViIjoiMTIzNDU2Nzg5MCIsImlhdCI6MTUxNjIzOTAyMn0.pRIXE9UMdVzVijSFY7xXTSKMVzLORcCdRm3P1oqLuKtgOiVSbrMaISpB-9OnlYP1-w3RHOQzaSJ7EcvAzbDSwQ"
        .pure[F]

    // WHEN
    val actual: F[String] = encoder.encode(key)(sub = sub, iat = iat)(payload)

    // THEN
    assert(actual === expected)
  }

  it should "also generate a JWT Token when expiration time is set" in {
    // GIVEN
    val payload: Payload = Payload("John Doe", true)
    val sub = "1234567890".some
    val iat = 1516239022L.some
    val exp = 1516239150L.some
    val expected =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZSwic3ViIjoiMTIzNDU2Nzg5MCIsImV4cCI6MTUxNjIzOTE1MCwiaWF0IjoxNTE2MjM5MDIyfQ.eqXAOsbTfWtHtalNJLw3ZvpM_yb7W19B36LnV-0WGYfycMvdPLEzCUJpthxNuJK2sXCcmwV1VYxfXPEkg3HeRA"
        .pure[F]

    // WHEN
    val actual = encoder.encode(key)(sub = sub, exp = exp, iat = iat)(payload)

    // THEN
    assert(actual === expected)
  }

  it should "raise an exception when the private keys is empty" in {
    // GIVEN
    val payload: Payload = Payload("John Doe", true)
    val key = ""
    val sub = "1234567890".some
    val iat = 1516239022L.some
    val exp = 1516239150L.some
    val expected = EmptyPrivateKey.raiseError[F, String]

    // WHEN
    val actual = encoder.encode(key)(sub = sub, exp = exp, iat = iat)(payload)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it should "raise an exception when the private keys is null" in {
    // GIVEN
    val payload = Payload("John Doe", true)
    val key: String = null
    val sub = "1234567890".some
    val iat = 1516239022L.some
    val exp = 1516239150L.some
    val expected = NullPrivateKey.raiseError[F, String]

    // WHEN
    val actual = encoder.encode(key)(sub = sub, exp = exp, iat = iat)(payload)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }
