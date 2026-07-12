package io.github.kiberStender
package fjwt
package crypto
package hmac

import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA256
import org.scalatest._
import flatspec._
import matchers._

class HS256EncoderTest extends AnyFlatSpecLike with should.Matchers with GivenWhenThen {
  private type F[T] = Either[Throwable, T]
  private lazy val encoder: HmacAlgorithm = HmacSHA256

  import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons

  "HS256Encoder" should "encrypt a JWT Token" in {
    // GIVEN
    val key = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"
    val input = "test"
    val expected: Array[Byte] = Array(
      -40, -96, -50, 121, 125, -35, -44, -97, 116, -112, 100, -25, 38, -105, 90, 104, 11, -92, -117,
      -23, -48, 123, 9, 77, 7, 79, -22, 87, -13, -102, -18, -2
    )

    // WHEN
    val actual: F[Array[Byte]] = encoder.hash[F](key)(input)

    // THEN
    actual.map(value => assert(value === expected))
  }
}
