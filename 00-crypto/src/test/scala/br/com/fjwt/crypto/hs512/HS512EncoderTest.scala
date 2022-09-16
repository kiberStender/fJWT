package br.com.fjwt
package crypto
package hs512

import cats.*, cats.syntax.all.*

import org.scalatest.flatspec.AnyFlatSpecLike

class HS512EncoderTest extends AnyFlatSpecLike:
  private type F = [T] =>> T
  private lazy val hs512Encoder: HS512Encoder[F] = HS512Encoder.dsl

  "HS512Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val privatekey = "kleber-super-secret-key"
    val header = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9"
    val payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0="
    val input = s"$header.$payload"
    val expected = "d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae6".pure[F]

    // WHEN
    val actual = hs512Encoder.encode(privatekey)(input)

    // THEN
    assert(expected === actual)
  }
