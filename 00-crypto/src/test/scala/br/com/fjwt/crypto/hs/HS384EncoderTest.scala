package br.com.fjwt
package crypto
package hs

import cats.*, cats.syntax.all.*

import org.scalatest.flatspec.AnyFlatSpecLike

class HS384EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> T
  private lazy val hs1Encoder: HmacEncoder[F] = HmacEncoder.hs224Encoder

  "HS384Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val privatekey = "kleber-super-secret-key"
    val header = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0="
    val input = s"$header.$payload"
    val expected = "047d40827bec542ba8fead697bdc99315695be4f9d16457f6d4900008f12eb365ba10019c70c666e2a4848d8716a7a90".pure[F]

    // WHEN
    val actual = hs1Encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }