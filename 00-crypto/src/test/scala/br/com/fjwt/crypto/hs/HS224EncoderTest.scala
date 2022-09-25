package br.com.fjwt
package crypto
package hs

import cats.*, cats.syntax.all.*

import org.scalatest.flatspec.AnyFlatSpecLike

class HS224EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> T
  private lazy val hs1Encoder: HmacEncoder[F] = HmacEncoder.hs224Encoder

  "HS224Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val privatekey = "kleber-super-secret-key"
    val header = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0="
    val input = s"$header.$payload"
    val expected = "7998a37cb093567e9b41b27fdf1bf0fd2e8e9a2634f23e85d6b2caf0".pure[F]

    // WHEN
    val actual = hs1Encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }
