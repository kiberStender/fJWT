package br.com.fjwt
package crypto
package hs

import cats.*, cats.syntax.all.*

import org.scalatest.flatspec.AnyFlatSpecLike

class HMD5EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> T
  private lazy val hs1Encoder: HmacEncoder[F] = HmacEncoder.hs224Encoder

  "HMD5Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val privatekey = "kleber-super-secret-key"
    val header = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0="
    val input = s"$header.$payload"
    val expected = "2b3d5b8c2e058d7fe632cbde9123c521".pure[F]

    // WHEN
    val actual = hs1Encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }
