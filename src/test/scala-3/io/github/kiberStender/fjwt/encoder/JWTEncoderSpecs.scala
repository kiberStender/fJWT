package io.github.kiberStender
package fjwt
package encoder

import cats.syntax.all.{catsSyntaxApplicativeId, catsSyntaxOptionId}
import org.scalatest.*
import flatspec.*
import io.github.kiberStender.fjwt.models.Claim
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA512
import io.github.kiberStender.fjwt.payload.Payload
import matchers.*

import java.time.{Instant, LocalDateTime, ZoneId}

class JWTEncoderSpecs extends AnyFlatSpecLike with should.Matchers with GivenWhenThen {

  private type F[T] = Either[Throwable, T]
  private val hs512Encoder: HmacAlgorithm = HmacSHA512
  private val privateKey = "super-secret-key-sixty-four-characters-long-to-satisfy-test-please"

  "JWTEncoder" should "generate a token with a Long as type for time" in {
    Given("A simple payload(John Doe, true) and issued time as 1516239022L")

    import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
    import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons
    import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.toLong

    val encoder: JWTEncoder[F, Long, Payload] = JWTEncoder.dsl(hs512Encoder)

    val payload: Payload = Payload("John Doe", true)
    val sub = "1234567890".some
    val iat = 1516239022L.some
    val expected: F[String] =
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0.z3LDxh4ejoYjtF1B3IJaosPXGqjM_qnKMiTZltLw7R-nfaRV71rI6nJXDflP0Z782Z2D1bimvleLpahltDDd5Q"
        .pure[F]
    val claim: Claim[Long] = Claim(sub = sub, iat = iat)

    When("Encoding the token")
    val actual: F[String] = encoder.encode(privateKey)(claim)(payload)

    Then(s"It should return $expected")
    for {
      act <- actual
      exp <- expected
    } yield act should be(exp)
  }

  "JwtEncoder" should "generate a token with a LocalDateTime as type for time" in {
    Given("A simple payload(John Doe, true) and issued time as 1516239022L")

    implicit val zoneId: ZoneId = ZoneId.of("UTC")

    import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
    import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons
    import io.github.kiberStender.fjwt.implicits.claim.Implicits.LocalDateTimeInstances.toLong

    val encoder: JWTEncoder[F, LocalDateTime, Payload] = JWTEncoder.dsl(hs512Encoder)

    val payload: Payload = Payload("John Doe", true)
    val sub = "1234567890".some
    val epochMilli = 1516239022L
    val iat = Instant.ofEpochMilli(epochMilli).atZone(zoneId).toLocalDateTime.some
    val expected: F[String] = {
      "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0.z3LDxh4ejoYjtF1B3IJaosPXGqjM_qnKMiTZltLw7R-nfaRV71rI6nJXDflP0Z782Z2D1bimvleLpahltDDd5Q"
        .pure[F]
    }
    val claim: Claim[LocalDateTime] = Claim(sub = sub, iat = iat)

    When("Encoding the token")
    val actual: F[String] = encoder.encode(privateKey)(claim)(payload)

    Then(s"It should return $expected")
    for {
      act <- actual
      exp <- expected
    } yield act should be(exp)
  }
}
