package br.com.fjwt
package crypto
package hs

import cats.*, cats.syntax.all.*

import org.scalatest.flatspec.AnyFlatSpecLike

class HS1EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> T
  private lazy val hs1Encoder: HmacEncoder[F] = HmacEncoder.hs1Encoder

  "HS1Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val privatekey = "kleber-super-secret-key"
    val header = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0="
    val input = s"$header.$payload"
    val expected = "32e3da8cf63fc3640a1fa350564154212cd3e403".pure[F]

    // WHEN
    val actual = hs1Encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }