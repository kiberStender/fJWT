package io.github.kiberStender.fjwt.utils

import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, catsSyntaxOptionId}
import io.github.kiberStender.fjwt.crypto.base64.Base64Decoder
import io.github.kiberStender.fjwt.error.JWTError.{ExpiredToken, InvalidSignature, NotMappedError, WrongTokenParts}
import io.github.kiberStender.fjwt.payload.Payload
import org.scalatest.flatspec.AnyFlatSpecLike

import java.time.ZoneId

class PayloadExtractorHeaderTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val base64Decoder: Base64Decoder[F] = Base64Decoder.dsl
  private given ZoneId = ZoneId.of("UTC")

  private lazy val payloadExtractor: PayloadExtractor[F, Payload] = PayloadExtractor.dsl(base64Decoder)

  "PayloadExtractorHeader" should "extract the payload " in {
    val expected: F[Payload] = Payload("John Doe", true).pure[F]
    val key: String = "kleber-super-secret-key"
    val input: String = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.29743acd123183547fe568550db9416a02ed3a7aeeb9c4a74ec73baf02bb688b58d58f6508f7f0ab956a22aaf619602359038abae336abf0e0ceb7b0a0268b64"

    // WHEN
    val actual: F[Payload] = payloadExtractor.extract(key)(input)

    // THEN
    assert(actual === expected)
  }

  it must "fail to decode a JWT with less than 3 parts" in {
    // GIVEN
    val expected: F[Payload] = WrongTokenParts.raiseError[F, Payload]
    val key: String = "kleber-super-secret-key"
    val input: String = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae"

    // WHEN
    val actual: F[Payload] = payloadExtractor.extract(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to extract the payload with an invalid signature" in {
    // GIVEN
    val expected: F[Payload] = InvalidSignature.raiseError[F, Payload]
    val key: String = "kleber-super-secret-key"
    val input: String = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae"

    // WHEN
    val actual: F[Payload] = payloadExtractor.extract(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode the payload when it is corrupted in any way" in {
    // GIVEN
    val expected: F[Payload] = NotMappedError("exhausted input").raiseError[F, Payload]
    val key: String = "kleber-super-secret-key"
    val input: String = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMg==.9ba31eaf501e464131e4b6ec0ae4ba5bf6ea0b9ebbcd2cce462d3d2fb538e1f279aa1850f6323cf8e95bbbf6a5a592a88f3bd4fa5dd69bc0345c60e9a6ab0f4f"

    // WHEN
    val actual: F[Payload] = payloadExtractor.extract(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }

  it must "fail to decode when the the token is expired" in {
    // GIVEN
    val expected: F[Payload] = ExpiredToken.raiseError[F, Payload]
    val key: String = "kleber-super-secret-key"
    val input: String = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6MTY2MzUxNzU1MzM0MiwibmJmIjpudWxsLCJpYXQiOjE2NjM1MTgxNTMzNDIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.1ce2db83ae064b10966a7e6a321358521a3557431bc6690f0787fc5908b357aea03a839839c99673d10f9c5b48793f09a956ef1f9e65c99ee54ede4a2b4fc3a2"

    // WHEN
    val actual: F[Payload] = payloadExtractor.extract(key)(input)

    // THEN
    assert(actual.isLeft)
    assert(actual === expected)
  }