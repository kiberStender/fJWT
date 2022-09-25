package br.com.fjwt
package crypto
package hs

import cats.*, cats.syntax.all.*

import org.scalatest.flatspec.AnyFlatSpecLike

class HS256EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> T
  private lazy val hs1Encoder: HmacEncoder[F] = HmacEncoder.hs224Encoder

  "HS256Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val privatekey = "kleber-super-secret-key"
    val header = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0="
    val input = s"$header.$payload"
    val expected = "26f5f5c9a07a60022707cf1b68d5ff1b05bbe0f982d2c8528a94d46785e13095".pure[F]

    // WHEN
    val actual = hs1Encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }